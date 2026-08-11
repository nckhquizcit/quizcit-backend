package vn.id.quiz.quizcitbackend.modules.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import vn.id.quiz.quizcitbackend.shared.exceptions.AppException;
import vn.id.quiz.quizcitbackend.shared.exceptions.ErrorMessages;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${quizcit.auth.otp.from-address}") String fromAddress
    ) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    public void sendOtp(String toEmail, String code) {
        if (!isSmtpConfigured()) {
            log.warn("SMTP mail credentials are not configured. QUIZCIT LOCAL OTP for {}: {}", toEmail, code);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Mã xác thực tài khoản QuizCIT");
        message.setText(
                "Mã xác thực của bạn là: " + code + "\n\n"
                        + "Mã có hiệu lực trong 5 phút. Vui lòng không chia sẻ mã này với bất kỳ ai.\n\n"
                        + "Nếu bạn không yêu cầu thao tác này, hãy bỏ qua email."
        );
        try {
            mailSender.send(message);
        } catch (MailException ex) {
            throw new AppException(ErrorMessages.EMAIL_SEND_FAILED, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void sendPasswordResetOtp(String toEmail, String code) {
        if (!isSmtpConfigured()) {
            log.warn("SMTP mail credentials are not configured. QUIZCIT LOCAL PASSWORD RESET OTP for {}: {}", toEmail, code);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Mã đặt lại mật khẩu QuizCIT");
        message.setText(
                "Mã đặt lại mật khẩu của bạn là: " + code + "\n\n"
                        + "Mã có hiệu lực trong 5 phút. Vui lòng không chia sẻ mã này với bất kỳ ai.\n\n"
                        + "Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này."
        );
        try {
            mailSender.send(message);
        } catch (MailException ex) {
            throw new AppException(ErrorMessages.PASSWORD_RESET_EMAIL_SEND_FAILED, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean isSmtpConfigured() {
        if (mailSender == null) {
            return false;
        }
        if (mailSender instanceof JavaMailSenderImpl senderImpl) {
            return StringUtils.hasText(senderImpl.getUsername())
                    && StringUtils.hasText(senderImpl.getPassword());
        }
        return true;
    }
}
