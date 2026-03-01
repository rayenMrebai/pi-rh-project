package org.example.services.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.example.enums.BonusRuleStatus;
import org.example.model.salaire.BonusRule;
import org.example.model.salaire.Salaire;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import java.io.InputStream;

public class PDFService {

    private static final float MARGIN = 50;
    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();

    // =========================================================================
    // GÉNÉRATION DU CODE UNIQUE
    // =========================================================================

    /**
     * Génère un code unique pour la fiche de paie
     * Format : FP-NOM-AAAAMMJJ-XXXXXXXX
     */
    private String generatePayslipCode(Salaire salaire) {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        String fullName = salaire.getUser().getUsername().replaceAll("\\s+", "");
        String namePart = fullName.toUpperCase()
                .substring(0, fullName.length());

        String uniquePart = UUID.randomUUID().toString()
                .substring(0, 8)
                .toUpperCase();

        return "FP-" + namePart + "-" + datePart + "-" + uniquePart;
    }

    // =========================================================================
    // MÉTHODE PRINCIPALE
    // =========================================================================

    /**
     * Génère une fiche de paie PDF pour un salaire donné.
     * Affiche une boîte de dialogue "Enregistrer sous" pour choisir l'emplacement.
     * Si l'utilisateur annule, sauvegarde dans C:\Users\MSI\Downloads par défaut.
     *
     * @param salaire Le salaire pour lequel générer la fiche
     * @return Le chemin du fichier PDF généré, ou null en cas d'erreur
     */
    public String generatePayslip(Salaire salaire) {
        String fileName = generateFileName(salaire);

        // Afficher la boîte de dialogue de sauvegarde
        String outputPath = showSaveDialog(fileName);

        // Si l'utilisateur a annulé → chemin par défaut
        if (outputPath == null) {
            outputPath = "C:\\Users\\MSI\\Downloads\\" + fileName;
            System.out.println("⚠️ Aucun chemin sélectionné, utilisation du chemin par défaut : " + outputPath);
        }

        // Créer les dossiers parents si nécessaire
        File outputFile = new File(outputPath);
        if (outputFile.getParentFile() != null && !outputFile.getParentFile().exists()) {
            boolean created = outputFile.getParentFile().mkdirs();
            if (!created) {
                System.err.println("❌ Impossible de créer le dossier : " + outputFile.getParentFile());
            }
        }

        // Générer le code unique de la fiche
        String payslipCode = generatePayslipCode(salaire);

        try {
            PDDocument document = new PDDocument();
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream content = new PDPageContentStream(document, page);
            drawLogo(document, content);
            float yPosition = PAGE_HEIGHT - MARGIN;

            // En-tête principal
            yPosition = drawHeader(content, yPosition);
            yPosition -= 10;

            // Code unique de la fiche (sous l'en-tête)
            yPosition = drawPayslipCode(content, payslipCode, yPosition);
            yPosition -= 20;

            // Informations employé
            yPosition = drawEmployeeInfo(content, salaire, yPosition);
            yPosition -= 30;

            // Détails de la rémunération
            yPosition = drawSalaryDetails(content, salaire, yPosition);
            yPosition -= 20;

            // Règles de bonus (si présentes)
            if (salaire.getBonusRules() != null && !salaire.getBonusRules().isEmpty()) {
                yPosition = drawBonusRules(content, salaire, yPosition);
                yPosition -= 20;
            }

            // Total net à payer
            yPosition = drawTotal(content, salaire, yPosition);
            yPosition -= 30;

            // Informations de paiement
            yPosition = drawPaymentInfo(content, salaire, yPosition);

            // Pied de page
            drawFooter(content);

            content.close();
            document.save(outputPath);
            document.close();

            System.out.println("✅ Fiche de paie générée : " + outputPath);
            System.out.println("📋 Code de la fiche : " + payslipCode);

            // Message de confirmation à l'utilisateur
            JOptionPane.showMessageDialog(
                    null,
                    "✅ Fiche de paie enregistrée avec succès !\n\n" +
                            "📁 Emplacement : " + outputPath + "\n" +
                            "📋 Code fiche  : " + payslipCode,
                    "Fiche de paie générée",
                    JOptionPane.INFORMATION_MESSAGE
            );

            return outputPath;

        } catch (IOException e) {
            System.err.println("❌ Erreur génération PDF : " + e.getMessage());
            e.printStackTrace();

            JOptionPane.showMessageDialog(
                    null,
                    "❌ Erreur lors de la génération de la fiche de paie :\n" + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE
            );
            return null;
        }
    }

    // =========================================================================
    // BOÎTE DE DIALOGUE "ENREGISTRER SOUS"
    // =========================================================================

    /**
     * Affiche une boîte de dialogue native "Enregistrer sous"
     * avec C:\Users\MSI\Downloads comme dossier par défaut.
     *
     * @param defaultFileName Nom de fichier proposé par défaut
     * @return Le chemin complet choisi par l'utilisateur, ou null si annulé
     */
    private String showSaveDialog(String defaultFileName) {
        // Appliquer le look & feel natif du système d'exploitation
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Enregistrer la fiche de paie");

        // Dossier par défaut : C:\Users\MSI\Downloads
        File defaultDir = new File("C:\\Users\\MSI\\Downloads");

        // Fallback si le dossier n'existe pas sur cette machine
        if (!defaultDir.exists()) {
            defaultDir = new File(System.getProperty("user.home") + File.separator + "Downloads");
        }
        if (!defaultDir.exists()) {
            defaultDir = new File(System.getProperty("user.home"));
        }

        fileChooser.setCurrentDirectory(defaultDir);
        fileChooser.setSelectedFile(new File(defaultDir, defaultFileName));

        // Filtre : fichiers PDF uniquement
        FileNameExtensionFilter pdfFilter = new FileNameExtensionFilter("Fichiers PDF (*.pdf)", "pdf");
        fileChooser.setFileFilter(pdfFilter);
        fileChooser.setAcceptAllFileFilterUsed(false);

        // Afficher la boîte de dialogue
        int result = fileChooser.showSaveDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            String path = fileChooser.getSelectedFile().getAbsolutePath();
            // Ajouter l'extension .pdf si l'utilisateur ne l'a pas tapée
            if (!path.toLowerCase().endsWith(".pdf")) {
                path += ".pdf";
            }
            return path;
        }

        // L'utilisateur a cliqué "Annuler"
        return null;
    }

    // =========================================================================
    // DESSIN DU CODE UNIQUE EN HAUT DE LA FICHE
    // =========================================================================

    /**
     * Dessine le code unique de la fiche en haut du document,
     * avec la date de génération alignée à droite.
     */
    private float drawPayslipCode(PDPageContentStream content, String code, float yPosition) throws IOException {
        content.setFont(PDType1Font.HELVETICA_BOLD, 9);

        // Code à gauche
        content.beginText();
        content.newLineAtOffset(MARGIN, yPosition);
        content.showText("Code fiche : " + code);
        content.endText();

        // Date de génération à droite
        String genDate = "Generee le : " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        float dateWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(genDate) / 1000 * 9;
        content.beginText();
        content.newLineAtOffset(PAGE_WIDTH - MARGIN - dateWidth, yPosition);
        content.showText(genDate);
        content.endText();

        yPosition -= 8;
        drawLine(content, MARGIN, yPosition, PAGE_WIDTH - MARGIN, yPosition);

        return yPosition - 8;
    }

    // =========================================================================
    // EN-TÊTE DU DOCUMENT
    // =========================================================================

    /**
     * Dessine l'en-tête du document
     */
    private float drawHeader(PDPageContentStream content, float yPosition) throws IOException {
        content.setFont(PDType1Font.HELVETICA_BOLD, 20);
        content.beginText();
        content.newLineAtOffset(MARGIN, yPosition);
        content.showText("INTEGRA - FICHE DE PAIE");
        content.endText();

        yPosition -= 20;
        content.setFont(PDType1Font.HELVETICA, 10);
        content.beginText();
        content.newLineAtOffset(MARGIN, yPosition);
        content.showText("Systeme de Gestion RH");
        content.endText();

        yPosition -= 15;
        drawLine(content, MARGIN, yPosition, PAGE_WIDTH - MARGIN, yPosition);

        return yPosition - 10;
    }

    private void drawLogo(PDDocument document, PDPageContentStream content) throws IOException {

        InputStream is = getClass().getClassLoader().getResourceAsStream("images/logo2.png");

        if (is != null) {
            PDImageXObject logo = PDImageXObject.createFromByteArray(
                    document,
                    is.readAllBytes(),
                    "logo"
            );

            float logoWidth = 80;   // largeur du logo
            float logoHeight = 50;  // hauteur du logo

            float x = PAGE_WIDTH - MARGIN - logoWidth;
            float y = PAGE_HEIGHT - MARGIN - logoHeight + 20;

            content.drawImage(logo, x, y, logoWidth, logoHeight);
        }
    }

    // =========================================================================
    // INFORMATIONS EMPLOYÉ
    // =========================================================================

    /**
     * Dessine les informations de l'employé
     */
    private float drawEmployeeInfo(PDPageContentStream content, Salaire salaire, float yPosition) throws IOException {
        content.setFont(PDType1Font.HELVETICA_BOLD, 12);
        content.beginText();
        content.newLineAtOffset(MARGIN, yPosition);
        content.showText("INFORMATIONS EMPLOYE");
        content.endText();

        yPosition -= 20;
        content.setFont(PDType1Font.HELVETICA, 10);

        // Nom
        content.beginText();
        content.newLineAtOffset(MARGIN, yPosition);
        content.showText("Nom: ");
        content.endText();

        content.setFont(PDType1Font.HELVETICA_BOLD, 10);
        content.beginText();
        content.newLineAtOffset(MARGIN + 100, yPosition);
        content.showText(salaire.getUser().getUsername());
        content.endText();

        yPosition -= 15;
        content.setFont(PDType1Font.HELVETICA, 10);

        // Email
        content.beginText();
        content.newLineAtOffset(MARGIN, yPosition);
        content.showText("Email: ");
        content.endText();

        content.beginText();
        content.newLineAtOffset(MARGIN + 100, yPosition);
        content.showText(salaire.getUser().getEmail());
        content.endText();

        yPosition -= 15;

        // Période
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        String periode = salaire.getDatePaiement().format(formatter);

        content.beginText();
        content.newLineAtOffset(MARGIN, yPosition);
        content.showText("Periode: ");
        content.endText();

        content.beginText();
        content.newLineAtOffset(MARGIN + 100, yPosition);
        content.showText(periode);
        content.endText();

        yPosition -= 10;
        drawLine(content, MARGIN, yPosition, PAGE_WIDTH - MARGIN, yPosition);

        return yPosition - 10;
    }

    // =========================================================================
    // DÉTAILS DU SALAIRE
    // =========================================================================

    /**
     * Dessine les détails du salaire
     */
    private float drawSalaryDetails(PDPageContentStream content, Salaire salaire, float yPosition) throws IOException {
        content.setFont(PDType1Font.HELVETICA_BOLD, 12);
        content.beginText();
        content.newLineAtOffset(MARGIN, yPosition);
        content.showText("DETAIL DE LA REMUNERATION");
        content.endText();

        yPosition -= 25;
        content.setFont(PDType1Font.HELVETICA, 10);

        // Salaire de base
        content.beginText();
        content.newLineAtOffset(MARGIN, yPosition);
        content.showText("Salaire de base");
        content.endText();

        String baseAmountStr = String.format("%.2f DT", salaire.getBaseAmount());
        float baseAmountWidth = PDType1Font.HELVETICA.getStringWidth(baseAmountStr) / 1000 * 10;
        content.beginText();
        content.newLineAtOffset(PAGE_WIDTH - MARGIN - baseAmountWidth, yPosition);
        content.showText(baseAmountStr);
        content.endText();

        return yPosition - 15;
    }

    // =========================================================================
    // RÈGLES DE BONUS
    // =========================================================================

    /**
     * Dessine les règles de bonus actives
     */
    private float drawBonusRules(PDPageContentStream content, Salaire salaire, float yPosition) throws IOException {
        List<BonusRule> activeRules = salaire.getBonusRules().stream()
                .filter(rule -> rule.getStatus() == BonusRuleStatus.ACTIVE)
                .collect(Collectors.toList());

        if (activeRules.isEmpty()) {
            return yPosition;
        }

        yPosition -= 10;
        drawLine(content, MARGIN + 20, yPosition, PAGE_WIDTH - MARGIN, yPosition);
        yPosition -= 15;

        content.setFont(PDType1Font.HELVETICA_BOLD, 10);
        content.beginText();
        content.newLineAtOffset(MARGIN, yPosition);
        content.showText("PRIMES ET BONUS:");
        content.endText();

        yPosition -= 20;
        content.setFont(PDType1Font.HELVETICA, 9);

        for (BonusRule rule : activeRules) {
            // Nom et pourcentage de la règle
            content.beginText();
            content.newLineAtOffset(MARGIN + 10, yPosition);
            content.showText("* " + rule.getNomRegle() + " (" + String.format("%.0f", rule.getPercentage()) + "%)");
            content.endText();

            // Montant du bonus aligné à droite
            String bonusStr = String.format("%.2f DT", rule.getBonus());
            float bonusWidth = PDType1Font.HELVETICA.getStringWidth(bonusStr) / 1000 * 9;
            content.beginText();
            content.newLineAtOffset(PAGE_WIDTH - MARGIN - bonusWidth, yPosition);
            content.showText(bonusStr);
            content.endText();

            yPosition -= 12;

            // Condition de la règle (tronquée si trop longue)
            content.setFont(PDType1Font.HELVETICA, 8);
            content.beginText();
            content.newLineAtOffset(MARGIN + 20, yPosition);
            String condition = rule.getCondition();
            if (condition.length() > 60) {
                condition = condition.substring(0, 57) + "...";
            }
            content.showText("Condition: " + condition);
            content.endText();

            yPosition -= 15;
            content.setFont(PDType1Font.HELVETICA, 9);
        }

        yPosition -= 5;
        drawLine(content, MARGIN + 20, yPosition, PAGE_WIDTH - MARGIN, yPosition);
        yPosition -= 15;

        // Total des bonus
        content.setFont(PDType1Font.HELVETICA_BOLD, 10);
        content.beginText();
        content.newLineAtOffset(MARGIN, yPosition);
        content.showText("Total des bonus");
        content.endText();

        String totalBonusStr = String.format("%.2f DT", salaire.getBonusAmount());
        float totalBonusWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(totalBonusStr) / 1000 * 10;
        content.beginText();
        content.newLineAtOffset(PAGE_WIDTH - MARGIN - totalBonusWidth, yPosition);
        content.showText(totalBonusStr);
        content.endText();

        return yPosition - 15;
    }

    // =========================================================================
    // TOTAL NET À PAYER
    // =========================================================================

    /**
     * Dessine le total net à payer
     */
    private float drawTotal(PDPageContentStream content, Salaire salaire, float yPosition) throws IOException {
        drawLine(content, MARGIN, yPosition, PAGE_WIDTH - MARGIN, yPosition);
        yPosition -= 20;

        content.setFont(PDType1Font.HELVETICA_BOLD, 14);
        content.beginText();
        content.newLineAtOffset(MARGIN, yPosition);
        content.showText("TOTAL NET A PAYER");
        content.endText();

        String totalStr = String.format("%.2f DT", salaire.getTotalAmount());
        float totalWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(totalStr) / 1000 * 14;
        content.beginText();
        content.newLineAtOffset(PAGE_WIDTH - MARGIN - totalWidth, yPosition);
        content.showText(totalStr);
        content.endText();

        yPosition -= 10;
        drawLine(content, MARGIN, yPosition, PAGE_WIDTH - MARGIN, yPosition);

        return yPosition - 10;
    }

    // =========================================================================
    // INFORMATIONS DE PAIEMENT
    // =========================================================================

    /**
     * Dessine les informations de paiement
     */
    private float drawPaymentInfo(PDPageContentStream content, Salaire salaire, float yPosition) throws IOException {
        content.setFont(PDType1Font.HELVETICA, 9);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Date de paiement
        content.beginText();
        content.newLineAtOffset(MARGIN, yPosition);
        content.showText("Date de paiement: " + salaire.getDatePaiement().format(formatter));
        content.endText();

        yPosition -= 15;

        // Mode de paiement
        content.beginText();
        content.newLineAtOffset(MARGIN, yPosition);
        content.showText("Mode de paiement: Virement bancaire");
        content.endText();

        yPosition -= 15;

        // Statut
        content.setFont(PDType1Font.HELVETICA_BOLD, 9);
        content.beginText();
        content.newLineAtOffset(MARGIN, yPosition);
        String statusText = "Statut: " + (salaire.getStatus().name().equals("PAYÉ") ? "[X] PAYE" : salaire.getStatus().name());
        content.showText(statusText);
        content.endText();

        return yPosition - 15;
    }

    // =========================================================================
    // PIED DE PAGE
    // =========================================================================

    /**
     * Dessine le pied de page
     */
    private void drawFooter(PDPageContentStream content) throws IOException {
        float yPosition = MARGIN + 20;

        content.setFont(PDType1Font.HELVETICA, 8);
        content.beginText();
        content.newLineAtOffset(MARGIN, yPosition);
        content.showText("Ce document est genere automatiquement par le systeme INTEGRA RH");
        content.endText();

        yPosition -= 12;
        content.beginText();
        content.newLineAtOffset(MARGIN, yPosition);
        content.showText("INTEGRA - 2026");
        content.endText();
    }

    // =========================================================================
    // UTILITAIRES
    // =========================================================================

    /**
     * Dessine une ligne horizontale
     */
    private void drawLine(PDPageContentStream content, float x1, float y, float x2, float yEnd) throws IOException {
        content.moveTo(x1, y);
        content.lineTo(x2, yEnd);
        content.stroke();
    }

    /**
     * Génère le nom du fichier PDF
     * Format : fiche_paie_NOM_PRENOM_YYYY-MM-DD.pdf
     */
    private String generateFileName(Salaire salaire) {
        String employeeName = salaire.getUser().getUsername().replaceAll("\\s+", "_");
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return "fiche_paie_" + employeeName + "_" + date + ".pdf";
    }
}