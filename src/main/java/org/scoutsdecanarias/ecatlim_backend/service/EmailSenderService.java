package org.scoutsdecanarias.ecatlim_backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailSenderService {
    private final JavaMailSender emailSender;

    public EmailSenderService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Async("emailExecutor")
    public void sendEmail(String subject, String html, String... to) {
        MimeMessage message = this.createEmailWithHtml(html, subject, to);
        if (message != null) {
            emailSender.send(message);
        }
    }

    private MimeMessage createEmailWithHtml(String htmlContent, String subject, String... to) {
        try {
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(subject);

            mimeMessageHelper.setText(htmlContent, true);

            return mimeMessage;
        } catch (MessagingException e) {
            log.error(e.getMessage());
        }
        return null;
    }
}
