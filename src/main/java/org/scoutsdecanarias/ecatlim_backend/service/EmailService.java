package org.scoutsdecanarias.ecatlim_backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    private final JavaMailSender emailSender;

    private final EmailTemplateService emailTemplateService;

    @Value("${spring.mail.username}")
    private String from;

    public EmailService(JavaMailSender emailSender, EmailTemplateService emailTemplateService) {
        this.emailSender = emailSender;
        this.emailTemplateService = emailTemplateService;
    }

    public void sendWelcomeEmail(String to, String name, String email, String password) {
        String html = emailTemplateService.loadWelcomeEmailTemplate(name, email, password);
        MimeMessage message = this.createEmailWithHtml(to, "Bienvenida a la ECATLIM", html);

        assert message != null;
        emailSender.send(message);
    }

    private MimeMessage createEmailWithHtml(String to, String subject, String htmlContent) {
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
