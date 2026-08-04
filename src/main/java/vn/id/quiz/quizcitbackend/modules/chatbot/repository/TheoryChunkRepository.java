package vn.id.quiz.quizcitbackend.modules.chatbot.repository;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

/**
 * Truy cập bảng {@code theory_chunks} (xem migration
 * {@code backend/migration/V__ai_chatbot_and_advice.sql}).
 *
 * <p>Dùng {@link JdbcTemplate} + native SQL thay vì JPA entity vì Hibernate
 * không có mapping chuẩn cho kiểu {@code vector} của pgvector; cách đơn giản
 * và ít phụ thuộc nhất là truyền embedding dưới dạng literal
 * {@code '[0.1,0.2,...]'::vector} qua tham số String — không cần thêm thư
 * viện {@code pgvector-java} hay đăng ký custom type ở tầng JDBC driver.</p>
 */
@Repository
@RequiredArgsConstructor
public class TheoryChunkRepository {

    private final JdbcTemplate jdbcTemplate;

    public void deleteByTheoryId(String theoryId) {
        jdbcTemplate.update("DELETE FROM theory_chunks WHERE theory_id = ?", theoryId);
    }

    /**
     * Lưu các chunk mới cho 1 theory. Gọi {@link #deleteByTheoryId} trước để
     * đảm bảo re-index không để lại chunk cũ mồ côi.
     */
    public void insertChunks(String theoryId, String chapterId, List<String> contents, List<float[]> embeddings) {
        if (contents.size() != embeddings.size()) {
            throw new IllegalArgumentException("contents và embeddings phải cùng kích thước");
        }
        for (int i = 0; i < contents.size(); i++) {
            String chunkId = UUID.randomUUID().toString();
            String vectorLiteral = toVectorLiteral(embeddings.get(i));
            jdbcTemplate.update(
                    """
                    INSERT INTO theory_chunks (chunk_id, theory_id, chapter_id, chunk_index, content, embedding)
                    VALUES (?, ?, ?, ?, ?, ?::vector)
                    """,
                    chunkId, theoryId, chapterId, i, contents.get(i), vectorLiteral);
        }
    }

    /**
     * Tìm top-k chunk gần nhất với {@code queryEmbedding} (cosine distance,
     * toán tử {@code <=>} của pgvector), CHỈ trong phạm vi
     * {@code allowedChapterIds} — đây chính là nơi thực thi ràng buộc
     * "chỉ trả lời trong phạm vi kiến thức đã mở khóa".
     */
    public List<TheoryChunkMatch> findTopK(float[] queryEmbedding, Collection<String> allowedChapterIds, int k) {
        if (allowedChapterIds == null || allowedChapterIds.isEmpty()) {
            return List.of();
        }
        String vectorLiteral = toVectorLiteral(queryEmbedding);

        return jdbcTemplate.query(con -> {
            PreparedStatement ps = con.prepareStatement(
                    """
                    SELECT chunk_id, theory_id, chapter_id, content, embedding <=> ?::vector AS distance
                    FROM theory_chunks
                    WHERE chapter_id = ANY (?)
                    ORDER BY distance ASC
                    LIMIT ?
                    """);
            ps.setString(1, vectorLiteral);
            Array chapterIdsArray = con.createArrayOf("varchar", allowedChapterIds.toArray());
            ps.setArray(2, chapterIdsArray);
            ps.setInt(3, k);
            return ps;
        }, (rs, rowNum) -> new TheoryChunkMatch(
                rs.getString("chunk_id"),
                rs.getString("theory_id"),
                rs.getString("chapter_id"),
                rs.getString("content"),
                rs.getDouble("distance")));
    }

    private String toVectorLiteral(float[] vector) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(vector[i]);
        }
        return builder.append(']').toString();
    }
}
