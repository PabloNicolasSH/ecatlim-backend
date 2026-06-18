package org.scoutsdecanarias.ecatlim_backend.service;

import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.entity.ScoutGroup;
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

    public String loadWelcomeEmailTemplate(String name, String email, String password) {
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("email", email);
        context.setVariable("password", password);

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

    private String generateScoutGroupString(ScoutGroup scoutGroup){
        return scoutGroup.getName() + " " + scoutGroup.getGroupNumber();
    }
}
