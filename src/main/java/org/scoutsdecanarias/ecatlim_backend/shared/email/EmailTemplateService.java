package org.scoutsdecanarias.ecatlim_backend.shared.email;

import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.features.scout_group.ScoutGroup;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;

@Slf4j
@Service
public class EmailTemplateService {

    private final TemplateEngine templateEngine;

    public EmailTemplateService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String loadWelcomeEmailTemplate(String email, String password, String webPageLink) {
        Context context = new Context();
        context.setVariable("email", email);
        context.setVariable("password", password);
        context.setVariable("webPageLink", webPageLink);

        return templateEngine.process("welcome_email.html", context);
    }

    public String loadRecoverPasswordTemplate(String resetLink) {
        Context context = new Context();
        context.setVariable("resetLink", resetLink);

        return templateEngine.process("recover_password_email.html", context);
    }

    public String loadPendingUserCreatedEmailTemplate(String name, String surname, String nif, ScoutGroup scoutGroup, String email) {
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("surname", surname);
        context.setVariable("nif", nif);
        context.setVariable("scoutGroup", generateScoutGroupString(scoutGroup));
        context.setVariable("email", email);

        return templateEngine.process("pending_user_request_email.html", context);
    }

    public String loadEventNotificationTemplate(String name, String eventTitle, String eventLocation, String eventDate, List<String> missingBlocks, List<String> allEventBlocks, String enrollLink) {
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("eventTitle", eventTitle);
        context.setVariable("eventLocation", eventLocation);
        context.setVariable("eventDate", eventDate);
        context.setVariable("missingBlocks", missingBlocks);
        context.setVariable("enrollLink", enrollLink);
        context.setVariable("allEventBlocks", allEventBlocks);

        return templateEngine.process("event_notification_email.html", context);
    }

    public String loadAttendanceCertificateTemplate(String name, int eventsCount, int blocksCount, String stageName, List<String> events) {
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("eventsCount", eventsCount);
        context.setVariable("blocksCount", blocksCount);
        context.setVariable("stageName", stageName);
        context.setVariable("events", events);

        return templateEngine.process("attendance_certificate_email.html", context);
    }

    public String loadBlockCertificateTemplate(String name, List<String> blocks, String progressLink) {
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("blocks", blocks);
        context.setVariable("progressLink", progressLink);

        return templateEngine.process("block_certificate_email.html", context);
    }

    public String loadStageCertificateTemplate(String name, String stageName) {
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("stageName", stageName);

        return templateEngine.process("stage_certificate_email.html", context);
    }

    private String generateScoutGroupString(ScoutGroup scoutGroup) {
        if (scoutGroup == null) {
            return "Sin Entidad de Procedencia";
        }
        return scoutGroup.getName() + " " + scoutGroup.getGroupNumber();
    }
}
