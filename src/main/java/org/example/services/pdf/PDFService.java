package org.example.services.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.example.enums.BonusRuleStatus;
import org.example.model.salaire.BonusRule;
import org.example.model.salaire.Salaire;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class PDFService {

    private static final float MARGIN = 50;
    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();

    /**
     * Génère une fiche de paie PDF pour un salaire donné
     * @param salaire Le salaire pour lequel générer la fiche
     * @return Le chemin du fichier PDF généré
     */
    public String generatePayslip(Salaire salaire) {
        String fileName = generateFileName(salaire);
        String outputPath = "/mnt/user-data/outputs/" + fileName;

        try {
            PDDocument document = new PDDocument();
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream content = new PDPageContentStream(document, page);

            float yPosition = PAGE_HEIGHT - MARGIN;

            // En-tête
            yPosition = drawHeader(content, yPosition);
            yPosition -= 30;

            // Informations employé
            yPosition = drawEmployeeInfo(content, salaire, yPosition);
            yPosition -= 30;

            // Détails de la rémunération
            yPosition = drawSalaryDetails(content, salaire, yPosition);
            yPosition -= 20;

            // Règles de bonus
            if (salaire.getBonusRules() != null && !salaire.getBonusRules().isEmpty()) {
                yPosition = drawBonusRules(content, salaire, yPosition);
                yPosition -= 20;
            }

            // Total
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
            return outputPath;

        } catch (IOException e) {
            System.err.println("❌ Erreur génération PDF : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

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
        content.showText(salaire.getUser().getName());
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

    /**
     * Dessine les règles de bonus
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
            // Nom de la règle avec symbole check
            content.beginText();
            content.newLineAtOffset(MARGIN + 10, yPosition);
            content.showText("\u2713 " + rule.getNomRegle() + " (" + String.format("%.0f", rule.getPercentage()) + "%)");
            content.endText();

            // Montant
            String bonusStr = String.format("%.2f DT", rule.getBonus());
            float bonusWidth = PDType1Font.HELVETICA.getStringWidth(bonusStr) / 1000 * 9;
            content.beginText();
            content.newLineAtOffset(PAGE_WIDTH - MARGIN - bonusWidth, yPosition);
            content.showText(bonusStr);
            content.endText();

            yPosition -= 12;

            // Condition
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

    /**
     * Dessine le total
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
        String statusText = "Statut: " + (salaire.getStatus().name().equals("PAYÉ") ? "\u2713 PAYE" : salaire.getStatus().name());
        content.showText(statusText);
        content.endText();

        return yPosition - 15;
    }

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
        content.showText("INTEGRA - 2024");
        content.endText();
    }

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
     */
    private String generateFileName(Salaire salaire) {
        String employeeName = salaire.getUser().getName().replaceAll("\\s+", "_");
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return "fiche_paie_" + employeeName + "_" + date + ".pdf";
    }
}