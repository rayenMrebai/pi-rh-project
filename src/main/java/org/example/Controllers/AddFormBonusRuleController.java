package org.example.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.model.salaire.BonusRule;
import org.example.model.salaire.Salaire;
import org.example.enums.BonusRuleStatus;
import org.example.services.salaire.BonusRuleService;
import org.example.services.salaire.SalaireService;

public class AddFormBonusRuleController {

    @FXML private TextField txtNomRegle;
    @FXML private TextField txtPercentage;

    private Salaire currentSalaire;
    private final BonusRuleService bonusRuleService = new BonusRuleService();
    private final SalaireService salaireService = new SalaireService();

    public void setSalaire(Salaire s) {
        this.currentSalaire = s;
    }

    @FXML
    private void onSave() {
        double pct = Double.parseDouble(txtPercentage.getText());

        // 1. Créer la règle de bonus
        BonusRule rule = new BonusRule();
        rule.setSalaire(currentSalaire);
        rule.setNomRegle(txtNomRegle.getText());
        rule.setPercentage(pct);

        // CALCUL DU MONTANT (ex: 10% du salaire de base)
        double montantBonus = (currentSalaire.getBaseAmount() * pct) / 100;
        rule.setBonus(montantBonus);
        rule.setStatus(BonusRuleStatus.ACTIVE);

        // 2. Sauvegarder la règle via le service
        bonusRuleService.create(rule);

        // 3. MISE À JOUR DU SALAIRE PARENT (Recalcul du total)
        currentSalaire.setBonusAmount(currentSalaire.getBonusAmount() + montantBonus);
        currentSalaire.setTotalAmount(currentSalaire.getBaseAmount() + currentSalaire.getBonusAmount());

        // Mettre à jour le salaire dans la DB pour que le total change partout
        salaireService.update(currentSalaire);

        ((Stage) txtNomRegle.getScene().getWindow()).close();
    }
}