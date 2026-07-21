package vn.id.quiz.quizcitbackend.shared.security;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tiện ích parse payload từ token do {@code TokenService} sinh ra.
 * Token nội bộ không dùng thư viện JWT nên ta parse thủ công bằng
 * regex các trường JSON trong payload (sub, username, email, role,
 * status, iat, exp).
 */
public record TokenPayload(
        String userId,
        String username,
        String email,
        String role,
        String status) {

    private static final Pattern FIELD_PATTERN = Pattern.compile(
            "\"([a-zA-Z_]+)\"\\s*:\\s*\"([^\"]*)\"");

    public static TokenPayload from(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Token không hợp lệ");
        }
        String decoded;
        try {
            decoded = new String(
                    Base64.getUrlDecoder().decode(parts[1]),
                    StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Payload token không hợp lệ", ex);
        }

        String userId = null;
        String username = null;
        String email = null;
        String role = null;
        String status = null;

        Matcher matcher = FIELD_PATTERN.matcher(decoded);
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2);
            switch (key) {
                case "sub" -> userId = value;
                case "username" -> username = value;
                case "email" -> email = value;
                case "role" -> role = value;
                case "status" -> status = value;
                default -> { /* ignore */ }
            }
        }

        return new TokenPayload(userId, username, email, role, status);
    }
}
