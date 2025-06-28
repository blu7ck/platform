package com.blu4ck.topluluk_platform.Service;

import net.sargue.mailgun.Configuration;
import net.sargue.mailgun.Mail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
public class MailService {

    @Value("${mailgun.api-key}")
    private String apiKey;

    @Value("${mailgun.domain}")
    private String domain;

    @Value("${mailgun.from-email}")
    private String fromEmail;

    @Value("${mailgun.from-name}")
    private String fromName;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    private final TemplateEngine templateEngine;

    public MailService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    private Configuration getMailgunConfig() {
        return new Configuration()
                .domain(domain)
                .apiKey(apiKey)
                .from(fromName, fromEmail);
    }

    public void sendInvitationEmail(String toEmail, String inviteCode, String inviterName, String customMessage) {
        try {
            Context context = new Context();
            context.setVariable("inviteCode", inviteCode);
            context.setVariable("inviterName", inviterName);
            context.setVariable("customMessage", customMessage);
            context.setVariable("registerUrl", frontendUrl + "/register?code=" + inviteCode);
            context.setVariable("frontendUrl", frontendUrl);

            String htmlContent = templateEngine.process("emails/invitation", context);

            Mail.using(getMailgunConfig())
                    .to(toEmail)
                    .subject("Davetli Topluluk'a Davetlisiniz! 🎉")
                    .html(htmlContent)
                    .send();

            System.out.println("✅ Davet maili gönderildi: " + toEmail);
        } catch (Exception e) {
            System.err.println("❌ Mail gönderme hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendPasswordResetEmail(String toEmail, String resetToken, String userName) {
        try {
            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("resetUrl", frontendUrl + "/reset-password?token=" + resetToken);
            context.setVariable("frontendUrl", frontendUrl);

            String htmlContent = templateEngine.process("emails/password-reset", context);

            Mail.using(getMailgunConfig())
                    .to(toEmail)
                    .subject("Şifre Sıfırlama Talebi")
                    .html(htmlContent)
                    .send();

            System.out.println("✅ Şifre sıfırlama maili gönderildi: " + toEmail);
        } catch (Exception e) {
            System.err.println("❌ Şifre sıfırlama mail hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendPaymentNotification(String toEmail, String userName, String paymentType, String amount, String status) {
        try {
            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("paymentType", paymentType);
            context.setVariable("amount", amount);
            context.setVariable("status", status);
            context.setVariable("frontendUrl", frontendUrl);

            String htmlContent = templateEngine.process("emails/payment-notification", context);

            String subject = status.equals("completed") ? "Ödemeniz Alındı ✅" : "Ödeme Durumu Güncellendi";

            Mail.using(getMailgunConfig())
                    .to(toEmail)
                    .subject(subject)
                    .html(htmlContent)
                    .send();

            System.out.println("✅ Ödeme bildirimi gönderildi: " + toEmail);
        } catch (Exception e) {
            System.err.println("❌ Ödeme bildirimi hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendInteractionNotification(String toEmail, String userName, String interactionType, String contentTitle, String actorName) {
        try {
            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("interactionType", interactionType);
            context.setVariable("contentTitle", contentTitle);
            context.setVariable("actorName", actorName);
            context.setVariable("frontendUrl", frontendUrl);

            String htmlContent = templateEngine.process("emails/interaction-notification", context);

            Mail.using(getMailgunConfig())
                    .to(toEmail)
                    .subject("Yeni Etkileşim Bildirimi 🔔")
                    .html(htmlContent)
                    .send();

            System.out.println("✅ Etkileşim bildirimi gönderildi: " + toEmail);
        } catch (Exception e) {
            System.err.println("❌ Etkileşim bildirimi hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendCollaborationNotification(String toEmail, String userName, String collaborationType, String initiatorName, String title) {
        try {
            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("collaborationType", collaborationType);
            context.setVariable("initiatorName", initiatorName);
            context.setVariable("title", title);
            context.setVariable("frontendUrl", frontendUrl);

            String htmlContent = templateEngine.process("emails/collaboration-notification", context);

            Mail.using(getMailgunConfig())
                    .to(toEmail)
                    .subject("Yeni İşbirliği Talebi 🤝")
                    .html(htmlContent)
                    .send();

            System.out.println("✅ İşbirliği bildirimi gönderildi: " + toEmail);
        } catch (Exception e) {
            System.err.println("❌ İşbirliği bildirimi hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendMessageNotification(String toEmail, String userName, String senderName, String messagePreview) {
        try {
            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("senderName", senderName);
            context.setVariable("messagePreview", messagePreview);
            context.setVariable("frontendUrl", frontendUrl);

            String htmlContent = templateEngine.process("emails/message-notification", context);

            Mail.using(getMailgunConfig())
                    .to(toEmail)
                    .subject("Yeni Mesaj 💬")
                    .html(htmlContent)
                    .send();

            System.out.println("✅ Mesaj bildirimi gönderildi: " + toEmail);
        } catch (Exception e) {
            System.err.println("❌ Mesaj bildirimi hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Generic template mail sender
    public void sendTemplateEmail(String toEmail, String subject, String templateName, Map<String, Object> variables) {
        try {
            Context context = new Context();
            variables.forEach(context::setVariable);
            context.setVariable("frontendUrl", frontendUrl);

            String htmlContent = templateEngine.process("emails/" + templateName, context);

            Mail.using(getMailgunConfig())
                    .to(toEmail)
                    .subject(subject)
                    .html(htmlContent)
                    .send();

            System.out.println("✅ Template mail gönderildi: " + toEmail + " - " + templateName);
        } catch (Exception e) {
            System.err.println("❌ Template mail hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }
}