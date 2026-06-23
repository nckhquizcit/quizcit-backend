package vn.id.quiz.quizcitbackend.modules.user.presentation.model;

/**
 * Các vai trò có trong hệ thống QuizCIT.
 *
 * <p>Hệ thống chỉ hỗ trợ 2 vai trò: {@code USER} (người dùng phổ thông) và
 * {@code ADMINISTRATOR} (quản trị viên). Tên enum phải trùng với giá trị
 * lưu trong cột {@code roles.name} trong database.</p>
 *
 * <p>Vấn đề trước đây: DB tồn tại role tên {@code Student} (chuỗi thường)
 * nhưng enum Java chỉ có {@code USER} và {@code ADMINISTRATOR}. Khi Hibernate
 * đọc danh sách user, mapping enum bị vỡ, dẫn tới lỗi 500 cho
 * {@code GET /api/users}. File migration {@code V2__sync_roles.sql} sẽ
 * chuẩn hoá các giá trị cũ về {@code USER}.</p>
 */
public enum UserRole {
    USER,
    ADMINISTRATOR
}
