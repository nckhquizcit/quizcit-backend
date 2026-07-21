package vn.id.quiz.quizcitbackend.shared.exceptions;

/**
 * Tập trung toàn bộ thông điệp lỗi dùng chung để tránh tình trạng cùng một loại
 * lỗi nhưng lại có nhiều nội dung khác nhau rải rác trong codebase. Mọi nơi cần
 * ném lỗi nên tham chiếu tới hằng số ở đây thay vì viết chuỗi trực tiếp.
 */
public final class ErrorMessages {

    private ErrorMessages() {
    }

    // Not found
    public static final String SUBJECT_NOT_FOUND = "Không tìm thấy học phần được yêu cầu";
    public static final String CHAPTER_NOT_FOUND = "Không tìm thấy chương được yêu cầu";
    public static final String QUIZ_NOT_FOUND = "Không tìm thấy bài kiểm tra được yêu cầu";
    public static final String QUESTION_NOT_FOUND = "Không tìm thấy câu hỏi được yêu cầu";
    public static final String ATTEMPT_NOT_FOUND = "Không tìm thấy lần làm bài được yêu cầu";
    public static final String USER_NOT_FOUND = "Không tìm thấy người dùng được yêu cầu";

    // Validation
    public static final String INVALID_ROLE = "Vai trò người dùng không hợp lệ";
    public static final String INVALID_PERMISSION = "Quyền không hợp lệ";

    // Authorization
    public static final String LOGIN_REQUIRED = "Bạn cần đăng nhập để thực hiện thao tác này.";
    public static final String ACCESS_DENIED = "Bạn không có quyền thực hiện thao tác này.";
}
