package org.example.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.model.recrutement.Candidat;
import org.example.model.recrutement.JobPosition;
import org.example.services.recrutement.CandidatService;
import org.example.services.recrutement.JobPositionService;
import org.example.services.recrutement.ai.ATSService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.stage.FileChooser;
import java.io.File;
import java.time.LocalDate;
import org.example.services.recrutement.pdf.PdfExportService;

public class manageRecruitment {

    // ====== JOB CARDS ======
    @FXML private VBox jobCardsContainer;

    @FXML private Label statsTotalJobs;
    @FXML private Label statsTotalCandidats;
    @FXML private Label statsOpenJobs;
    @FXML private Label statsInReview;
    @FXML private Label statsTotalJobs2;

    // ====== CANDIDAT TABLE ======
    @FXML private TableView<Candidat> candidatTable;
    @FXML private TableColumn<Candidat, Integer> colCandId;
    @FXML private TableColumn<Candidat, String>  colCandFullName;
    @FXML private TableColumn<Candidat, String>  colCandEmail;
    @FXML private TableColumn<Candidat, String>  colCandEducation;
    @FXML private TableColumn<Candidat, String>  colCandStatus;
    @FXML private TextField tfSearchCand;
    @FXML private Label     lblForJob;

    // ====== DETAILS ======
    @FXML private Label    dFullName;
    @FXML private Label    dEmail;
    @FXML private Label    dPhone;
    @FXML private Label    dEducation;
    @FXML private TextArea dSkills;

    private final JobPositionService jobService       = new JobPositionService();
    private final CandidatService    candidatService  = new CandidatService();

    private ObservableList<JobPosition> jobMaster       = FXCollections.observableArrayList();
    private ObservableList<Candidat>    allCandidats    = FXCollections.observableArrayList();
    private FilteredList<Candidat>      candidatFiltered;
    private JobPosition                 selectedJob     = null;
    private List<JobCardController>     jobCardControllers = new ArrayList<>();

    // ═══════════════════════════════════════════════════════════
    @FXML
    public void initialize() {
        System.out.println("=== Initialisation de manageRecruitment ===");
        initCandColumns();
        initSelectionListeners();
        loadAllCandidats();
        loadJobs();
        updateStats();
        updateCandidatsForSelectedJob();
        System.out.println("=== Initialisation terminée ===");
    }

    // ═══════════════════════════════════════════════════════════
    private void initCandColumns() {
        colCandId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCandEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colCandEducation.setCellValueFactory(new PropertyValueFactory<>("educationLevel"));
        colCandStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colCandFullName.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getFirstName() + " " + data.getValue().getLastName()));

        // Colonne ATS Score
        TableColumn<Candidat, String> colATSScore = new TableColumn<>("Score ATS");
        colATSScore.setCellValueFactory(data -> {
            double score = ATSService.calculateMatchScore(data.getValue(), selectedJob);
            return new SimpleStringProperty(String.format("%.0f%%", score));
        });
        candidatTable.getColumns().add(colATSScore);

        // Colorier les statuts (version compatible Java 8/11)
        colCandStatus.setCellFactory(column -> new TableCell<Candidat, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(status);
                String style;
                switch (status) {
                    case "NEW":
                        style = "-fx-text-fill: #0B63CE; -fx-font-weight: bold;";
                        break;
                    case "IN_REVIEW":
                        style = "-fx-text-fill: #f59e0b; -fx-font-weight: bold;";
                        break;
                    case "ACCEPTED":
                        style = "-fx-text-fill: #0FA36B; -fx-font-weight: bold;";
                        break;
                    case "REJECTED":
                        style = "-fx-text-fill: #ef4444; -fx-font-weight: bold;";
                        break;
                    default:
                        style = "";
                }
                setStyle(style);
            }
        });
    }

    // ═══════════════════════════════════════════════════════════
    @FXML
    private void onUploadCV() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/upload_cv.fxml"));
            Parent root = loader.load();
            UploadCVController controller = loader.getController();
            controller.setSelectedJob(selectedJob);
            Stage stage = new Stage();
            stage.setTitle("Upload et analyse de CV");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadAllCandidats();
            updateCandidatsForSelectedJob();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur lors de l'ouverture de la fenêtre d'upload: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════
    private void initSelectionListeners() {
        candidatTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldV, c) -> {
                    if (c == null) clearDetails();
                    else updateDetails(c);
                });
    }

    private void updateDetails(Candidat c) {
        if (c == null) return;
        dFullName.setText(c.getFirstName() + " " + c.getLastName());
        dEmail.setText(safe(c.getEmail()));
        dPhone.setText(String.valueOf(c.getPhone()));
        dEducation.setText(safe(c.getEducationLevel()));
        dSkills.setText(c.getSkills() == null ? "" : c.getSkills());
    }

    private void clearDetails() {
        dFullName.setText("—");
        dEmail.setText("—");
        dPhone.setText("—");
        dEducation.setText("—");
        dSkills.setText("");
    }

    // ═══════════════════════════════════════════════════════════
    private void loadJobs() {
        try {
            List<JobPosition> jobs = jobService.getAll();
            jobMaster.setAll(jobs);
            createJobCards();
            updateStats();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur lors du chargement des offres: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════
    private void createJobCards() {
        jobCardsContainer.getChildren().clear();
        jobCardControllers.clear();

        if (jobMaster.isEmpty()) {
            HBox noJobs = new HBox(12);
            noJobs.setAlignment(Pos.CENTER_LEFT);
            noJobs.setPrefHeight(70);
            noJobs.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12; " +
                    "-fx-border-color: #e2e8f0; -fx-border-radius: 12; -fx-border-width: 1; " +
                    "-fx-border-style: dashed; -fx-padding: 16 20;");
            Label icon  = new Label("💼"); icon.setStyle("-fx-font-size: 22;");
            Label msg   = new Label("Aucune offre disponible — cliquez sur « + Nouvelle offre » pour commencer");
            msg.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13;");
            noJobs.getChildren().addAll(icon, msg);
            jobCardsContainer.getChildren().add(noJobs);
            return;
        }

        for (JobPosition job : jobMaster) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/JobCard.fxml"));
                Parent card = loader.load();

                JobCardController ctrl = loader.getController();

                long count = allCandidats.stream()
                        .filter(c -> c.getJobPosition() != null &&
                                c.getJobPosition().getIdJob() == job.getIdJob())
                        .count();

                ctrl.setJobData(job, (int) count);
                ctrl.setClickListener(this::onJobSelected);

                if (selectedJob != null && selectedJob.getIdJob() == job.getIdJob())
                    ctrl.setSelected(true);

                jobCardControllers.add(ctrl);

                VBox.setMargin(card, new Insets(0));
                jobCardsContainer.getChildren().add(card);

            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Erreur lors de la création des cartes: " + e.getMessage());
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    private void onJobSelected(JobPosition job) {
        selectedJob = job;
        jobCardControllers.forEach(ctrl ->
                ctrl.setSelected(ctrl.getJob() != null && selectedJob != null &&
                        ctrl.getJob().getIdJob() == selectedJob.getIdJob()));
        lblForJob.setText(job != null
                ? "Candidats pour : " + job.getTitle()
                : "Sélectionnez une offre pour voir ses candidats");
        updateCandidatsForSelectedJob();
        candidatTable.getSelectionModel().clearSelection();
    }

    // ═══════════════════════════════════════════════════════════
    private void loadAllCandidats() {
        try {
            allCandidats.setAll(candidatService.getAll());
            updateStats();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur lors du chargement des candidats: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════
    private void updateStats() {
        if (statsTotalJobs  != null) statsTotalJobs.setText(String.valueOf(jobMaster.size()));
        if (statsTotalJobs2 != null) statsTotalJobs2.setText("(" + jobMaster.size() + ")");
        if (statsTotalCandidats != null) statsTotalCandidats.setText(String.valueOf(allCandidats.size()));
        if (statsOpenJobs != null)
            statsOpenJobs.setText(String.valueOf(
                    jobMaster.stream().filter(j -> "Open".equals(j.getStatus())).count()));
        if (statsInReview != null)
            statsInReview.setText(String.valueOf(
                    allCandidats.stream().filter(c -> "IN_REVIEW".equals(c.getStatus())).count()));
    }

    // ═══════════════════════════════════════════════════════════
    private void updateCandidatsForSelectedJob() {
        ObservableList<Candidat> filtered;
        if (selectedJob == null) {
            filtered = FXCollections.observableArrayList();
            if (lblForJob != null) lblForJob.setText("Sélectionnez une offre pour voir ses candidats");
        } else {
            List<Candidat> list = allCandidats.stream()
                    .filter(c -> c.getJobPosition() != null &&
                            c.getJobPosition().getIdJob() == selectedJob.getIdJob())
                    .collect(Collectors.toList());
            filtered = FXCollections.observableArrayList(list);
            if (lblForJob != null)
                lblForJob.setText(list.isEmpty()
                        ? "Aucun candidat pour : " + selectedJob.getTitle()
                        : "Candidats pour : " + selectedJob.getTitle() + " (" + list.size() + ")");
        }
        candidatFiltered = new FilteredList<>(filtered, p -> true);
        SortedList<Candidat> sorted = new SortedList<>(candidatFiltered);
        sorted.comparatorProperty().bind(candidatTable.comparatorProperty());
        candidatTable.setItems(sorted);
        setupSearch();
    }

    private void setupSearch() {
        if (tfSearchCand == null || candidatFiltered == null) return;
        tfSearchCand.textProperty().addListener((obs, o, newV) -> {
            String key = newV == null ? "" : newV.trim().toLowerCase();
            candidatFiltered.setPredicate(c -> {
                if (key.isEmpty()) return true;
                String fn    = safe(c.getFirstName()).toLowerCase();
                String ln    = safe(c.getLastName()).toLowerCase();
                String email = safe(c.getEmail()).toLowerCase();
                return fn.contains(key) || ln.contains(key) ||
                        (fn + " " + ln).contains(key) || email.contains(key);
            });
        });
    }

    @FXML private void onClearSearch() { tfSearchCand.clear(); }

    // ═══════════════════════════════════════════════════════════
    // ACTIONS JOBS
    // ═══════════════════════════════════════════════════════════
    @FXML private void onAddJob()  { openJobForm(null); }

    @FXML
    private void onEditJob() {
        if (selectedJob == null) { showAlert("Veuillez sélectionner une offre d'emploi."); return; }
        openJobForm(selectedJob);
    }

    @FXML
    private void onDeleteJob() {
        if (selectedJob == null) { showAlert("Veuillez sélectionner une offre d'emploi."); return; }
        boolean hasCandidates = allCandidats.stream()
                .anyMatch(c -> c.getJobPosition() != null &&
                        c.getJobPosition().getIdJob() == selectedJob.getIdJob());
        if (hasCandidates) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation");
            confirm.setHeaderText("Cette offre a des candidats associés");
            confirm.setContentText("La suppression dissociera les candidats. Continuer ?");
            if (confirm.showAndWait().get() != ButtonType.OK) return;
        }
        try {
            jobService.delete(selectedJob.getIdJob());
            selectedJob = null;
            loadJobs();
            loadAllCandidats();
            updateCandidatsForSelectedJob();
        } catch (Exception e) { showAlert("Erreur lors de la suppression: " + e.getMessage()); }
    }

    private void openJobForm(JobPosition job) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/job_form.fxml"));
            Parent root = loader.load();
            jobForm controller = loader.getController();
            if (job != null) controller.setJobToEdit(job);
            controller.setOnSaveCallback(() -> {
                loadJobs();
                loadAllCandidats();
                if (selectedJob != null) {
                    JobPosition refreshed = jobMaster.stream()
                            .filter(j -> j.getIdJob() == selectedJob.getIdJob())
                            .findFirst().orElse(null);
                    onJobSelected(refreshed);
                }
            });
            Stage stage = new Stage();
            stage.setTitle(job == null ? "Ajouter une offre" : "Modifier l'offre");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur lors de l'ouverture du formulaire: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ACTIONS CANDIDATS
    // ═══════════════════════════════════════════════════════════
    @FXML
    private void onAddCandidat() {
        if (selectedJob == null) { showAlert("Veuillez sélectionner une offre d'emploi d'abord."); return; }
        openCandidatForm(null, selectedJob);
    }

    @FXML
    private void onEditCandidat() {
        Candidat c = candidatTable.getSelectionModel().getSelectedItem();
        if (c == null) { showAlert("Veuillez sélectionner un candidat."); return; }
        openCandidatForm(c, c.getJobPosition());
    }

    @FXML
    private void onDeleteCandidat() {
        Candidat c = candidatTable.getSelectionModel().getSelectedItem();
        if (c == null) { showAlert("Veuillez sélectionner un candidat."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le candidat");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer ce candidat ?");
        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                candidatService.delete(c.getId());
                loadAllCandidats();
                updateCandidatsForSelectedJob();
            } catch (Exception e) { showAlert("Erreur lors de la suppression: " + e.getMessage()); }
        }
    }

    private void openCandidatForm(Candidat candidat, JobPosition job) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/candidat_form.fxml"));
            Parent root = loader.load();
            candidatForm controller = loader.getController();
            if (candidat != null) controller.setCandidatToEdit(candidat);
            else controller.setSelectedJob(job);
            Stage stage = new Stage();
            stage.setTitle(candidat == null ? "Ajouter un candidat" : "Modifier le candidat");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadAllCandidats();
            updateCandidatsForSelectedJob();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur lors de l'ouverture du formulaire: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════
    @FXML
    private void onUpdateStatus() {
        Candidat c = candidatTable.getSelectionModel().getSelectedItem();
        if (c == null) { showAlert("Veuillez sélectionner un candidat."); return; }
        ChoiceDialog<String> dialog = new ChoiceDialog<>(c.getStatus(),
                "NEW", "IN_REVIEW", "ACCEPTED", "REJECTED");
        dialog.setTitle("Mettre à jour le statut");
        dialog.setHeaderText("Changer le statut de " + c.getFirstName() + " " + c.getLastName());
        dialog.setContentText("Nouveau statut :");
        dialog.showAndWait().ifPresent(status -> {
            c.setStatus(status);
            candidatService.update(c);
            loadAllCandidats();
            updateCandidatsForSelectedJob();
            updateDetails(c);
        });
    }

    @FXML
    private void onSendEmail() {
        Candidat c = candidatTable.getSelectionModel().getSelectedItem();
        if (c == null) { showAlert("Veuillez sélectionner un candidat."); return; }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/email_form.fxml"));
            Parent root = loader.load();
            EmailController controller = loader.getController();
            controller.setCandidat(c);
            Stage stage = new Stage();
            stage.setTitle("Envoyer un email - " + c.getFirstName() + " " + c.getLastName());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void onViewCv() {
        Candidat c = candidatTable.getSelectionModel().getSelectedItem();
        if (c == null) { showAlert("Veuillez sélectionner un candidat."); return; }
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("CV - " + c.getFirstName() + " " + c.getLastName());
        info.setHeaderText("Compétences");
        info.setContentText(c.getSkills() != null ? c.getSkills() : "Aucune compétence renseignée");
        info.showAndWait();
    }

    @FXML
    private void onReject() {
        Candidat c = candidatTable.getSelectionModel().getSelectedItem();
        if (c == null) { showAlert("Veuillez sélectionner un candidat."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Rejeter la candidature");
        confirm.setHeaderText("Rejeter " + c.getFirstName() + " " + c.getLastName());
        confirm.setContentText("Êtes-vous sûr de vouloir rejeter ce candidat ?");
        if (confirm.showAndWait().get() == ButtonType.OK) {
            c.setStatus("REJECTED");
            candidatService.update(c);
            loadAllCandidats();
            updateCandidatsForSelectedJob();
            updateDetails(c);
        }
    }

    @FXML
    private void onExportAllCandidatsPDF() {
        if (allCandidats == null || allCandidats.isEmpty()) {
            showAlert("Aucun candidat à exporter.");
            return;
        }
        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer le rapport PDF");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        fc.setInitialFileName("candidats_" + LocalDate.now() + ".pdf");
        String home = System.getProperty("user.home");
        File dir = new File(home + "/Downloads");
        if (!dir.exists()) dir = new File(home + "/Documents");
        fc.setInitialDirectory(dir);
        File file = fc.showSaveDialog(candidatTable.getScene().getWindow());
        if (file != null) {
            try {
                PdfExportService.exportAllCandidats(new ArrayList<>(allCandidats), file.getAbsolutePath());
                showInfo("PDF exporté : " + file.getName());
                openFileLocation(file);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur export PDF : " + e.getMessage());
            }
        }
    }

    private void openFileLocation(File file) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("PDF créé");
        alert.setHeaderText("Le PDF a été créé avec succès !");
        alert.setContentText("Voulez-vous ouvrir le dossier contenant le fichier ?");
        ButtonType openBtn   = new ButtonType("Ouvrir le dossier");
        ButtonType closeBtn  = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(openBtn, closeBtn);
        alert.showAndWait().ifPresent(r -> {
            if (r == openBtn) {
                try { java.awt.Desktop.getDesktop().open(file.getParentFile()); }
                catch (Exception e) { showAlert("Impossible d'ouvrir le dossier : " + e.getMessage()); }
            }
        });
    }

    @FXML
    private void onOpenStatistiques() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/statistiques.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Tableau de bord statistiques");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setWidth(1200);
            stage.setHeight(800);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur lors de l'ouverture des statistiques: " + e.getMessage());
        }
    }

    @FXML private void onLogout() { System.exit(0); }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Attention"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Information"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    private String safe(String s) { return s != null ? s : ""; }
}