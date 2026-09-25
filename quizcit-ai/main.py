# main.py
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import numpy as np

from rag_chat import (
    AdviceData,
    ChatData,
    EmbedData,
    generate_advice,
    generate_chat_answer,
    embed_texts,
)

app = FastAPI()

# Hằng số chung cho thang đo theta/b (logit), dùng cho cả rasch_theta() và
# update_difficulty(). Đặt ở đây để tránh lặp lại theta_min/theta_max rải rác.
THETA_MIN = -4.0
THETA_MAX = 4.0
MIN_RESPONSES_FOR_CALIBRATION = 20  # xem giải thích trong update_difficulty
SHRINKAGE_K = 30  # n=30 -> weight_data=0.5 ; n=90 -> weight_data=0.75 ...


class AttemptData(BaseModel):
    oldTheta: float = 0.0
    responses: list  # [1,0,1,1,...]
    difficulties: list  # [0.2, -0.5, ...]
    weights: list        # [0.8, 0.3, 1.0, ...]


def rasch_theta(old_theta, responses, difficulties, weights, max_iter=20,
                theta_min=-4.0, theta_max=4.0):
    # Khởi tạo giá trị
    theta = old_theta
    prior_theta = old_theta
    # sigma điều khiển độ mạnh của prior N(prior_theta, sigma^2)
    # sigma nhỏ → prior mạnh → θ bị kéo về prior_theta nhiều hơn (ít tin dữ liệu)
    # sigma lớn → prior yếu → θ linh hoạt hơn (tin dữ liệu nhiều hơn)
    sigma = 1
    # Công thức toán học xác suất thống kê Bayes. sigma càng lớn => niềm tin rằng câu hỏi này đánh giá đúng càng cao, sigma càng thấp => niềm tin rằng câu hỏi này đánh giá đúng càng thấp 
    #sigma = 1, nghĩa là theta kì vọng nằm khoảng (prior_theta - sigma^2) => tin rằng đa số học viên có năng lực từ khoảng prior_theta - 1

    # Đổi kiểu dữ liệu sang numpy, cho việc thực hiện tính vector hóa nhanh hơn
    responses = np.array(responses, dtype=float)
    difficulties = np.array(difficulties, dtype=float)
    weights = np.array(weights, dtype=float)

    hessian = -1.0 / (sigma**2)

    if len(responses) == 0:
        theta_scaled = (theta - theta_min) / (theta_max - theta_min) * 4.0
        theta_scaled = float(np.clip(theta_scaled, 0.0, 4.0))
        return {
            "theta":          round(theta, 4),
            "theta_scaled":   round(theta_scaled, 4),
        }

    # Lặp tối đa 20 lần đề tìm theta tốt nhất, chặn trường hợp bị giao động mạnh và gây ra lặp vô tận
    for _ in range(max_iter):

        # Công thức IRT tính xác suất làm đúng câu hỏi
        # Càng cao thì kì vọng sinh viên trả lời đúng câu đó càng cao
        p = 1 / (1 + np.exp(-(theta - difficulties)))

        # gradient (chiều và độ lớn), thg này lớn thì sv đang làm tốt hơn những gì theta đang thể hiện => tăng theta và ngược lại
        gradient = np.sum(weights * (responses - p)) - (theta - prior_theta) / (sigma**2)

        # hessian (độ tin tưởng vào gradient)
        hessian = -np.sum(weights * p * (1 - p)) - 1 / (sigma**2)
        # Newton-Raphson update
        new_theta = theta - gradient / hessian
        new_theta = np.clip(new_theta, theta_min, theta_max)  # clamp đưa giá trị theta về khoảng [-4:4]

        # Convergencce, thực hiện kết thúc vòng lặp sớm nếu như đã hội tụ
        if abs(new_theta - theta) < 1e-4:
            theta = new_theta
            break
        theta = new_theta

    theta = float(theta)

    # theta_scaled: ánh xạ tuyến tính từ [-4, 4] (logit) sang [0, 4] để hiển thị.
    # Phải khớp với ThetaUtils.toScaled() ở backend.
    theta_scaled = (theta - theta_min) / (theta_max - theta_min) * 4.0
    theta_scaled = float(np.clip(theta_scaled, 0.0, 4.0))

    return {
        "theta":          round(theta, 4),         # logit gốc, dùng cho tính toán
        "theta_scaled":   round(theta_scaled, 4),  # [0, 4], dùng để hiển thị cho FE
    }


@app.post("/estimate")
def estimate_theta(data: AttemptData):
    return rasch_theta(data.oldTheta, data.responses, data.difficulties, data.weights)


# ---- Batch estimate: ước lượng theta cho NHIỀU skill trong 1 request ----
# Backend gọi endpoint này khi nộp bài để tránh N lần round-trip HTTP
# (mỗi skill 1 request) — vốn rất nặng khi service cold start trên Render.
class BatchItem(BaseModel):
    key: str            # skillId để backend map kết quả trả về
    oldTheta: float = 0.0
    responses: list
    difficulties: list
    weights: list


class BatchData(BaseModel):
    items: list[BatchItem]


@app.post("/estimate-batch")
def estimate_theta_batch(data: BatchData):
    results = []
    for item in data.items:
        estimated = rasch_theta(item.oldTheta, item.responses, item.difficulties, item.weights)
        results.append({
            "key":          item.key,
            "theta":        estimated["theta"],
            "theta_scaled": estimated["theta_scaled"],
        })
    return {"results": results}


# =============================================================================
# Chatbox lý thuyết (RAG) + sinh lời khuyên cải thiện — dùng Gemini API.
# Logic RAG (truy hồi + giới hạn theo chương đã mở khóa) nằm ở backend Java
# (ChatbotServiceImplement + TheoryChunkRepository, pgvector). AI Service ở
# đây chỉ đóng vai trò: (1) sinh embedding, (2) sinh câu trả lời/generation.
# =============================================================================

@app.post("/embed")
def embed(data: EmbedData):
    try:
        embeddings = embed_texts(data.texts)
    except RuntimeError as ex:
        raise HTTPException(status_code=502, detail=str(ex)) from ex
    return {"embeddings": embeddings}


@app.post("/chat")
def chat(data: ChatData):
    try:
        answer = generate_chat_answer(data)
    except RuntimeError as ex:
        raise HTTPException(status_code=502, detail=str(ex)) from ex
    return {"answer": answer}


@app.post("/advice")
def advice(data: AdviceData):
    try:
        text = generate_advice(data)
    except RuntimeError as ex:
        raise HTTPException(status_code=502, detail=str(ex)) from ex
    return {"advice": text}


# =============================================================================
# Item calibration (hiệu chỉnh độ khó câu hỏi cuối kỳ) — CHƯA gắn endpoint.
# Chỉ dùng để kiểm chứng bằng dữ liệu mô phỏng (xem test_calibration.py),
# chưa đủ dữ liệu thực tế (200-300 lượt/kỳ) để chạy calibration thật.
# =============================================================================

def update_difficulty(
    b_old: float,
    theta_raws: list,   # theta_raw lấy từ ability_history.theta, KHÔNG dùng theta_scaled
    responses: list,    # 0/1 từ responses.score, đúng thứ tự tương ứng với theta_raws
    max_iter: int = 20,
) -> float:
    """
    Cập nhật difficulty_b cho MỘT câu hỏi, dựa trên toàn bộ lượt làm bài
    tích lũy được của câu đó trong kỳ.

    Dùng Newton-Raphson để giải b tối đa hóa likelihood (giữ theta cố định),
    thay vì suy b từ theta_mean/p_observed — cách cũ là "khớp mô-men"
    (moment matching), dễ lệch khi mẫu nhỏ hoặc theta không đồng nhất do
    tính phi tuyến của hàm sigmoid (bất đẳng thức Jensen).

    Trọng số kết hợp b_old/b_new phụ thuộc vào n (số lượt phản hồi) thay vì
    decay cố định: n nhỏ -> tin b_old (giảng viên) nhiều hơn; n lớn -> tin
    b_mle (dữ liệu) nhiều hơn.
    """
    n = len(theta_raws)
    if n < MIN_RESPONSES_FOR_CALIBRATION:
        return round(b_old, 4)

    theta = np.array(theta_raws, dtype=float)
    y = np.array(responses, dtype=float)

    b = b_old  # khởi tạo từ giá trị hiện tại, hội tụ nhanh hơn

    for _ in range(max_iter):
        p = 1 / (1 + np.exp(-(theta - b)))

        # d/db [ log P ]: b tăng thì p giảm, nên dấu ngược với gradient
        # theo theta trong rasch_theta().
        gradient = -np.sum(y - p)
        hessian = -np.sum(p * (1 - p))

        if hessian == 0:
            break

        b_new = b - gradient / hessian
        b_new = float(np.clip(b_new, THETA_MIN, THETA_MAX))

        if abs(b_new - b) < 1e-4:
            b = b_new
            break
        b = b_new

    b_mle = float(b)

    # Shrinkage theo n: weight_data -> 1 khi n -> vô cực.
    weight_data = n / (n + SHRINKAGE_K)
    b_final = b_old * (1 - weight_data) + b_mle * weight_data

    return round(float(np.clip(b_final, THETA_MIN, THETA_MAX)), 4)