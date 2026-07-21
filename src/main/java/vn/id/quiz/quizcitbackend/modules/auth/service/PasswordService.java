package vn.id.quiz.quizcitbackend.modules.auth.service;

import org.springframework.stereotype.Service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

@Service
public class PasswordService {
    private static final String HASH_PREFIX = "pbkdf2";
    private static final String HASH_SEPARATOR = "$";
    private static final String HASH_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 185_000;
    private static final int SALT_LENGTH_BYTES = 16;
    private static final int KEY_LENGTH_BITS = 256;

    private final SecureRandom secureRandom = new SecureRandom();

    public String hash(String rawPassword) {
        validateRawPassword(rawPassword);

        byte[] salt = new byte[SALT_LENGTH_BYTES];
        secureRandom.nextBytes(salt);
        byte[] hash = pbkdf2(rawPassword.toCharArray(), salt, ITERATIONS);

        return String.join(
                HASH_SEPARATOR,
                HASH_PREFIX,
                String.valueOf(ITERATIONS),
                Base64.getEncoder().encodeToString(salt),
                Base64.getEncoder().encodeToString(hash)
        );
    }

    public boolean matches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null || storedPassword.isBlank()) {
            return false;
        }

        if (!isHashed(storedPassword)) {
            return constantTimeEquals(rawPassword.getBytes(StandardCharsets.UTF_8), storedPassword.getBytes(StandardCharsets.UTF_8));
        }

        String[] parts = storedPassword.split("\\$");
        if (parts.length != 4) {
            return false;
        }

        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[3]);
            byte[] actualHash = pbkdf2(rawPassword.toCharArray(), salt, iterations);
            return constantTimeEquals(actualHash, expectedHash);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public boolean isHashed(String storedPassword) {
        return storedPassword != null && storedPassword.startsWith(HASH_PREFIX + HASH_SEPARATOR);
    }

    private byte[] pbkdf2(char[] password, byte[] salt, int iterations) {
        try {
            KeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_LENGTH_BITS);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(HASH_ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể mã hóa mật khẩu", ex);
        }
    }

    private boolean constantTimeEquals(byte[] actual, byte[] expected) {
        return MessageDigest.isEqual(actual, expected);
    }

    private void validateRawPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống");
        }
    }
}
