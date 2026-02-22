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
import org.example.services.SkillService;
import org.example.services.TrainingProgramService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ManageTrainingController implements Initializable {

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
                (obs, o, n) -> { if (n != null) loadTrainingsForSkill(n); });
        trainingsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, o, n) -> { if (n != null) displayTrainingDetails(n); });

        loadSkills();
        updateFooter();
    }

    private void loadSkills() {
        try {
            allSkills = FXCollections.observableArrayList(skillService.getAll());
            skillsTable.setItems(allSkills);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void applyFilters() {
        String txt = searchSkillField.getText().toLowerCase();
        String cat = filterCategoryCombo.getValue();
        skillsTable.setItems(FXCollections.observableArrayList(
                allSkills.stream()
                        .filter(s -> (s.getNom().toLowerCase().contains(txt) || s.getDescription().toLowerCase().contains(txt))
                                && (cat.equals("Toutes") || s.getCategorie().equals(cat)))
                        .collect(Collectors.toList())));
    }

    private void loadTrainingsForSkill(Skill skill) {
        List<TrainingProgram> trainings = trainingService.getAll().stream()
                .filter(t -> skillService.getByTrainingProgramId(t.getId()).stream().anyMatch(s -> s.getId() == skill.getId()))
                .collect(Collectors.toList());
        trainingsTable.setItems(FXCollections.observableArrayList(trainings));
        if (trainings.isEmpty()) {
            selectionLabel.setText("⚠️ Aucune formation associée à : " + skill.getNom());
            selectionLabel.setStyle("-fx-text-fill: #f57c00; -fx-font-size: 12px; -fx-font-style: italic;");
        } else {
            selectionLabel.setText("📚 Formations pour : " + skill.getNom());
            selectionLabel.setStyle("-fx-text-fill: #1e88e5; -fx-font-size: 13px; -fx-font-weight: bold;");
        }
    }

    private void displayTrainingDetails(TrainingProgram t) {
        detailNameLabel.setText(t.getTitle());
        detailTrainerLabel.setText("Dr. " + (t.getId() % 2 == 0 ? "Michael Stevens" : "Sophie Martin"));
        detailDurationLabel.setText(t.getDuration() + " semaines");
        detailDescriptionLabel.setText(t.getDescription());

        String status = getStatus(t);
        detailStatusLabel.setText(status);
        detailStatusLabel.setStyle(
                status.equals("EN COURS")  ? "-fx-text-fill: #66bb6a; -fx-font-weight: bold; -fx-font-size: 14px;" :
                        status.equals("PROGRAMMÉ") ? "-fx-text-fill: #1e88e5; -fx-font-weight: bold; -fx-font-size: 14px;" :
                                "-fx-text-fill: #f57c00; -fx-font-weight: bold; -fx-font-size: 14px;");

        String skillName = getSkillForTraining(t);
        detailSkillLabel.setText(skillName);
        detailSkillLabel.setStyle(!skillName.equals("Aucune")
                ? "-fx-background-color: #66bb6a; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 4; -fx-font-weight: bold;"
                : "-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 4; -fx-font-weight: bold;");
    }

    // ======================== POPUP HELPER ========================

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

    // ======================== ACTIONS ========================

    @FXML
    private void handleAddSkill() {
        try {
            openModal("/AddFormSkill.fxml", "Ajouter une Compétence");
            loadSkills();
            updateFooter();
        } catch (Exception e) { showAlert("❌ " + e.getMessage()); }
    }

    @FXML
    private void handleEditSkill() {
        Skill sel = skillsTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert("⚠️ Sélectionnez une compétence à modifier"); return; }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UpdateFormSkill.fxml"));
            Stage popup = new Stage();
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.initOwner(skillsTable.getScene().getWindow());
            popup.setTitle("Modifier la Compétence");
            popup.setResizable(false);
            popup.setScene(new Scene(loader.load()));
            UpdateFormSkillController ctrl = loader.getController();
            ctrl.setSkill(sel);
            popup.showAndWait();
            loadSkills();
            updateFooter();
        } catch (Exception e) { showAlert("❌ " + e.getMessage()); }
    }

    @FXML
    private void handleAddTraining() {
        try {
            openModal("/AddFormTrainingProgram.fxml", "Ajouter une Formation");
            Skill sel = skillsTable.getSelectionModel().getSelectedItem();
            if (sel != null) loadTrainingsForSkill(sel);
            updateFooter();
        } catch (Exception e) { showAlert("❌ " + e.getMessage()); }
    }

    @FXML
    private void handleUpdateTraining() {
        TrainingProgram sel = trainingsTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert("⚠️ Sélectionnez une formation à modifier"); return; }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UpdateFormTrainingProgram.fxml"));
            Stage popup = new Stage();
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.initOwner(trainingsTable.getScene().getWindow());
            popup.setTitle("Modifier la Formation");
            popup.setResizable(false);
            popup.setScene(new Scene(loader.load()));
            UpdateFormTrainingProgramController ctrl = loader.getController();
            ctrl.setTrainingProgram(sel);
            popup.showAndWait();
            Skill selectedSkill = skillsTable.getSelectionModel().getSelectedItem();
            if (selectedSkill != null) loadTrainingsForSkill(selectedSkill);
            updateFooter();
        } catch (Exception e) { showAlert("❌ " + e.getMessage()); }
    }

    @FXML
    private void handleAssignSkill() {
        Skill sk = skillsTable.getSelectionModel().getSelectedItem();
        TrainingProgram tr = trainingsTable.getSelectionModel().getSelectedItem();
        if (sk == null || tr == null) { showAlert("⚠️ Sélectionnez une compétence ET une formation"); return; }
        try {
            skillService.assignToTraining(sk.getId(), tr.getId());
            showAlert("✅ Compétence '" + sk.getNom() + "' assignée à '" + tr.getTitle() + "' !");
            loadTrainingsForSkill(sk);
            updateFooter();
        } catch (Exception e) { showAlert("❌ " + e.getMessage()); }
    }

    @FXML
    private void handleLogout() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Déconnexion");
        a.setHeaderText("Voulez-vous vraiment vous déconnecter ?");
        if (a.showAndWait().get() == ButtonType.OK)
            ((Stage) skillsTable.getScene().getWindow()).close();
    }

    // ======================== HELPERS ========================

    private String getSkillForTraining(TrainingProgram t) {
        try {
            List<Skill> skills = skillService.getByTrainingProgramId(t.getId());
            if (!skills.isEmpty()) return skills.stream().map(Skill::getNom).collect(Collectors.joining(", "));
        } catch (Exception ignored) {}
        return "Aucune";
    }

    private String getStatus(TrainingProgram t) {
        long now = System.currentTimeMillis(), start = t.getStartDate().getTime(), end = t.getEndDate().getTime();
        return now >= start && now <= end ? "EN COURS" : now < start ? "PROGRAMMÉ" : "TERMINÉ";
    }

    private String getLevelBadge(int l) {
        switch (l) {
            case 5: return "EXPERT"; case 4: return "AVANCÉ";
            case 3: return "INTERMÉDIAIRE"; case 2: return "DÉBUTANT"; default: return "BASE";
        }
    }

    private void applyCellStyles() {
        skillLevelColumn.setCellFactory(col -> new TableCell<Skill, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                String color = item.equals("EXPERT") ? "#f44336" : item.equals("AVANCÉ") ? "#ff9800" :
                        item.equals("INTERMÉDIAIRE") ? "#4caf50" : "#9e9e9e";
                setStyle("-fx-background-color:" + color + "; -fx-text-fill:white; -fx-font-weight:bold; -fx-alignment:CENTER;");
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
                String c = item.equals("EN COURS") ? "#66bb6a" : item.equals("PROGRAMMÉ") ? "#1e88e5" : "#9e9e9e";
                setStyle("-fx-background-color:" + c + "; -fx-text-fill:white; -fx-font-weight:bold; -fx-alignment:CENTER;");
            }
        });
    }

    private void updateFooter() {
        try {
            footerLabel.setText(String.format("Connecté • Base de données : INTEGRA_DB • %d compétences • %d programmes",
                    skillService.getAll().size(), trainingService.getAll().size()));
        } catch (Exception e) { footerLabel.setText("Connecté • Base de données : INTEGRA_DB"); }
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Information"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}