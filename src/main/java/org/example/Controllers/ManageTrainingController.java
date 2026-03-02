package org.example.Controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.model.formation.Skill;
import org.example.model.formation.TrainingProgram;
import org.example.model.user.UserAccount;
import org.example.services.formation.SkillService;
import org.example.services.formation.TrainingProgramService;
import org.example.util.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ManageTrainingController implements Initializable {

    @FXML private Label userNameLabel;

    @FXML private TableView<Skill> skillsTable;
    @FXML private TableColumn<Skill, String> skillIdColumn;
    @FXML private TableColumn<Skill, String> skillNameColumn;
    @FXML private TableColumn<Skill, String> skillLevelColumn;
    @FXML private TableColumn<Skill, String> skillCategoryColumn;
    @FXML private TextField searchSkillField;
    @FXML private ComboBox<String> filterCategoryCombo;

    @FXML private TableView<TrainingProgram> trainingsTable;
    @FXML private TableColumn<TrainingProgram, String> trainingIdColumn;
    @FXML private TableColumn<TrainingProgram, String> trainingNameColumn;
    @FXML private TableColumn<TrainingProgram, String> trainingDurationColumn;
    @FXML private TableColumn<TrainingProgram, String> trainingSkillColumn;
    @FXML private TableColumn<TrainingProgram, String> trainingTypeColumn;
    @FXML private TableColumn<TrainingProgram, String> trainingStatusColumn;

    @FXML private Label selectionLabel;
    @FXML private Label detailNameLabel;
    @FXML private Label detailTrainerLabel;
    @FXML private Label detailDurationLabel;
    @FXML private Label detailStatusLabel;
    @FXML private Label detailDescriptionLabel;
    @FXML private Label detailSkillLabel;
    @FXML private Label footerLabel;

    private SkillService skillService;
    private TrainingProgramService trainingService;
    private ObservableList<Skill> allSkills;
    private UserAccount loggedInUser;

    // ===================== SET USER =====================

    public void setLoggedInUser(UserAccount user) {
        this.loggedInUser = user;
        if (userNameLabel != null && user != null) {
            userNameLabel.setText(user.getUsername() + " (" + user.getRole() + ")");
        }
    }

    // ===================== INITIALIZE =====================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        skillService = new SkillService();
        trainingService = new TrainingProgramService();

        skillIdColumn.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getId())));
        skillNameColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNom()));
        skillLevelColumn.setCellValueFactory(d -> new SimpleStringProperty(getLevelBadge(d.getValue().getLevelRequired())));
        skillCategoryColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCategorie().toUpperCase()));

        trainingIdColumn.setCellValueFactory(d -> new SimpleStringProperty(String.format("%04d", d.getValue().getId())));
        trainingNameColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitle()));
        trainingDurationColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDuration() + " sem."));
        trainingSkillColumn.setCellValueFactory(d -> new SimpleStringProperty(getSkillForTraining(d.getValue())));
        trainingTypeColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getType().toUpperCase()));
        trainingStatusColumn.setCellValueFactory(d -> new SimpleStringProperty(getStatus(d.getValue())));

        applyCellStyles();

        filterCategoryCombo.setItems(FXCollections.observableArrayList("Toutes", "technique", "soft"));
        filterCategoryCombo.setValue("Toutes");
        filterCategoryCombo.setOnAction(e -> applyFilters());
        searchSkillField.textProperty().addListener((obs, old, nv) -> applyFilters());

        skillsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, o, n) -> {
                    if (n != null) loadTrainingsForSkill(n);
                    else loadAllTrainings();
                });

        skillsTable.setOnMouseClicked(event -> {
            if (skillsTable.getSelectionModel().getSelectedItem() == null) {
                loadAllTrainings();
            }
        });

        trainingsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, o, n) -> {
                    if (n != null) displayTrainingDetails(n);
                });

        trainingsTable.setOnMouseClicked(event -> {
            if (skillsTable.getSelectionModel().getSelectedItem() == null) {
                loadAllTrainings();
            }
        });

        loadSkills();
        loadAllTrainings();
        updateFooter();
    }

    // ===================== LOAD DATA =====================

    private void loadSkills() {
        try {
            List<Skill> skills = skillService.getAll();
            allSkills = FXCollections.observableArrayList(skills);
            skillsTable.setItems(null);          // ✅ forcer reset
            skillsTable.setItems(allSkills);     // ✅ recharger
            skillsTable.refresh();               // ✅ forcer rafraîchissement visuel
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadAllTrainings() {
        selectionLabel.setText("📋 Tous les programmes de formation");
        selectionLabel.setStyle("-fx-text-fill: #546e7a; -fx-font-size: 13px; -fx-font-style: italic;");
        trainingsTable.setItems(FXCollections.observableArrayList(trainingService.getAll()));
    }

    private void loadTrainingsForSkill(Skill skill) {
        List<TrainingProgram> allTrainings = trainingService.getAll();
        List<TrainingProgram> trainings = allTrainings.stream()
                .filter(t -> {
                    List<Skill> skills = skillService.getByTrainingProgramId(t.getId());
                    return skills.stream().anyMatch(s -> s.getId() == skill.getId());
                })
                .collect(Collectors.toList());

        if (trainings.isEmpty()) {
            selectionLabel.setText("⚠️ Aucune formation associée à : " + skill.getNom());
            selectionLabel.setStyle("-fx-text-fill: #f57c00; -fx-font-size: 12px; -fx-font-style: italic;");
            trainingsTable.setItems(FXCollections.observableArrayList(allTrainings));
        } else {
            selectionLabel.setText("📚 Formations pour : " + skill.getNom());
            selectionLabel.setStyle("-fx-text-fill: #1e88e5; -fx-font-size: 13px; -fx-font-weight: bold;");
            trainingsTable.setItems(FXCollections.observableArrayList(trainings));
        }
    }

    private void applyFilters() {
        String txt = searchSkillField.getText().toLowerCase();
        String cat = filterCategoryCombo.getValue();
        skillsTable.setItems(FXCollections.observableArrayList(
                allSkills.stream()
                        .filter(s -> (s.getNom().toLowerCase().contains(txt) ||
                                s.getDescription().toLowerCase().contains(txt))
                                && (cat.equals("Toutes") || s.getCategorie().equals(cat)))
                        .collect(Collectors.toList())));
    }

    private void displayTrainingDetails(TrainingProgram t) {
        detailNameLabel.setText(t.getTitle());
        detailTrainerLabel.setText("Dr. " + (t.getId() % 2 == 0 ? "Michael Stevens" : "Sophie Martin"));
        detailDurationLabel.setText(t.getDuration() + " semaines");
        detailDescriptionLabel.setText(t.getDescription());

        String status = getStatus(t);
        detailStatusLabel.setText(status);
        detailStatusLabel.setStyle(
                status.equals("EN COURS")  ? "-fx-text-fill: #66bb6a; -fx-font-weight: bold; -fx-font-size: 13px;" :
                        status.equals("PROGRAMMÉ") ? "-fx-text-fill: #1e88e5; -fx-font-weight: bold; -fx-font-size: 13px;" :
                                "-fx-text-fill: #f57c00; -fx-font-weight: bold; -fx-font-size: 13px;");

        String skillName = getSkillForTraining(t);
        detailSkillLabel.setText(skillName);
        detailSkillLabel.setStyle(!skillName.equals("Aucune")
                ? "-fx-background-color: #66bb6a; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 4; -fx-font-weight: bold;"
                : "-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 4; -fx-font-weight: bold;");
    }

    // ===================== ACTIONS SKILL =====================

    @FXML
    private void handleAddSkill() {
        try {
            openModal("/AddFormSkill.fxml", "Ajouter une Compétence");
            loadSkills();
            loadAllTrainings();
            updateFooter();
        } catch (Exception e) {
            showAlert("❌ " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEditSkill() {
        Skill sel = skillsTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert("⚠️ Sélectionnez une compétence à modifier");
            return;
        }
        try {
            // ✅ ORDRE CORRECT : load → getController → setSkill → showAndWait
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UpdateFormSkill.fxml"));
            Parent root = loader.load();                            // 1. charger FXML

            UpdateFormSkillController ctrl = loader.getController();
            ctrl.setSkill(sel);                                     // 2. passer données

            Stage popup = new Stage();
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.initOwner(skillsTable.getScene().getWindow());
            popup.setTitle("Modifier la Compétence");
            popup.setResizable(false);
            popup.setScene(new Scene(root));
            popup.showAndWait();                                    // 3. ouvrir

            loadSkills();
            updateFooter();
        } catch (Exception e) {
            showAlert("❌ " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteSkill() {
        Skill sel = skillsTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert("⚠️ Sélectionnez une compétence à supprimer");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText("Supprimer la compétence ?");
        confirm.setContentText("Voulez-vous vraiment supprimer '" + sel.getNom() + "' ?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            skillService.delete(sel.getId());
            loadSkills();
            loadAllTrainings();
            updateFooter();
            showAlert("✅ Compétence '" + sel.getNom() + "' supprimée.");
        }
    }

    // ===================== ACTIONS TRAINING =====================

    @FXML
    private void handleAddTraining() {
        try {
            openModal("/AddFormTrainingProgram.fxml", "Ajouter une Formation");
            loadAllTrainings();
            loadSkills();
            updateFooter();
        } catch (Exception e) {
            showAlert("❌ " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdateTraining() {
        TrainingProgram sel = trainingsTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert("⚠️ Sélectionnez un programme de formation à modifier");
            return;
        }
        try {
            // ✅ ORDRE CORRECT : load → getController → setTrainingProgram → showAndWait
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UpdateFormTrainingProgram.fxml"));
            Parent root = loader.load();                            // 1. charger FXML

            UpdateFormTrainingProgramController ctrl = loader.getController();
            ctrl.setTrainingProgram(sel);                           // 2. passer données

            Stage popup = new Stage();
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.initOwner(trainingsTable.getScene().getWindow());
            popup.setTitle("Modifier la Formation");
            popup.setResizable(false);
            popup.setScene(new Scene(root));
            popup.showAndWait();                                    // 3. ouvrir

            loadAllTrainings();
            updateFooter();
        } catch (Exception e) {
            showAlert("❌ " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteTraining() {
        TrainingProgram sel = trainingsTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert("⚠️ Sélectionnez un programme de formation à supprimer");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText("Supprimer la formation ?");
        confirm.setContentText("Voulez-vous vraiment supprimer '" + sel.getTitle() + "' ?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            trainingService.delete(sel.getId());
            loadAllTrainings();
            loadSkills();
            updateFooter();
            showAlert("✅ Formation '" + sel.getTitle() + "' supprimée.");
        }
    }

    @FXML
    private void handleAssignSkill() {
        Skill sk = skillsTable.getSelectionModel().getSelectedItem();
        TrainingProgram tr = trainingsTable.getSelectionModel().getSelectedItem();
        if (sk == null || tr == null) {
            showAlert("⚠️ Sélectionnez une compétence ET une formation");
            return;
        }
        skillService.assignToTraining(sk.getId(), tr.getId());
        showAlert("✅ Compétence '" + sk.getNom() + "' assignée à '" + tr.getTitle() + "' !");
        loadTrainingsForSkill(sk);
        loadAllTrainings();
        updateFooter();
    }

    // ===================== NAVIGATION =====================

    @FXML
    private void handleGoToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dashboard.fxml"));
            Parent root = loader.load();
            DashboardController ctrl = loader.getController();
            ctrl.setLoggedInUser(loggedInUser);

            Stage stage = (Stage) skillsTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Mon Tableau de Bord");
            stage.setMaximized(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Déconnexion");
        a.setHeaderText("Voulez-vous vraiment vous déconnecter ?");
        if (a.showAndWait().get() == ButtonType.OK) {
            SessionManager.logout();
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
                Stage stage = (Stage) skillsTable.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("INTEGRA – Connexion");
                stage.setResizable(false);
                stage.setMaximized(false);
                stage.centerOnScreen();
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // ===================== HELPERS =====================

    // ✅ openModal : pour les formulaires Add (aucune donnée à passer avant ouverture)
    private void openModal(String fxml, String title) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initOwner(skillsTable.getScene().getWindow());
        popup.setTitle(title);
        popup.setResizable(false);
        popup.setScene(new Scene(loader.load()));
        popup.showAndWait();
    }

    private String getSkillForTraining(TrainingProgram t) {
        try {
            List<Skill> skills = skillService.getByTrainingProgramId(t.getId());
            if (!skills.isEmpty())
                return skills.stream().map(Skill::getNom).collect(Collectors.joining(", "));
        } catch (Exception ignored) {}
        return "Aucune";
    }

    private String getStatus(TrainingProgram t) {
        long now = System.currentTimeMillis();
        long start = t.getStartDate().getTime();
        long end = t.getEndDate().getTime();
        return now >= start && now <= end ? "EN COURS" : now < start ? "PROGRAMMÉ" : "TERMINÉ";
    }

    private String getLevelBadge(int l) {
        switch (l) {
            case 5: return "EXPERT";
            case 4: return "AVANCÉ";
            case 3: return "INTERMÉDIAIRE";
            case 2: return "DÉBUTANT";
            default: return "BASE";
        }
    }

    private void applyCellStyles() {
        skillLevelColumn.setCellFactory(col -> new TableCell<Skill, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                String c = item.equals("EXPERT") ? "#f44336" : item.equals("AVANCÉ") ? "#ff9800" :
                        item.equals("INTERMÉDIAIRE") ? "#4caf50" : "#9e9e9e";
                setStyle("-fx-background-color:" + c + "; -fx-text-fill:white; -fx-font-weight:bold; -fx-alignment:CENTER;");
            }
        });

        skillCategoryColumn.setCellFactory(col -> new TableCell<Skill, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle("-fx-background-color:" + (item.equals("TECHNIQUE") ? "#2196f3" : "#e91e63") +
                        "; -fx-text-fill:white; -fx-font-weight:bold; -fx-alignment:CENTER;");
            }
        });

        trainingTypeColumn.setCellFactory(col -> new TableCell<TrainingProgram, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                String c = item.contains("LIGNE") ? "#2196f3" : item.contains("HYBRIDE") ? "#9c27b0" :
                        item.contains("WORKSHOP") ? "#ff9800" : "#f44336";
                setStyle("-fx-background-color:" + c + "; -fx-text-fill:white; -fx-font-weight:bold; -fx-alignment:CENTER;");
            }
        });

        trainingStatusColumn.setCellFactory(col -> new TableCell<TrainingProgram, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                String c = item.equals("EN COURS") ? "#66bb6a" :
                        item.equals("PROGRAMMÉ") ? "#1e88e5" : "#9e9e9e";
                setStyle("-fx-background-color:" + c + "; -fx-text-fill:white; -fx-font-weight:bold; -fx-alignment:CENTER;");
            }
        });
    }

    private void updateFooter() {
        try {
            footerLabel.setText(String.format(
                    "Connecté • Base de données : INTEGRA_DB • %d compétences • %d programmes",
                    skillService.getAll().size(), trainingService.getAll().size()));
        } catch (Exception e) {
            footerLabel.setText("Connecté • Base de données : INTEGRA_DB");
        }
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Information");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
