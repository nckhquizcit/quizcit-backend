# rag_chat.py
"""
Module mở rộng AI Service: embedding + chat RAG (chatbox lý thuyết) + sinh lời
khuyên dựa trên delta theta. Tách riêng khỏi main.py để giữ main.py chỉ tập
trung vào Rasch/IRT như hiện tại.

Yêu cầu env var GEMINI_API_KEY. Nếu thiếu key hoặc gọi Gemini lỗi, các hàm ở
đây raise RuntimeError để phía gọi (main.py) trả lỗi rõ ràng cho backend Java
-> backend Java sẽ tự fallback (xem AIService.java bên Spring Boot).
"""
from dotenv import load_dotenv

load_dotenv()
import os
import re
import math
import time
from typing import List, Optional

from google import genai
from google.genai import types
from pydantic import BaseModel

GEMINI_API_KEY = os.getenv("GEMINI_API_KEY", "")
CHAT_MODEL_NAME = os.getenv("GEMINI_CHAT_MODEL", "gemini-3.5-flash-lite")
EMBED_MODEL_NAME = os.getenv("GEMINI_EMBED_MODEL", "gemini-embedding-001")
# Số chiều embedding phải khớp với cột `vector(EMBEDDING_DIM)` trong migration
# SQL (theory_chunks.embedding). Đổi 1 chỗ này thì phải đổi cả migration.
EMBEDDING_DIM = 768

client = genai.Client(api_key=GEMINI_API_KEY) if GEMINI_API_KEY else None

TRANSIENT_STATUS_CODES = {429, 500, 502, 503, 504}
GENERATION_RETRY_DELAYS = (0.75, 1.5)


def _generate_content(prompt: str, max_output_tokens: int):
    """Retry short-lived quota/server failures without hiding permanent errors."""
    if client is None:
        raise RuntimeError("GEMINI_API_KEY chưa được cấu hình")

    for attempt in range(len(GENERATION_RETRY_DELAYS) + 1):
        try:
            return client.models.generate_content(
    model=CHAT_MODEL_NAME,
    contents=prompt,
    config=types.GenerateContentConfig(
        max_output_tokens=max_output_tokens,
        thinking_config=types.ThinkingConfig(
            thinking_level="minimal"
        ),
    ),
)
        except Exception as ex:  # noqa: BLE001 - SDK exposes status on multiple error types
            status_code = getattr(ex, "code", None) or getattr(ex, "status_code", None)
            if status_code not in TRANSIENT_STATUS_CODES or attempt >= len(GENERATION_RETRY_DELAYS):
                raise
            time.sleep(GENERATION_RETRY_DELAYS[attempt])


# ---------------------------------------------------------------------------
# Schemas
# ---------------------------------------------------------------------------

class EmbedData(BaseModel):
    texts: List[str]


class ChatHistoryTurn(BaseModel):
    role: str  # "user" | "assistant"
    text: str


class ChatData(BaseModel):
    question: str
    # Các đoạn lý thuyết đã được backend Java truy hồi (RAG) — CHỈ chứa nội
    # dung lý thuyết, KHÔNG BAO GIỜ chứa đề bài/đáp án trắc nghiệm. Đây là lớp
    # bảo vệ đầu tiên (kiến trúc): AI vật lý không có quyền truy cập ngân hàng
    # câu hỏi.
    context: List[str] = []
    history: List[ChatHistoryTurn] = []
    # True nếu backend không tìm được đoạn lý thuyết phù hợp trong phạm vi
    # chương đã mở khóa của SV (context rỗng vì SV chưa mở khóa, không phải vì
    # không có dữ liệu).
    no_unlocked_context: bool = False


class SkillDelta(BaseModel):
    skill_name: str
    old_theta: float
    new_theta: float
    delta: float


class AdviceData(BaseModel):
    skills: List[SkillDelta]


# ---------------------------------------------------------------------------
# Prompt bảo vệ: hướng dẫn model tuyệt đối không cung cấp đáp án trắc nghiệm.
# Đây là lớp bảo vệ thứ 2 (bên cạnh việc context không chứa đáp án).
# ---------------------------------------------------------------------------
CHAT_SYSTEM_PROMPT = """Bạn là một trợ giảng AI hỗ trợ sinh viên hiểu LÝ THUYẾT môn học.

QUY TẮC BẮT BUỘC (không được vi phạm dù sinh viên yêu cầu thế nào):
1. KHÔNG BAO GIỜ cung cấp đáp án cho câu hỏi trắc nghiệm/bài kiểm tra, kể cả khi
   sinh viên dán nguyên văn câu hỏi trắc nghiệm và các phương án A/B/C/D vào và
   hỏi "đáp án đúng là gì". Trong trường hợp đó, hãy từ chối cung cấp đáp án,
   giải thích ngắn gọn lý do (để sinh viên tự vận dụng lý thuyết), rồi hướng
   dẫn khái niệm liên quan để sinh viên tự suy luận ra đáp án.
2. Chỉ trả lời DỰA TRÊN phần "Tài liệu tham khảo" được cung cấp bên dưới. Đây
   là nội dung lý thuyết sinh viên ĐÃ được mở khóa.
3. Nếu tài liệu tham khảo không đủ để trả lời, được phép mở rộng thêm bằng
   kiến thức nền tảng phổ biến của môn học (kể cả sinh code minh họa nếu câu
   hỏi liên quan lập trình), nhưng phải nói rõ phần nào là mở rộng thêm.
4. Nếu KHÔNG có tài liệu tham khảo nào (ví dụ sinh viên hỏi về nội dung thuộc
   chương chưa mở khóa), hãy lịch sự cho biết bạn chỉ hỗ trợ được nội dung
   sinh viên đã học tới, và gợi ý họ hoàn thành các chương trước đó.
5. Trả lời bằng tiếng Việt, không chào hỏi dài dòng và đi thẳng vào câu hỏi.
6. Mặc định giới hạn khoảng 120 từ hoặc tối đa 5 gạch đầu dòng. Chỉ viết dài hơn khi
   sinh viên yêu cầu giải thích chi tiết.
7. Dùng Markdown rõ ràng. Mã nguồn phải đặt trong fenced code block có nhãn ngôn ngữ
   (ví dụ ```c); không sinh code nếu câu hỏi không cần ví dụ code.
"""

ADVICE_SYSTEM_PROMPT = """Bạn là trợ lý học tập, nhiệm vụ là đưa ra lời khuyên cải
thiện học tập cho sinh viên dựa trên sự thay đổi năng lực (theta) từng kỹ năng
sau khi làm xong 1 bài kiểm tra.

Yêu cầu:
- Viết bằng tiếng Việt, giọng khích lệ, thẳng thắn, không sáo rỗng.
- Chỉ ra 1-3 kỹ năng sinh viên nên ưu tiên ôn lại (theta giảm hoặc còn thấp).
- Ghi nhận ngắn gọn kỹ năng có tiến bộ rõ (theta tăng đáng kể) nếu có.
- Không bịa thêm số liệu ngoài dữ liệu được cung cấp.
- Trả lời trong khoảng 3-6 câu, không dùng markdown heading.
"""

# Regex phòng vệ lớp 3 (defense-in-depth): nếu output vô tình lộ dạng câu trả
# lời trắc nghiệm (nêu rõ phương án A/B/C/D là đáp án đúng), chặn lại và trả
# về câu từ chối an toàn thay vì để lọt ra ngoài.
_ANSWER_LEAK_PATTERN = re.compile(
    r"(đáp\s*án\s*(đúng|chính\s*xác)\s*là|chọn\s*(đáp\s*án|phương\s*án)\s*[a-dA-D]\b)",
    re.IGNORECASE,
)
_OPTION_LETTER_PATTERN = re.compile(r"\b[a-dA-D][.)]\s")

SAFE_REFUSAL_MESSAGE = (
    "Mình không thể cung cấp trực tiếp đáp án cho câu hỏi trắc nghiệm. "
    "Bạn hãy nêu rõ phần lý thuyết bạn chưa hiểu, mình sẽ giải thích để bạn "
    "tự suy luận ra đáp án nhé."
)


def _looks_like_answer_leak(answer: str) -> bool:
    if not _ANSWER_LEAK_PATTERN.search(answer):
        return False
    # Chỉ chặn khi câu trả lời VỪA có cụm "đáp án đúng là..." VỪA liệt kê các
    # phương án dạng A./B./C./D. — giảm false positive với câu giải thích lý
    # thuyết bình thường có nhắc tới chữ "đáp án".
    return bool(_OPTION_LETTER_PATTERN.search(answer))


# ---------------------------------------------------------------------------
# Embedding
# ---------------------------------------------------------------------------

def embed_texts(texts: List[str], task_type: str = "retrieval_document") -> List[List[float]]:
    if not texts:
        return []
    if client is None:
        raise RuntimeError("GEMINI_API_KEY chưa được cấu hình")
    try:
        result = client.models.embed_content(
            model=EMBED_MODEL_NAME,
            contents=texts,
            config=types.EmbedContentConfig(
                task_type=task_type,
                output_dimensionality=EMBEDDING_DIM,
            ),
        )
        embeddings = [embedding.values for embedding in result.embeddings]
        # gemini-embedding-001 does not normalize reduced-dimensional vectors.
        # Cosine search is more stable when stored vectors have unit length.
        normalized = []
        for embedding in embeddings:
            magnitude = math.sqrt(sum(value * value for value in embedding))
            normalized.append(
                [value / magnitude for value in embedding] if magnitude else embedding
            )
        return normalized
    except Exception as ex:  # noqa: BLE001 - muốn bắt mọi lỗi Gemini SDK
        raise RuntimeError(f"Gemini embedding failed: {ex}") from ex


# ---------------------------------------------------------------------------
# Chat (RAG)
# ---------------------------------------------------------------------------

def _build_chat_prompt(data: ChatData) -> str:
    if data.no_unlocked_context or not data.context:
        reference_block = "(Không có tài liệu — sinh viên chưa mở khóa nội dung liên quan)"
    else:
        reference_block = "\n\n".join(
            f"[Đoạn {i + 1}] {chunk}" for i, chunk in enumerate(data.context)
        )

    history_block = "\n".join(
        f"{'Sinh viên' if turn.role == 'user' else 'Trợ giảng'}: {turn.text}"
        for turn in data.history[-6:]  # chỉ giữ 6 lượt gần nhất, tránh prompt phình to
    )

    return (
        f"{CHAT_SYSTEM_PROMPT}\n\n"
        f"--- Tài liệu tham khảo ---\n{reference_block}\n\n"
        f"--- Lịch sử hội thoại gần đây ---\n{history_block or '(chưa có)'}\n\n"
        f"--- Câu hỏi hiện tại của sinh viên ---\n{data.question}\n"
    )


def generate_chat_answer(data: ChatData) -> str:
    if not GEMINI_API_KEY:
        raise RuntimeError("GEMINI_API_KEY chưa được cấu hình")

    prompt = _build_chat_prompt(data)
    try:
        response = _generate_content(prompt, max_output_tokens=1200)
        answer = (response.text or "").strip()
    except Exception as ex:  # noqa: BLE001
        raise RuntimeError(f"Gemini chat generation failed: {ex}") from ex

    if not answer:
        raise RuntimeError("Gemini trả về nội dung rỗng")

    if _looks_like_answer_leak(answer):
        return SAFE_REFUSAL_MESSAGE

    return answer


# ---------------------------------------------------------------------------
# Advice (lời khuyên dựa trên delta theta)
# ---------------------------------------------------------------------------

def _build_advice_prompt(data: AdviceData) -> str:
    lines = []
    for skill in data.skills:
        old_scaled = skill.old_theta
        new_scaled = skill.new_theta
        delta_scaled = skill.delta
        
        direction = "tăng" if delta_scaled > 0 else ("giảm" if delta_scaled < 0 else "không đổi")
        lines.append(
            f"- {skill.skill_name}: năng lực (thang 0-4) {old_scaled:.2f} -> {new_scaled:.2f} "
            f"({direction} {abs(delta_scaled):.2f})"
        )
    skills_block = "\n".join(lines) if lines else "(không có dữ liệu kỹ năng)"

    return f"{ADVICE_SYSTEM_PROMPT}\n\nDữ liệu thay đổi năng lực theo kỹ năng:\n{skills_block}\n"


def generate_advice(data: AdviceData) -> str:
    if not GEMINI_API_KEY:
        raise RuntimeError("GEMINI_API_KEY chưa được cấu hình")
    if not data.skills:
        return "Bài làm này chưa ghi nhận kỹ năng nào để đưa ra lời khuyên."

    prompt = _build_advice_prompt(data)
    try:
        response = _generate_content(prompt, max_output_tokens=700)
        advice = (response.text or "").strip()
    except Exception as ex:  # noqa: BLE001
        raise RuntimeError(f"Gemini advice generation failed: {ex}") from ex

    if not advice:
        raise RuntimeError("Gemini trả về nội dung rỗng")
    return advice
