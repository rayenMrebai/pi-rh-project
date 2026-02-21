package org.example.services.email;

import org.example.model.salaire.BonusRule;
import org.example.model.salaire.Salaire;

import java.time.format.DateTimeFormatter;

public class EmailTemplate {

    private static final String BASE_STYLE = """
        <style>
            body {
                font-family: Arial, sans-serif;
                line-height: 1.6;
                color: #333;
                max-width: 600px;
                margin: 0 auto;
            }
            .header {
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                color: white;
                padding: 30px;
                text-align: center;
                border-radius: 10px 10px 0 0;
            }
            .content {
                background: #f8f9fa;
                padding: 30px;
            }
            .detail-box {
                background: white;
                padding: 20px;
                margin: 20px 0;
                border-radius: 8px;
                box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            }
            .amount {
                font-size: 32px;
                font-weight: bold;
                color: #667eea;
                margin: 20px 0;
            }
            .info-row {
                padding: 10px 0;
                border-bottom: 1px solid #e9ecef;
            }
            .label {
                font-weight: bold;
                color: #666;
            }
            .value {
                color: #333;
                float: right;
            }
            .footer {
                background: #2d3748;
                color: white;
                padding: 20px;
                text-align: center;
                border-radius: 0 0 10px 10px;
                font-size: 12px;
            }
        </style>
    """;

    /**
     * 1️⃣ Template : Salaire créé
     */
    public static String salaryCreatedTemplate(Salaire salaire) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        return "<!DOCTYPE html><html><head>" + BASE_STYLE + "</head><body>" +
                "<div class='header'>" +
                "<h1>🎉 Votre salaire a été créé</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<p>Bonjour <strong>" + salaire.getUser().getName() + "</strong>,</p>" +
                "<p>Votre salaire pour le mois de <strong>" +
                salaire.getDatePaiement().getMonth() + " " +
                salaire.getDatePaiement().getYear() +
                "</strong> a été créé avec succès.</p>" +

                "<div class='detail-box'>" +
                "<div class='info-row'>" +
                "<span class='label'>Salaire de base:</span>" +
                "<span class='value'>" + String.format("%.2f", salaire.getBaseAmount()) + " DT</span>" +
                "</div>" +
                "<div class='info-row'>" +
                "<span class='label'>Bonus appliqué:</span>" +
                "<span class='value'>" + String.format("%.2f", salaire.getBonusAmount()) + " DT</span>" +
                "</div>" +
                "<div class='info-row' style='border-bottom: none;'>" +
                "<span class='label'>Date de paiement:</span>" +
                "<span class='value'>" + salaire.getDatePaiement().format(formatter) + "</span>" +
                "</div>" +
                "</div>" +

                "<div class='amount'>" +
                "💰 Total: " + String.format("%.2f", salaire.getTotalAmount()) + " DT" +
                "</div>" +

                "<p style='color: #666; font-size: 14px;'>" +
                "Statut actuel: <strong>" + salaire.getStatus() + "</strong>" +
                "</p>" +

                "<p>Vous recevrez une notification lorsque le paiement sera effectué.</p>" +
                "</div>" +
                "<div class='footer'>" +
                "INTEGRA - Système de gestion RH<br>" +
                "Cet email a été envoyé automatiquement, merci de ne pas y répondre." +
                "</div>" +
                "</body></html>";
    }

    /**
     * 2️⃣ Template : Règle de bonus activée
     */
    public static String bonusRuleActivatedTemplate(BonusRule rule, Salaire salaire) {
        return "<!DOCTYPE html><html><head>" + BASE_STYLE + "</head><body>" +
                "<div class='header'>" +
                "<h1>⭐ Règle de bonus activée !</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<p>Bonjour <strong>" + salaire.getUser().getName() + "</strong>,</p>" +
                "<p>Une nouvelle règle de bonus vient d'être <strong>activée</strong> sur votre salaire.</p>" +

                "<div class='detail-box'>" +
                "<h3 style='margin-top: 0; color: #667eea;'>📋 Détails de la règle</h3>" +
                "<div class='info-row'>" +
                "<span class='label'>Nom de la règle:</span>" +
                "<span class='value'>" + rule.getNomRegle() + "</span>" +
                "</div>" +
                "<div class='info-row'>" +
                "<span class='label'>Pourcentage:</span>" +
                "<span class='value'>" + String.format("%.0f", rule.getPercentage()) + "%</span>" +
                "</div>" +
                "<div class='info-row'>" +
                "<span class='label'>Montant du bonus:</span>" +
                "<span class='value' style='color: #27ae60; font-weight: bold;'>" +
                String.format("%.2f", rule.getBonus()) + " DT</span>" +
                "</div>" +
                "<div class='info-row' style='border-bottom: none;'>" +
                "<span class='label'>Condition:</span>" +
                "<span class='value'>" + rule.getCondition() + "</span>" +
                "</div>" +
                "</div>" +

                "<div class='detail-box' style='background: #e8f5e9;'>" +
                "<h3 style='margin-top: 0; color: #27ae60;'>💵 Nouveau salaire total</h3>" +
                "<div class='info-row' style='border: none;'>" +
                "<span class='label'>Salaire de base:</span>" +
                "<span class='value'>" + String.format("%.2f", salaire.getBaseAmount()) + " DT</span>" +
                "</div>" +
                "<div class='info-row' style='border: none;'>" +
                "<span class='label'>Total des bonus:</span>" +
                "<span class='value'>" + String.format("%.2f", salaire.getBonusAmount()) + " DT</span>" +
                "</div>" +
                "<div class='amount' style='font-size: 28px; margin: 10px 0;'>" +
                "= " + String.format("%.2f", salaire.getTotalAmount()) + " DT" +
                "</div>" +
                "</div>" +

                "<p style='color: #666;'>Félicitations ! 🎉</p>" +
                "</div>" +
                "<div class='footer'>" +
                "INTEGRA - Système de gestion RH<br>" +
                "Cet email a été envoyé automatiquement, merci de ne pas y répondre." +
                "</div>" +
                "</body></html>";
    }

    /**
     * 3️⃣ Template : Salaire payé
     */
    public static String salaryPaidTemplate(Salaire salaire) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        return "<!DOCTYPE html><html><head>" + BASE_STYLE + "</head><body>" +
                "<div class='header' style='background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);'>" +
                "<h1>✅ Votre salaire a été payé</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<p>Bonjour <strong>" + salaire.getUser().getName() + "</strong>,</p>" +
                "<p>Bonne nouvelle ! Votre salaire du mois de <strong>" +
                salaire.getDatePaiement().getMonth() + " " +
                salaire.getDatePaiement().getYear() +
                "</strong> a été <strong>versé avec succès</strong>.</p>" +

                "<div class='detail-box' style='background: #e8f5e9;'>" +
                "<h3 style='margin-top: 0; color: #27ae60;'>💳 Récapitulatif du paiement</h3>" +
                "<div class='amount' style='color: #27ae60;'>" +
                String.format("%.2f", salaire.getTotalAmount()) + " DT" +
                "</div>" +
                "<p style='text-align: center; color: #666; margin: 10px 0;'>versé le</p>" +
                "<p style='text-align: center; font-size: 18px; font-weight: bold; color: #333;'>" +
                salaire.getDatePaiement().format(formatter) +
                "</p>" +
                "</div>" +

                "<div class='detail-box'>" +
                "<h3 style='margin-top: 0;'>📊 Détails</h3>" +
                "<div class='info-row'>" +
                "<span class='label'>Salaire de base:</span>" +
                "<span class='value'>" + String.format("%.2f", salaire.getBaseAmount()) + " DT</span>" +
                "</div>" +
                "<div class='info-row' style='border-bottom: none;'>" +
                "<span class='label'>Bonus total:</span>" +
                "<span class='value'>" + String.format("%.2f", salaire.getBonusAmount()) + " DT</span>" +
                "</div>" +
                "</div>" +

                "<p>Vous pouvez consulter votre fiche de paie détaillée dans l'application.</p>" +
                "<p style='color: #666; font-size: 14px;'>Merci pour votre travail ! 💼</p>" +
                "</div>" +
                "<div class='footer'>" +
                "INTEGRA - Système de gestion RH<br>" +
                "Cet email a été envoyé automatiquement, merci de ne pas y répondre." +
                "</div>" +
                "</body></html>";
    }



}