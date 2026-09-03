package vn.id.quiz.quizcitbackend.shared.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DataIntegrityViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vn.id.quiz.quizcitbackend.shared.utils.ApiResponse;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Object>> handleAppException(AppException ex){
        return ResponseEntity
                .status(ex.getStatus())
                .body(new ApiResponse<>(ex.getStatus(), ex.getMessage(), ex.getData()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleUnreadableRequest(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), ErrorMessages.INVALID_REQUEST));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataConflict(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(HttpStatus.CONFLICT.value(), "Dữ liệu đã tồn tại hoặc đang được sử dụng"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), ErrorMessages.ACCESS_DENIED));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthentication(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), ErrorMessages.LOGIN_REQUIRED));
    }
    //Lỗi liên quan đến validation trong dữ liệu
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = error instanceof FieldError fieldError ? fieldError.getField() : error.getObjectName();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Dữ liệu không hợp lệ", errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneralException(Exception ex) {
        log.error("Lỗi hệ thống chưa được xử lý", ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Đã có lỗi hệ thống xảy ra!"));
    }
}
