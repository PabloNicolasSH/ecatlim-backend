package org.scoutsdecanarias.ecatlim_backend.shared.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class EmailSenderService {
    private final JavaMailSender emailSender;

    public EmailSenderService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Async("emailExecutor")
    public void sendEmail(String subject, String html, String... to) {
        MimeMessage message = this.createEmailWithHtml(html, subject, List.of(), to);
        if (message != null) {
            emailSender.send(message);
        }
    }

    @Async("emailExecutor")
    public void sendEmailWithAttachments(String subject, String html, List<EmailAttachment> attachments, String to) {
        MimeMessage message = this.createEmailWithHtml(html, subject, attachments, to);
        if (message != null) {
            emailSender.send(message);
        }
    }

    private MimeMessage createEmailWithHtml(String htmlContent, String subject, List<EmailAttachment> attachments, String... to) {
        try {
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(subject);

            mimeMessageHelper.setText(htmlContent, true);

            for (EmailAttachment attachment : attachments) {
                mimeMessageHelper.addAttachment(attachment.fileName(), new ByteArrayResource(attachment.content()), attachment.mimeType());
            }

            return mimeMessage;
        } catch (MessagingException e) {
            log.error(e.getMessage());
        }
        return null;
    }
}
