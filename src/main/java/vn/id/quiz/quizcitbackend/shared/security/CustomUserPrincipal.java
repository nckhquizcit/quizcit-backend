package vn.id.quiz.quizcitbackend.shared.security;

import java.io.Serializable;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Principal lưu trong {@code Authentication} sau khi filter JWT xác
 * thực thành công. Cho phép controller/service inject thông tin user
 * hiện tại qua {@code @AuthenticationPrincipal CustomUserPrincipal}.
 */
@Getter
@RequiredArgsConstructor
public class CustomUserPrincipal implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Tiền tố authority cho {@code hasRole(...)} trong Spring Security.
     * Nếu role là {@code ADMINISTRATOR}, principal sẽ có authority
     * {@code ROLE_ADMINISTRATOR}.
     */
    private static final String ROLE_PREFIX = "ROLE_";

    private final String userId;
    private final String username;
    private final String email;
    private final String role;
    private final String status;

    public String getAuthority() {
        return ROLE_PREFIX + (role == null ? "ANONYMOUS" : role);
    }

    public boolean isAdministrator() {
        return "ADMINISTRATOR".equalsIgnoreCase(role);
    }

    public boolean isBanned() {
        return "BANNED".equalsIgnoreCase(status);
    }
}
