package org.scoutsdecanarias.ecatlim_backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.entity.ScoutGroup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class EmailService {

    private final JavaMailSender emailSender;

    private final EmailTemplateService emailTemplateService;

    @Value("${ecatlim.link}")
    private String webPageLink;

    public EmailService(JavaMailSender emailSender, EmailTemplateService emailTemplateService) {
        this.emailSender = emailSender;
        this.emailTemplateService = emailTemplateService;
    }

    public void sendEmail(String to, String subject, String body){
        MimeMessage message = this.createSimpleEmail(to, subject, body);

        assert message != null;
        emailSender.send(message);
    }

    public void sendWelcomeEmail(String to, String name, String email, String password) {
        String html = emailTemplateService.loadWelcomeEmailTemplate(name, email, password);
        MimeMessage message = this.createEmailWithHtml(to, "Bienvenida a la ECATLIM", html);

        assert message != null;
        emailSender.send(message);
    }

    public void sendRecoverPasswordEmail(String to, String token) {
        String resetLink = webPageLink + "/resetear-contraseña?token=" + token;
        String html = emailTemplateService.loadRecoverPasswordTemplate(resetLink);
        MimeMessage message = this.createEmailWithHtml(to, "Aula Virtual ECATLIM - Restablecer Contraseña", html);

        assert message != null;
        emailSender.send(message);
    }

    public void sendPendingUserCreatedEmail(String to, String name, String surname, String nif, ScoutGroup scoutGroup) {
        String html = emailTemplateService.loadPendingUserCreatedEmailTemplate(name, surname, nif, scoutGroup, to);
        MimeMessage message = this.createEmailWithHtml(to, "Aula Virtual ECATLIM - Solicitud de Alta Recibida", html);

        assert message != null;
        emailSender.send(message);
    }

    public void sendEventRecommendationEmail(String to, String name, String eventTitle, String eventLocation, String eventDate, List<String> missingBlocks) {
        String enrollLink = webPageLink + "/eventos/" + eventTitle;
        String html = emailTemplateService.loadEventNotificationTemplate(name, eventTitle, eventLocation, eventDate, missingBlocks, enrollLink);

        MimeMessage message = this.createEmailWithHtml(to, "Aula Virtual ECATLIM - Nueva Formación: " + eventTitle + " - ¡Completa tu etapa!", html);

        assert message != null;
        emailSender.send(message);
    }

    private MimeMessage createSimpleEmail(String to, String subject, String body) {
        try {
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);

            return mimeMessage;
        } catch (MessagingException e) {
            log.error(e.getMessage());
        }
        return null;
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
