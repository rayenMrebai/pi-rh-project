package org.example.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.example.model.formation.Skill;
import org.example.services.SkillService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ListSkillsController implements Initializable {

    @FXML private ListView<Skill> skillListView;
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

        // Configurer le filtre
        filterCategorie.setItems(FXCollections.observableArrayList("Toutes", "technique", "soft"));
        filterCategorie.setValue("Toutes");
        filterCategorie.setOnAction(e -> appliquerFiltres());

        // Recherche en temps réel
        searchField.textProperty().addListener((observable, oldValue, newValue) -> appliquerFiltres());

        // Configurer le rendu personnalisé de la ListView
        skillListView.setCellFactory(param -> new SkillListCell());

        // Charger les données
        chargerSkills();
    }

    @FXML
    private void handleGoToAdd() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AddFormSkill.fxml"));
            Stage stage = (Stage) skillListView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter une Compétence");
        } catch (Exception e) {
            afficherMessage("Erreur lors de l'ouverture du formulaire", "error");
            e.printStackTrace();
        }
    }
    @FXML
    private void handleGoToHome() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Home.fxml"));
            Stage stage = (Stage) skillListView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Accueil - Système de Gestion RH");
            stage.setMaximized(false);
        } catch (Exception e) {
            System.err.println("Erreur lors du retour à l'accueil : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRafraichir() {
        chargerSkills();
        afficherMessage("Liste rafraîchie avec succès", "info");
    }
    @FXML
    private void handleModifier() {
        Skill selectedSkill = skillListView.getSelectionModel().getSelectedItem();

        if (selectedSkill == null) {
            afficherMessage("⚠️ Veuillez sélectionner une compétence à modifier", "warning");
            return;
        }

        try {
            // Charger le FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UpdateFormSkill.fxml"));
            Parent root = loader.load();

            // IMPORTANT : Récupérer le controller APRÈS avoir chargé le FXML
            UpdateFormSkillController controller = loader.getController();

            // Passer le skill au controller pour pré-remplir les champs
            controller.setSkill(selectedSkill);

            // Changer de scène
            Stage stage = (Stage) skillListView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier une Compétence - " + selectedSkill.getNom());

        } catch (Exception e) {
            afficherMessage("❌ Erreur lors de l'ouverture du formulaire", "error");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSupprimer() {
        Skill selectedSkill = skillListView.getSelectionModel().getSelectedItem();

        if (selectedSkill == null) {
            afficherMessage("Veuillez sélectionner une compétence à supprimer", "warning");
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
            skillListView.setItems(skillList);
            countLabel.setText(skills.size() + " compétence(s)");

            // Réinitialiser les filtres
            searchField.clear();
            filterCategorie.setValue("Toutes");
        } catch (Exception e) {
            afficherMessage(" Erreur lors du chargement des données", "error");
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
        skillListView.setItems(skillList);
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

    // ========== CLASSE INTERNE POUR PERSONNALISER L'AFFICHAGE ==========
    static class SkillListCell extends ListCell<Skill> {
        @Override
        protected void updateItem(Skill skill, boolean empty) {
            super.updateItem(skill, empty);

            if (empty || skill == null) {
                setText(null);
                setGraphic(null);
            } else {
                VBox container = new VBox(8);
                container.setPadding(new Insets(12, 15, 12, 15));
                container.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

                // Ligne 1 : ID et Nom
                HBox ligne1 = new HBox(10);
                ligne1.setAlignment(Pos.CENTER_LEFT);

                Label idLabel = new Label("#" + skill.getId());
                idLabel.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 3; -fx-font-size: 11px; -fx-font-weight: bold;");

                Label nomLabel = new Label(skill.getNom());
                nomLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
                nomLabel.setStyle("-fx-text-fill: #2c3e50;");

                ligne1.getChildren().addAll(idLabel, nomLabel);

                // Ligne 2 : Description
                Label descriptionLabel = new Label(skill.getDescription());
                descriptionLabel.setWrapText(true);
                descriptionLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13px;");
                descriptionLabel.setMaxWidth(1000);

                // Ligne 3 : Catégorie et Niveau
                HBox ligne3 = new HBox(15);
                ligne3.setAlignment(Pos.CENTER_LEFT);

                Label categorieLabel = new Label(skill.getCategorie());
                if (skill.getCategorie().equals("technique")) {
                    categorieLabel.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 3; -fx-font-size: 12px; -fx-font-weight: bold;");
                } else {
                    categorieLabel.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 3; -fx-font-size: 12px; -fx-font-weight: bold;");
                }

                Label levelLabel = new Label("Niveau requis: " + skill.getLevelRequired() + "/5");
                levelLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 12px; -fx-font-weight: bold;");

                // Barres de niveau
                HBox levelBars = new HBox(3);
                for (int i = 1; i <= 5; i++) {
                    Label bar = new Label(" ");
                    bar.setPrefHeight(12);
                    bar.setPrefWidth(20);
                    if (i <= skill.getLevelRequired()) {
                        bar.setStyle("-fx-background-color: #27ae60; -fx-background-radius: 2;");
                    } else {
                        bar.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 2;");
                    }
                    levelBars.getChildren().add(bar);
                }

                ligne3.getChildren().addAll(categorieLabel, levelLabel, levelBars);

                container.getChildren().addAll(ligne1, descriptionLabel, ligne3);

                setGraphic(container);
            }
        }
    }
}