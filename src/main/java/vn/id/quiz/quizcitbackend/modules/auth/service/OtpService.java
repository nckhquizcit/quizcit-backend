package vn.id.quiz.quizcitbackend.modules.auth.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class OtpService {
    private final SecureRandom random = new SecureRandom();

    public String generateCode() {
        return String.format("%06d", random.nextInt(1_000_000));
    }
}
