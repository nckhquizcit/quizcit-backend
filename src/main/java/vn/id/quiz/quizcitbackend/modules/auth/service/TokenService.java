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

import org.springframework.http.HttpStatus;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

@Service
public class TokenService {
    private static final String TOKEN_TYPE = "Bearer";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String JWT_HEADER = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    private static final String VERIFY_PURPOSE = "email_verify";
    private static final String RESET_PURPOSE = "password_reset";
    private static final String RESET_CONFIRMED_PURPOSE = "password_reset_confirmed";
    private static final Pattern EXPIRATION_PATTERN = Pattern.compile("\"exp\":(\\d+)");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("\"email\":\"((?:\\\\.|[^\"\\\\])*)\"");
    private static final Pattern OTP_HASH_PATTERN = Pattern.compile("\"otpHash\":\"((?:\\\\.|[^\"\\\\])*)\"");
    private static final Pattern PURPOSE_PATTERN = Pattern.compile("\"purpose\":\"((?:\\\\.|[^\"\\\\])*)\"");

    private final byte[] secret;
    private final byte[] verificationSecret;
    private final long expirationSeconds;
    private final long otpExpirationSeconds;

    public TokenService(
            @Value("${quizcit.auth.token.secret}") String secret,
            @Value("${quizcit.auth.token.expiration-seconds:86400}") long expirationSeconds,
            @Value("${quizcit.auth.otp.token-secret}") String verificationSecret,
            @Value("${quizcit.auth.otp.expiration-seconds:300}") long otpExpirationSeconds
    ) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("quizcit.auth.token.secret chưa được cấu hình");
        }
        if (verificationSecret == null || verificationSecret.isBlank()) {
            throw new IllegalStateException("quizcit.auth.otp.token-secret chưa được cấu hình");
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.verificationSecret = verificationSecret.getBytes(StandardCharsets.UTF_8);
        this.expirationSeconds = expirationSeconds;
        this.otpExpirationSeconds = otpExpirationSeconds;
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
        String expectedSignature = base64Url(sign(unsignedToken, secret));
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

    public String generateVerificationToken(String email, String otpHash) {
        return generateOtpToken(VERIFY_PURPOSE, email, otpHash);
    }

    public String generatePasswordResetToken(String email, String otpHash) {
        return generateOtpToken(RESET_PURPOSE, email, otpHash);
    }

    public String generatePasswordResetConfirmedToken(String email) {
        return generateOtpToken(RESET_CONFIRMED_PURPOSE, email, null);
    }

    private String generateOtpToken(String purpose, String email, String otpHash) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(otpExpirationSeconds);

        StringBuilder payload = new StringBuilder("{")
                .append("\"purpose\":").append(quote(purpose)).append(",")
                .append("\"email\":").append(quote(email)).append(",");
        if (otpHash != null) {
            payload.append("\"otpHash\":").append(quote(otpHash)).append(",");
        }
        payload.append("\"iat\":").append(issuedAt.getEpochSecond()).append(",")
                .append("\"exp\":").append(expiresAt.getEpochSecond())
                .append("}");

        String encodedHeader = base64Url(JWT_HEADER.getBytes(StandardCharsets.UTF_8));
        String encodedPayload = base64Url(payload.toString().getBytes(StandardCharsets.UTF_8));
        String unsignedToken = encodedHeader + "." + encodedPayload;
        String signature = base64Url(sign(unsignedToken, verificationSecret));
        return unsignedToken + "." + signature;
    }

    public VerificationClaims parseVerificationToken(String token) {
        return parseOtpToken(token, VERIFY_PURPOSE, true);
    }

    public VerificationClaims parsePasswordResetToken(String token) {
        return parseOtpToken(token, RESET_PURPOSE, true);
    }

    public VerificationClaims parsePasswordResetConfirmedToken(String token) {
        return parseOtpToken(token, RESET_CONFIRMED_PURPOSE, false);
    }

    private VerificationClaims parseOtpToken(String token, String expectedPurpose, boolean requireOtpHash) {
        AppException invalid = new AppException(
                "Mã xác thực đã hết hạn hoặc không hợp lệ", HttpStatus.BAD_REQUEST);

        if (token == null || token.isBlank()) {
            throw invalid;
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw invalid;
        }

        String unsignedToken = parts[0] + "." + parts[1];
        String expectedSignature = base64Url(sign(unsignedToken, verificationSecret));
        if (!MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                parts[2].getBytes(StandardCharsets.UTF_8)
        )) {
            throw invalid;
        }

        String payload;
        try {
            payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            throw invalid;
        }

        if (!expectedPurpose.equals(extract(PURPOSE_PATTERN, payload))) {
            throw invalid;
        }
        boolean expired = extractExpiration(parts[1])
                .map(expiration -> expiration < Instant.now().getEpochSecond())
                .orElse(true);
        if (expired) {
            throw invalid;
        }

        String email = unescape(extract(EMAIL_PATTERN, payload));
        String otpHash = unescape(extract(OTP_HASH_PATTERN, payload));
        if (email == null || (requireOtpHash && otpHash == null)) {
            throw invalid;
        }
        return new VerificationClaims(email, otpHash);
    }

    private String extract(Pattern pattern, String payload) {
        Matcher matcher = pattern.matcher(payload);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String unescape(String value) {
        if (value == null) {
            return null;
        }
        return value.replace("\\\"", "\"").replace("\\\\", "\\");
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
        return sign(content, secret);
    }

    private byte[] sign(String content, byte[] key) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(key, HMAC_ALGORITHM));
            return mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể ký token", ex);
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
