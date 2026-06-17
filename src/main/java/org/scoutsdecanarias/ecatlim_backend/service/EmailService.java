package org.scoutsdecanarias.ecatlim_backend.service;

import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.entity.ScoutGroup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class EmailService {

    private final EmailTemplateService emailTemplateService;
    private final EmailSenderService emailSenderService;

    @Value("${ecatlim.link}")
    private String webPageLink;

    public EmailService(EmailSenderService emailSenderService, EmailTemplateService emailTemplateService) {
        this.emailTemplateService = emailTemplateService;
        this.emailSenderService = emailSenderService;
    }

    public void sendWelcomeEmail(String to, String name, String email, String password) {
        String html = emailTemplateService.loadWelcomeEmailTemplate(name, email, password);
        emailSenderService.sendEmail("Bienvenida a la ECATLIM", html, to);
    }

    public void sendRecoverPasswordEmail(String to, String token) {
        String resetLink = webPageLink + "/resetear-contraseña?token=" + token;
        String html = emailTemplateService.loadRecoverPasswordTemplate(resetLink);
        emailSenderService.sendEmail("Aula Virtual ECATLIM - Restablecer Contraseña", html, to);
    }

    public void sendPendingUserCreatedEmail(String to, String name, String surname, String nif, ScoutGroup scoutGroup) {
        String html = emailTemplateService.loadPendingUserCreatedEmailTemplate(name, surname, nif, scoutGroup, to);
        emailSenderService.sendEmail("Aula Virtual ECATLIM - Solicitud de Alta Recibida", html, to);
    }

    public void sendEventRecommendationEmail(String to, String name, String eventTitle, String eventLocation, String eventDate, List<String> missingBlocks, List<String> allEventBlocks) {
        String enrollLink = webPageLink + "/eventos/" + eventTitle;
        String html = emailTemplateService.loadEventNotificationTemplate(name, eventTitle, eventLocation, eventDate, missingBlocks, allEventBlocks, enrollLink);
        emailSenderService.sendEmail("Aula VIrtual ECATLIM - Nueva Formación: " + eventTitle + " - ¡Completa tu etapa!", html, to);
    }
}
