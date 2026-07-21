package vn.id.quiz.quizcitbackend.shared.security;

import org.springframework.http.HttpStatus;

import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;
import vn.id.quiz.quizcitbackend.shared.exceptions.ErrorMessages;

/**
 * Tập trung logic phân quyền dùng chung giữa các controller: xác định user thực
 * sự được thao tác dựa trên principal hiện tại và tham số userId yêu cầu. Trước
 * đây logic này bị lặp lại y hệt ở nhiều controller nên được gom về một nơi.
 */
public final class PrincipalAccess {

    private PrincipalAccess() {
    }

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public static void requirePrincipal(CustomUserPrincipal principal) {
        if (principal == null || isBlank(principal.getUserId())) {
            throw new AppException(ErrorMessages.LOGIN_REQUIRED, HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Trả về userId mà lời gọi được phép thao tác. Admin có thể chỉ định bất kỳ
     * userId nào; user thường chỉ được thao tác trên chính mình.
     */
    public static String resolveUserId(CustomUserPrincipal principal, String requestedUserId) {
        requirePrincipal(principal);

        if (principal.isAdministrator()) {
            return isBlank(requestedUserId) ? principal.getUserId() : requestedUserId;
        }

        if (!isBlank(requestedUserId) && !principal.getUserId().equals(requestedUserId)) {
            throw new AppException(ErrorMessages.ACCESS_DENIED, HttpStatus.FORBIDDEN);
        }

        return principal.getUserId();
    }
}
