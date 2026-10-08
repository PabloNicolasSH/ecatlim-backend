package org.scoutsdecanarias.ecatlim_backend.shared.email;

import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.features.scout_group.ScoutGroup;
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

    public void sendWelcomeEmail(String to, String name, String password) {
        String html = emailTemplateService.loadWelcomeEmailTemplate(to, password, webPageLink);
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

    public void sendAttendanceCertificateEmail(String to, String name, int eventsCount, int blocksCount, String stageName, List<String> events) {
        String html = emailTemplateService.loadAttendanceCertificateTemplate(name, eventsCount, blocksCount, stageName, events);
        emailSenderService.sendEmail("Aula Virtual ECATLIM - Certificado de asistencia", html, to);
    }

    public void sendBlockCertificateEmail(String to, String name, List<String> blocks) {
        String progressLink = webPageLink + "/app/mi-ruta-formacion";
        String html = emailTemplateService.loadBlockCertificateTemplate(name, blocks, progressLink);
        emailSenderService.sendEmail("Aula Virtual ECATLIM - Certificado de bloque disponible", html, to);
    }

    public void sendStageCertificateEmail(String to, String name, String stageName, List<EmailAttachment> attachments) {
        String html = emailTemplateService.loadStageCertificateTemplate(name, stageName);
        emailSenderService.sendEmailWithAttachments("Aula Virtual ECATLIM - ¡Felicidades por completar la etapa " + stageName + "!", html, attachments, to);
    }

    public void sendWeeklyReminderEmail(String to, String name, List<String> pendingActivities, List<String> otherPending) {
        String html = emailTemplateService.loadWeeklyReminderTemplate(name, pendingActivities, otherPending, webPageLink + "/app/home", webPageLink);
        emailSenderService.sendEmail("Aula Virtual ECATLIM - Tienes cosas pendientes", html, to);
    }
}
