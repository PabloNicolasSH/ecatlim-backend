package org.scoutsdecanarias.ecatlim_backend.features.certificate;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.core.exception.EcatlimException;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.LessonBlock;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Slf4j
@Service
public class CertificatePdfService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final TemplateEngine templateEngine;

    public CertificatePdfService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public byte[] generateBlockCertificate(User user, LessonBlock block, Date completionDate) {
        Context context = new Context();
        context.setVariable("fullName", fullName(user));
        context.setVariable("nif", user.getProfile() == null ? null : user.getProfile().getNif());
        context.setVariable("blockCode", block.getCode());
        context.setVariable("blockName", block.getName());
        context.setVariable("stageName", block.getModule() != null && block.getModule().getEducationStage() != null
                ? block.getModule().getEducationStage().getName() : null);
        context.setVariable("hours", block.getContactHours() + block.getOnlineHours());
        context.setVariable("completionDate", formatDate(completionDate));
        return render("pdf/block_certificate.html", context);
    }

    public byte[] generateStageDiploma(User user, String stageName, Date completionDate) {
        Context context = new Context();
        context.setVariable("fullName", fullName(user));
        context.setVariable("stageName", stageName);
        context.setVariable("completionDate", formatDate(completionDate));
        return render("pdf/stage_diploma.html", context);
    }

    public static String fullName(User user) {
        if (user.getProfile() == null) {
            return user.getEmail();
        }
        return (user.getProfile().getName() + " " + user.getProfile().getSurname()).trim();
    }

    private String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        return new java.sql.Date(date.getTime()).toLocalDate().format(DATE_FORMAT);
    }

    private byte[] render(String template, Context context) {
        String html = templateEngine.process(template, context);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (IOException | RuntimeException e) {
            log.error("METHOD render() - Could not generate PDF from template {}", template, e);
            throw new EcatlimException("No se ha podido generar el certificado, inténtalo de nuevo", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
