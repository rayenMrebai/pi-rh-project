package org.example.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.model.formation.Skill;
import org.example.services.SkillService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ListSkills implements Initializable {

    @FXML private TableView<Skill> skillTable;
    @FXML private TableColumn<Skill, Integer> idColumn;
    @FXML private TableColumn<Skill, String> nomColumn;
    @FXML private TableColumn<Skill, String> descriptionColumn;
    @FXML private TableColumn<Skill, String> categorieColumn;
    @FXML private TableColumn<Skill, Integer> levelColumn;
    @FXML private Label countLabel;
    @FXML private Label statusLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterCategorie;

    private SkillService skillService;
    private ObservableList<Skill> skillList;
    private ObservableList<Skill> allSkills;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        skillService = new SkillService();

        // Configurer les colonnes
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        categorieColumn.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        levelColumn.setCellValueFactory(new PropertyValueFactory<>("levelRequired"));

        // Configurer le filtre
        filterCategorie.setItems(FXCollections.observableArrayList("Toutes", "technique", "soft"));
        filterCategorie.setValue("Toutes");
        filterCategorie.setOnAction(e -> appliquerFiltres());

        // Recherche en temps réel
        searchField.textProperty().addListener((observable, oldValue, newValue) -> appliquerFiltres());

        // Charger les données
        chargerSkills();
    }

    @FXML
    private void handleGoToAdd() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AddFormSkill.fxml"));
            Stage stage = (Stage) skillTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter une Compétence");
        } catch (Exception e) {
            afficherMessage("❌ Erreur lors de l'ouverture du formulaire", "error");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRafraichir() {
        chargerSkills();
        afficherMessage("🔄 Liste rafraîchie avec succès", "info");
    }

    @FXML
    private void handleModifier() {
        Skill selectedSkill = skillTable.getSelectionModel().getSelectedItem();
        if (selectedSkill == null) {
            afficherMessage("⚠️ Veuillez sélectionner une compétence à modifier", "warning");
            return;
        }

        afficherMessage("ℹ️ Fonctionnalité de modification en cours de développement", "info");
    }

    @FXML
    private void handleSupprimer() {
        Skill selectedSkill = skillTable.getSelectionModel().getSelectedItem();

        if (selectedSkill == null) {
            afficherMessage("⚠️ Veuillez sélectionner une compétence à supprimer", "warning");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer la compétence : " + selectedSkill.getNom());
        alert.setContentText("Cette action est irréversible. Voulez-vous continuer ?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            skillService.delete(selectedSkill.getId());
            afficherMessage("✅ Compétence supprimée avec succès", "success");
            chargerSkills();
        }
    }

    private void chargerSkills() {
        try {
            List<Skill> skills = skillService.getAll();
            allSkills = FXCollections.observableArrayList(skills);
            skillList = FXCollections.observableArrayList(skills);
            skillTable.setItems(skillList);
            countLabel.setText(skills.size() + " compétence(s)");

            // Réinitialiser les filtres
            searchField.clear();
            filterCategorie.setValue("Toutes");
        } catch (Exception e) {
            afficherMessage("❌ Erreur lors du chargement des données", "error");
            e.printStackTrace();
        }
    }

    private void appliquerFiltres() {
        String searchText = searchField.getText().toLowerCase();
        String categorie = filterCategorie.getValue();

        List<Skill> filtered = allSkills.stream()
                .filter(skill -> {
                    boolean matchSearch = skill.getNom().toLowerCase().contains(searchText) ||
                            skill.getDescription().toLowerCase().contains(searchText);
                    boolean matchCategorie = categorie.equals("Toutes") || skill.getCategorie().equals(categorie);
                    return matchSearch && matchCategorie;
                })
                .collect(Collectors.toList());

        skillList = FXCollections.observableArrayList(filtered);
        skillTable.setItems(skillList);
        countLabel.setText(filtered.size() + " compétence(s)");
    }

    private void afficherMessage(String message, String type) {
        statusLabel.setText(message);

        switch (type) {
            case "success": statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;"); break;
            case "error": statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;"); break;
            case "warning": statusLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;"); break;
            case "info": statusLabel.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;"); break;
        }

        new Thread(() -> {
            try {
                Thread.sleep(4000);
                javafx.application.Platform.runLater(() -> statusLabel.setText(""));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}