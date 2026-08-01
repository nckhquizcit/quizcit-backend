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
        message.setSubject("Ma xac thuc tai khoan Quizcit");
        message.setText(
                "Ma xac thuc cua ban la: " + code + "\n\n"
                        + "Ma co hieu luc trong 5 phut. Vui long khong chia se ma nay voi bat ky ai.\n\n"
                        + "Neu ban khong yeu cau dang ky, hay bo qua email nay."
        );
        try {
            mailSender.send(message);
        } catch (MailException ex) {
            throw new AppException("Khong the gui email xac thuc", HttpStatus.INTERNAL_SERVER_ERROR);
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
        message.setSubject("Ma dat lai mat khau Quizcit");
        message.setText(
                "Ma dat lai mat khau cua ban la: " + code + "\n\n"
                        + "Ma co hieu luc trong 5 phut. Vui long khong chia se ma nay voi bat ky ai.\n\n"
                        + "Neu ban khong yeu cau dat lai mat khau, hay bo qua email nay."
        );
        try {
            mailSender.send(message);
        } catch (MailException ex) {
            throw new AppException("Khong the gui email dat lai mat khau", HttpStatus.INTERNAL_SERVER_ERROR);
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
