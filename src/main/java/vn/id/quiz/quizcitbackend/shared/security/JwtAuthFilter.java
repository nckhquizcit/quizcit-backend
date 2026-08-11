package vn.id.quiz.quizcitbackend.shared.security;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import vn.id.quiz.quizcitbackend.modules.auth.service.TokenService;
import vn.id.quiz.quizcitbackend.modules.auth.repository.AuthRepository;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserStatus;

/**
 * Filter đọc JWT trên header {@code Authorization: Bearer <token>},
 * verify chữ ký và set {@code Authentication} cho
 * {@link SecurityContextHolder}.
 *
 * <p>Sau khi parse thành công, principal là {@link CustomUserPrincipal}
 * chứa userId, username, email, role, status. Mọi controller muốn lấy
 * thông tin người dùng hiện tại có thể inject qua
 * {@code @AuthenticationPrincipal CustomUserPrincipal}.</p>
 *
 * <p>Filter chỉ chạy một lần cho mỗi request và bỏ qua khi không có
 * token hoặc token không hợp lệ - các quy tắc permit/deny sẽ do
 * {@link SecurityConfig} quyết định.</p>
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenService tokenService;
    private final AuthRepository authRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        Optional<String> token = extractToken(request);
        if (token.isPresent() && tokenService.isValid(token.get())) {
            // TokenService sinh token nội bộ (không phải JWT lib), nhưng cấu
            // trúc payload đã được dựng với các trường userId/role/status.
            // Ta tự decode payload để lấy các thông tin an toàn.
            TokenPayload payload = TokenPayload.from(token.get());

            var currentUser = authRepository.findUserById(payload.userId());
            if (currentUser.isEmpty()
                    || currentUser.get().getStatus() != UserStatus.VERIFIED
                    || payload.tokenVersion() == null
                    || !payload.tokenVersion().equals(currentUser.get().getTokenVersion())) {
                filterChain.doFilter(request, response);
                return;
            }

            CustomUserPrincipal principal = new CustomUserPrincipal(
                    payload.userId(),
                    payload.username(),
                    payload.email(),
                    payload.role(),
                    payload.status());

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    List.of(new SimpleGrantedAuthority(principal.getAuthority())));
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }

    private Optional<String> extractToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return Optional.empty();
        }
        String value = header.substring(BEARER_PREFIX.length()).trim();
        return value.isEmpty() ? Optional.empty() : Optional.of(value);
    }
}
