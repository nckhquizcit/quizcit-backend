package vn.id.quiz.quizcitbackend.modules.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.User;
import vn.id.quiz.quizcitbackend.modules.user.presentation.model.UserRole;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TokenService {
    private static final String TOKEN_TYPE = "Bearer";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String JWT_HEADER = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    private static final Pattern EXPIRATION_PATTERN = Pattern.compile("\"exp\":(\\d+)");

    private final byte[] secret;
    private final long expirationSeconds;

    public TokenService(
            @Value("${quizcit.auth.token.secret}") String secret,
            @Value("${quizcit.auth.token.expiration-seconds:86400}") long expirationSeconds
    ) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("quizcit.auth.token.secret chua duoc cau hinh");
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationSeconds = expirationSeconds;
    }

    public TokenDetails generate(User user, UserRole role) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(expirationSeconds);

        String encodedHeader = base64Url(JWT_HEADER.getBytes(StandardCharsets.UTF_8));
        String encodedPayload = base64Url(buildPayload(user, role, issuedAt, expiresAt).getBytes(StandardCharsets.UTF_8));
        String unsignedToken = encodedHeader + "." + encodedPayload;
        String signature = base64Url(sign(unsignedToken));

        return new TokenDetails(
                unsignedToken + "." + signature,
                TOKEN_TYPE,
                ZonedDateTime.ofInstant(issuedAt, ZoneId.systemDefault()),
                ZonedDateTime.ofInstant(expiresAt, ZoneId.systemDefault())
        );
    }

    public boolean isValid(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return false;
        }

        String unsignedToken = parts[0] + "." + parts[1];
        String expectedSignature = base64Url(sign(unsignedToken));
        if (!MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                parts[2].getBytes(StandardCharsets.UTF_8)
        )) {
            return false;
        }

        return extractExpiration(parts[1])
                .map(expiration -> expiration >= Instant.now().getEpochSecond())
                .orElse(false);
    }

    private String buildPayload(User user, UserRole role, Instant issuedAt, Instant expiresAt) {
        String roleName = role == null ? null : role.name();
        String status = user.getStatus() == null ? null : user.getStatus().name();

        return "{"
                + "\"sub\":" + quote(user.getUserId()) + ","
                + "\"username\":" + quote(user.getUsername()) + ","
                + "\"email\":" + quote(user.getEmail()) + ","
                + "\"role\":" + quote(roleName) + ","
                + "\"status\":" + quote(status) + ","
                + "\"iat\":" + issuedAt.getEpochSecond() + ","
                + "\"exp\":" + expiresAt.getEpochSecond()
                + "}";
    }

    private byte[] sign(String content) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
            return mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("Khong the ky token", ex);
        }
    }

    private java.util.Optional<Long> extractExpiration(String encodedPayload) {
        try {
            String payload = new String(Base64.getUrlDecoder().decode(encodedPayload), StandardCharsets.UTF_8);
            Matcher matcher = EXPIRATION_PATTERN.matcher(payload);
            if (!matcher.find()) {
                return java.util.Optional.empty();
            }
            return java.util.Optional.of(Long.parseLong(matcher.group(1)));
        } catch (IllegalArgumentException ex) {
            return java.util.Optional.empty();
        }
    }

    private String base64Url(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private String quote(String value) {
        if (value == null) {
            return "null";
        }
        return "\"" + value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                + "\"";
    }
}
