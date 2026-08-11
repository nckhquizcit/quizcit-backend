package vn.id.quiz.quizcitbackend.modules.auth.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailServiceTest {
    @Test
    void sendOtpShouldSkipMailSenderWhenSmtpCredentialsAreMissing() {
        RecordingMailSender mailSender = new RecordingMailSender();
        EmailService emailService = new EmailService(mailSender, "no-reply@quizcit.vn");

        emailService.sendOtp("student@example.com", "123456");

        assertThat(mailSender.sendCount).isZero();
    }

    @Test
    void sendOtpShouldSendEmailWhenSmtpCredentialsAreConfigured() {
        RecordingMailSender mailSender = new RecordingMailSender();
        mailSender.setUsername("quizcit@example.com");
        mailSender.setPassword("app-password");
        EmailService emailService = new EmailService(mailSender, "quizcit@example.com");

        emailService.sendOtp("student@example.com", "123456");

        assertThat(mailSender.sendCount).isEqualTo(1);
        assertThat(mailSender.lastMessage.getFrom()).isEqualTo("quizcit@example.com");
        assertThat(mailSender.lastMessage.getTo()).containsExactly("student@example.com");
        assertThat(mailSender.lastMessage.getSubject()).isEqualTo("Mã xác thực tài khoản QuizCIT");
        assertThat(mailSender.lastMessage.getText()).contains("123456");
    }

    @Test
    void sendOtpShouldReturnAppExceptionWhenConfiguredMailSenderFails() {
        RecordingMailSender mailSender = new RecordingMailSender();
        mailSender.setUsername("quizcit@example.com");
        mailSender.setPassword("app-password");
        mailSender.failSend = true;
        EmailService emailService = new EmailService(mailSender, "quizcit@example.com");

        assertThatThrownBy(() -> emailService.sendOtp("student@example.com", "123456"))
                .isInstanceOf(AppException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    private static final class RecordingMailSender extends JavaMailSenderImpl {
        private SimpleMailMessage lastMessage;
        private int sendCount;
        private boolean failSend;

        @Override
        public void send(SimpleMailMessage simpleMessage) {
            if (failSend) {
                throw new MailSendException("SMTP failed");
            }
            this.lastMessage = simpleMessage;
            this.sendCount++;
        }
    }
}
