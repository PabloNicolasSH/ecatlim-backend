package org.scoutsdecanarias.ecatlim_backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

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

    public String loadPendingUserCreatedEmailTemplate(String name, String email) {
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("email", email);

        return templateEngine.process("pending_user_request_email.html", context);
    }
}
