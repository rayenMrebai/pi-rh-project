package org.example.services.email;

import org.example.config.EmailConfig;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import java.io.File;

public class EmailService {

    /**
     * Méthode principale d'envoi d'email
     */
    public boolean sendEmail(String toEmail, String subject, String htmlContent) {

        // Validation de l'email destinataire
        if (toEmail == null || toEmail.isEmpty() || !toEmail.contains("@")) {
            System.err.println("❌ Adresse email invalide: " + toEmail);
            return false;
        }

        try {
            // Configuration SMTP
            Properties props = new Properties();
            props.put("mail.smtp.auth", EmailConfig.isSmtpAuthEnabled());
            props.put("mail.smtp.starttls.enable", EmailConfig.isStartTlsEnabled());
            props.put("mail.smtp.host", EmailConfig.getSmtpHost());
            props.put("mail.smtp.port", EmailConfig.getSmtpPort());

            // Authentification
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                            EmailConfig.getUsername(),
                            EmailConfig.getPassword()
                    );
                }
            });

            // Création du message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(
                    EmailConfig.getFromAddress(),
                    EmailConfig.getFromName()
            ));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(toEmail)
            );
            message.setSubject(subject);

            // Contenu HTML
            message.setContent(htmlContent, "text/html; charset=utf-8");

            // Envoi
            Transport.send(message);

            System.out.println("✅ Email envoyé avec succès à: " + toEmail);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de l'email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Méthode de test simple
     */
    public void sendTestEmail(String toEmail) {
        String subject = "Test Email - INTEGRA RH";
        String content = "<h1>Test réussi !</h1><p>Votre configuration email fonctionne.</p>";
        sendEmail(toEmail, subject, content);
    }

    /**
     * Envoie un email avec pièce jointe PDF
     */
    public boolean sendEmailWithAttachment(String toEmail, String subject, String htmlContent, String attachmentPath) {

        if (toEmail == null || toEmail.isEmpty() || !toEmail.contains("@")) {
            System.err.println("❌ Adresse email invalide: " + toEmail);
            return false;
        }

        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", EmailConfig.isSmtpAuthEnabled());
            props.put("mail.smtp.starttls.enable", EmailConfig.isStartTlsEnabled());
            props.put("mail.smtp.host", EmailConfig.getSmtpHost());
            props.put("mail.smtp.port", EmailConfig.getSmtpPort());

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                            EmailConfig.getUsername(),
                            EmailConfig.getPassword()
                    );
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(
                    EmailConfig.getFromAddress(),
                    EmailConfig.getFromName()
            ));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(toEmail)
            );
            message.setSubject(subject);

            // ⭐ Créer un message multipart (texte + pièce jointe)
            Multipart multipart = new MimeMultipart();

            // Partie 1 : Contenu HTML
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlContent, "text/html; charset=utf-8");
            multipart.addBodyPart(htmlPart);

            // Partie 2 : Pièce jointe PDF
            if (attachmentPath != null && !attachmentPath.isEmpty()) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                try {
                    attachmentPart.attachFile(new File(attachmentPath));
                    multipart.addBodyPart(attachmentPart);
                } catch (Exception e) {
                    System.err.println("⚠️ Impossible d'attacher le fichier: " + e.getMessage());
                }
            }

            message.setContent(multipart);

            // Envoi
            Transport.send(message);

            System.out.println("✅ Email avec pièce jointe envoyé à: " + toEmail);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de l'email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}