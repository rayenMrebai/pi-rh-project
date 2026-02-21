
import org.example.services.email.EmailService;

public class TestEmail {

    public static void main(String[] args) {
        EmailService emailService = new EmailService();

        // ⚠️ REMPLACEZ PAR VOTRE VRAI EMAIL
        String testEmail = "VOTRE_EMAIL@gmail.com";

        System.out.println("📧 Test d'envoi d'email...");
        emailService.sendTestEmail(testEmail);
        System.out.println("✅ Vérifiez votre boîte de réception !");
    }
}