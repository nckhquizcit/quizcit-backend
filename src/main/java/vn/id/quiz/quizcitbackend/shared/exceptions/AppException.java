package vn.id.quiz.quizcitbackend.shared.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;


/**
 * Exception này dùng cho các lỗi liên quan đến logic của app, dùng xử lý nội bộ
 * bên trong GlobalExceptionHandler trước khi quăng lỗi kết quả lỗi thực tế cho client
 */
@Getter
public class AppException extends RuntimeException {
    private final int status;
    public AppException(String message, HttpStatus httpStatus) {
        super(message);
        this.status=httpStatus.value();
    }
}
