package vn.id.quiz.quizcitbackend.shared.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import lombok.RequiredArgsConstructor;
import vn.id.quiz.quizcitbackend.shared.security.handlers.JsonAccessDeniedHandler;
import vn.id.quiz.quizcitbackend.shared.security.handlers.JsonAuthEntryPoint;

/**
 * Cấu hình Spring Security cho toàn bộ hệ thống.
 *
 * <p><b>Fix Bug 4 (Phase 1 - P1):</b> Trước đây project chưa kích hoạt
 * Spring Security nên mọi request đều được phép (kể cả thiếu token hoặc
 * token của USER gọi vào API admin). Sau khi fix:
 * <ul>
 *   <li>Bật {@code @EnableMethodSecurity} để dùng {@code @PreAuthorize}
 *       trên từng controller method.</li>
 *   <li>Cấu hình stateless session, thêm {@link JwtAuthFilter} trước
 *       {@link UsernamePasswordAuthenticationFilter}.</li>
 *   <li>Phân quyền theo pattern URL:
 *       <ul>
 *         <li>{@code /api/auth/**} - public</li>
 *         <li>{@code GET /api/subjects}, {@code GET /api/chapters},
 *             {@code GET /api/theories}, {@code GET /api/quizzes/**},
 *             {@code GET /api/questions/**} - cho phép USER + ADMIN</li>
 *         <li>POST/PUT/DELETE trên tài nguyên quản trị - chỉ ADMINISTRATOR</li>
 *       </ul>
 *   </li>
 *   <li>Trả về {@code 401/403} qua {@link JsonAuthEntryPoint} và
 *       {@link JsonAccessDeniedHandler} thay vì trang HTML mặc định.</li>
 * </ul>
 * </p>
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final JsonAuthEntryPoint authEntryPoint;
    private final JsonAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CorsConfigurationSource corsConfigurationSource
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint(authEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - auth
                        .requestMatchers("/api/auth/**").permitAll()

                        // Public health check (nếu có)
                        .requestMatchers("/actuator/health", "/error").permitAll()

                        // READ endpoints - cả USER + ADMINISTRATOR
                        // Chỉ danh mục và chi tiết môn được xem trước khi đăng ký.
                        .requestMatchers(HttpMethod.GET, "/api/uploads/**", "/uploads/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/subjects", "/api/subjects/*", "/api/v1/subjects", "/api/v1/subjects/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/chapters/**", "/api/v1/chapters/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/theories/**", "/api/v1/theories/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/quizzes/**", "/api/v1/quizzes/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/questions/**", "/api/v1/questions/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/skills/**", "/api/v1/skills/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/content/**", "/api/v1/content/**").authenticated()

                        // Các endpoint còn lại (bao gồm admin) - yêu cầu ADMINISTRATOR
                        .requestMatchers("/api/admin/**", "/api/v1/admin/**").hasRole("ADMINISTRATOR")
                        .requestMatchers("/api/users/**", "/api/v1/users/**").hasAnyRole("ADMINISTRATOR", "USER")
                        .requestMatchers("/api/pages/**", "/api/v1/pages/**").hasAnyRole("ADMINISTRATOR", "USER")
                        .requestMatchers("/api/progress/**", "/api/v1/progress/**").hasAnyRole("ADMINISTRATOR", "USER")

                        // Mặc định: yêu cầu đã authenticated cho các route còn lại
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Chuẩn BCrypt với strength mặc định.
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
