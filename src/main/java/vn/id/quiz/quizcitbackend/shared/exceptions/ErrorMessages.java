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
    public static final String ACCOUNT_NOT_FOUND = "Không tìm thấy tài khoản";
    public static final String COURSE_ENROLLMENT_NOT_FOUND = "Bạn chưa đăng ký môn học này";

    // Validation
    public static final String INVALID_ROLE = "Vai trò người dùng không hợp lệ";
    public static final String INVALID_PERMISSION = "Quyền không hợp lệ";
    public static final String INVALID_REQUEST = "Yêu cầu không hợp lệ";
    public static final String INVALID_ACCOUNT_STATUS = "Trạng thái tài khoản không hợp lệ";
    public static final String INVALID_CREDENTIALS = "Thông tin đăng nhập không chính xác";
    public static final String INVALID_CURRENT_PASSWORD = "Mật khẩu hiện tại không chính xác";
    public static final String INVALID_OTP = "Mã xác thực không đúng";
    public static final String INVALID_OR_EXPIRED_OTP = "Mã xác thực đã hết hạn hoặc không hợp lệ";
    public static final String OTP_ATTEMPTS_EXCEEDED = "Mã xác thực đã bị vô hiệu do nhập sai quá 3 lần";
    public static final String OTP_RESEND_TOO_SOON = "Vui lòng chờ trước khi yêu cầu gửi lại mã xác thực";
    public static final String EMAIL_EXISTS = "Email đã được sử dụng";
    public static final String USERNAME_EXISTS = "Tên đăng nhập đã tồn tại";
    public static final String ACCOUNT_UNVERIFIED = "Tài khoản chưa xác thực email";
    public static final String ACCOUNT_BANNED = "Tài khoản đã bị khóa";
    public static final String ACCOUNT_VERIFIED = "Tài khoản đã được xác thực";
    public static final String ROLE_NOT_CONFIGURED = "Vai trò chưa được cấu hình trong hệ thống";
    public static final String SELF_ADMIN_CHANGE_DENIED =
            "Bạn không thể khóa, xóa hoặc thay đổi vai trò của tài khoản đang đăng nhập";
    public static final String REFRESH_TOKEN_INVALID = "Refresh token đã hết hạn hoặc không hợp lệ";
    public static final String USERNAME_REQUIRED = "Tên đăng nhập không được để trống";
    public static final String USERNAME_LENGTH = "Tên đăng nhập phải từ 3 đến 50 ký tự";
    public static final String USERNAME_INVALID =
            "Tên đăng nhập không được chứa khoảng trắng, dấu tiếng Việt hoặc ký tự đặc biệt";
    public static final String IDENTIFIER_REQUIRED = "Tên đăng nhập hoặc email không được để trống";
    public static final String IDENTIFIER_LENGTH = "Tên đăng nhập hoặc email không được vượt quá 100 ký tự";
    public static final String COURSE_CODE_REQUIRED = "Mã học phần không được để trống";
    public static final String COURSE_CODE_LENGTH = "Mã học phần phải từ 2 đến 20 ký tự";
    public static final String COURSE_CODE_INVALID =
            "Mã học phần không được chứa khoảng trắng, dấu tiếng Việt hoặc ký tự đặc biệt";
    public static final String PASSWORD_REQUIRED = "Mật khẩu không được để trống";
    public static final String PASSWORD_LENGTH = "Mật khẩu phải từ 6 đến 255 ký tự";
    public static final String DISPLAY_NAME_REQUIRED = "Tên hiển thị không được để trống";
    public static final String DISPLAY_NAME_LENGTH = "Tên hiển thị không được vượt quá 100 ký tự";
    public static final String EMAIL_REQUIRED = "Email không được để trống";
    public static final String EMAIL_INVALID = "Email không hợp lệ";
    public static final String EMAIL_LENGTH = "Email không được vượt quá 100 ký tự";
    public static final String VERIFIED_EMAIL_CHANGE_REQUIRED =
            "Vui lòng sử dụng luồng xác thực để thay đổi email";
    public static final String RATE_LIMIT_EXCEEDED =
            "Bạn đã gửi quá nhiều yêu cầu. Vui lòng thử lại sau";
    public static final String EMAIL_SEND_FAILED = "Không thể gửi email xác thực";
    public static final String PASSWORD_RESET_EMAIL_SEND_FAILED = "Không thể gửi email đặt lại mật khẩu";
    public static final String COURSE_ALREADY_ENROLLED = "Bạn đã đăng ký môn học này";
    public static final String COURSE_ENROLLMENT_REQUIRED =
            "Bạn cần đăng ký môn học trước khi truy cập nội dung này";

    // Authorization
    public static final String LOGIN_REQUIRED = "Bạn cần đăng nhập để thực hiện thao tác này.";
    public static final String ACCESS_DENIED = "Bạn không có quyền thực hiện thao tác này.";
}
