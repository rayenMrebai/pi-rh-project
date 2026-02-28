package Controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.stage.FileChooser;
import java.io.File;
import java.time.LocalDate;
import org.example.services.pdf.PdfExportService;
public class manageRecruitment {

    // ====== JOB CARDS ======
    @FXML private HBox jobCardsContainer;
    @FXML private Label statsTotalJobs;
    @FXML private Label statsTotalCandidats;
    @FXML private Label statsOpenJobs;
    @FXML private Label statsInReview;
    @FXML private Label statsTotalJobs2;

    // ====== CANDIDAT TABLE ======
    @FXML private TableView<Candidat> candidatTable;
    @FXML private TableColumn<Candidat, Integer> colCandId;
    @FXML private TableColumn<Candidat, String> colCandFullName;
    @FXML private TableColumn<Candidat, String> colCandEmail;
    @FXML private TableColumn<Candidat, String> colCandEducation;
    @FXML private TableColumn<Candidat, String> colCandStatus;
    @FXML private TextField tfSearchCand;
    @FXML private Label lblForJob;

    // ====== DETAILS ======
    @FXML private Label dFullName;
    @FXML private Label dEmail;
    @FXML private Label dPhone;
    @FXML private Label dEducation;
    @FXML private TextArea dSkills;

    private final JobPositionService jobService = new JobPositionService();
    private final CandidatService candidatService = new CandidatService();

    private ObservableList<JobPosition> jobMaster = FXCollections.observableArrayList();
    private ObservableList<Candidat> allCandidats = FXCollections.observableArrayList();
    private FilteredList<Candidat> candidatFiltered;
    private JobPosition selectedJob = null;
    private List<JobCardController> jobCardControllers = new ArrayList<>();

    @FXML
    public void initialize() {
        System.out.println("=== Initialisation de manageRecruitment ===");

        initCandColumns();
        initSelectionListeners();

        // Charger d'abord les candidats, puis les jobs
        loadAllCandidats();
        loadJobs();
        updateStats();

        // Initialement, désactiver les boutons candidats
        updateCandidatsForSelectedJob();

        System.out.println("=== Initialisation terminée ===");
    }

    private void initCandColumns() {
        colCandId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCandEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colCandEducation.setCellValueFactory(new PropertyValueFactory<>("educationLevel"));
        colCandStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colCandFullName.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getFirstName() + " " + data.getValue().getLastName()
                )
        );

        // Colorier les statuts
        colCandStatus.setCellFactory(column -> new TableCell<Candidat, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    switch (status) {
                        case "NEW":
                            setStyle("-fx-text-fill: #0B63CE; -fx-font-weight: bold;");
                            break;
                        case "IN_REVIEW":
                            setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                            break;
                        case "ACCEPTED":
                            setStyle("-fx-text-fill: #0FA36B; -fx-font-weight: bold;");
                            break;
                        case "REJECTED":
                            setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                            break;
                    }
                }
            }
        });
    }

    private void initSelectionListeners() {
        candidatTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, c) -> {
            if (c == null) {
                clearDetails();
                return;
            }
            updateDetails(c);
        });
    }

    private void updateDetails(Candidat c) {
        if (c != null) {
            dFullName.setText(c.getFirstName() + " " + c.getLastName());
            dEmail.setText(c.getEmail());
            dPhone.setText(String.valueOf(c.getPhone()));
            dEducation.setText(c.getEducationLevel());
            dSkills.setText(c.getSkills() == null ? "" : c.getSkills());
        }
    }
    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Information");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
    private void clearDetails() {
        dFullName.setText("-");
        dEmail.setText("-");
        dPhone.setText("-");
        dEducation.setText("-");
        dSkills.setText("");
    }

    private void loadJobs() {
        try {
            System.out.println("Chargement des jobs...");
            List<JobPosition> jobs = jobService.getAll();
            System.out.println("Jobs chargés: " + jobs.size());
            jobMaster.setAll(jobs);
            createJobCards();
            updateStats();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur lors du chargement des offres: " + e.getMessage());
        }
    }

    private void createJobCards() {
        System.out.println("Création des cartes d'emploi...");
        jobCardsContainer.getChildren().clear();
        jobCardControllers.clear();

        if (jobMaster.isEmpty()) {
            System.out.println("Aucun job trouvé, affichage du message");
            // Afficher un message s'il n'y a pas d'offres
            VBox noJobsBox = new VBox(15);
            noJobsBox.setAlignment(Pos.CENTER);
            noJobsBox.setPrefWidth(320);
            noJobsBox.setPrefHeight(380);
            noJobsBox.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 25; -fx-border-color: #e2e8f0; -fx-border-radius: 25; -fx-border-width: 2; -fx-border-style: dashed;");

            Label noJobsIcon = new Label("💼");
            noJobsIcon.setStyle("-fx-font-size: 64;");

            Label noJobsLabel = new Label("Aucune offre disponible");
            noJobsLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 16; -fx-font-weight: bold;");

            Label noJobsSubLabel = new Label("Cliquez sur '+ Nouvelle offre' pour créer une offre");
            noJobsSubLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12;");

            noJobsBox.getChildren().addAll(noJobsIcon, noJobsLabel, noJobsSubLabel);
            jobCardsContainer.getChildren().add(noJobsBox);
            return;
        }

        System.out.println("Création de " + jobMaster.size() + " cartes");

        for (JobPosition job : jobMaster) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/JobCard.fxml"));
                Parent card = loader.load();

                JobCardController controller = loader.getController();

                // Compter les candidats pour ce job
                long count = 0;
                if (allCandidats != null) {
                    count = allCandidats.stream()
                            .filter(c -> c.getJobPosition() != null &&
                                    c.getJobPosition().getIdJob() == job.getIdJob())
                            .count();
                }

                System.out.println("Carte créée pour: " + job.getTitle() + " (ID: " + job.getIdJob() + ", Candidats: " + count + ")");

                controller.setJobData(job, (int) count);
                controller.setClickListener(this::onJobSelected);

                // Vérifier si ce job est celui sélectionné
                if (selectedJob != null && selectedJob.getIdJob() == job.getIdJob()) {
                    System.out.println("Ce job est sélectionné: " + job.getTitle());
                    controller.setSelected(true);
                }

                jobCardControllers.add(controller);
                jobCardsContainer.getChildren().add(card);

                // Espacement entre les cartes
                HBox.setMargin(card, new Insets(5, 20, 5, 0));

            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Erreur lors de la création des cartes d'emploi: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur inattendue: " + e.getMessage());
            }
        }

        System.out.println("Nombre de cartes dans le conteneur: " + jobCardsContainer.getChildren().size());
    }

    private void onJobSelected(JobPosition job) {
        System.out.println("=== Job sélectionné ===");
        System.out.println("Job: " + (job != null ? job.getTitle() + " (ID: " + job.getIdJob() + ")" : "null"));

        // Mettre à jour le job sélectionné
        selectedJob = job;

        // Mettre à jour le style de toutes les cartes
        for (JobCardController controller : jobCardControllers) {
            if (controller.getJob() != null && selectedJob != null &&
                    controller.getJob().getIdJob() == selectedJob.getIdJob()) {
                controller.setSelected(true);
                System.out.println("Carte sélectionnée: " + controller.getJob().getTitle());
            } else {
                controller.setSelected(false);
            }
        }

        // Mettre à jour le label et la table des candidats
        if (job != null) {
            lblForJob.setText("Candidats pour: " + job.getTitle());
            System.out.println("Label mis à jour: Candidats pour: " + job.getTitle());
        } else {
            lblForJob.setText("Sélectionnez une offre pour voir ses candidats");
            System.out.println("Label mis à jour: Sélectionnez une offre");
        }

        updateCandidatsForSelectedJob();

        // Effacer la sélection du tableau
        candidatTable.getSelectionModel().clearSelection();
    }
    private void loadAllCandidats() {
        try {
            System.out.println("Chargement des candidats...");
            List<Candidat> candidats = candidatService.getAll();
            System.out.println("Candidats chargés: " + candidats.size());
            allCandidats.setAll(candidats);
            updateStats();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur lors du chargement des candidats: " + e.getMessage());
        }
    }
    @FXML
    private void onSendEmail() {
        Candidat selected = candidatTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Veuillez sélectionner un candidat.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/email_form.fxml"));
            Parent root = loader.load();

            EmailController controller = loader.getController();
            controller.setCandidat(selected);

            Stage stage = new Stage();
            stage.setTitle("Envoyer un email - " + selected.getFullName());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur lors de l'ouverture du formulaire d'email: " + e.getMessage());
        }
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
    private void updateStats() {
        if (statsTotalJobs != null) {
            statsTotalJobs.setText(String.valueOf(jobMaster.size()));
        }
        if (statsTotalJobs2 != null) {
            statsTotalJobs2.setText("(" + jobMaster.size() + ")");
        }
        if (statsTotalCandidats != null) {
            statsTotalCandidats.setText(String.valueOf(allCandidats.size()));
        }
        if (statsOpenJobs != null) {
            long openCount = jobMaster.stream()
                    .filter(j -> "Open".equals(j.getStatus()))
                    .count();
            statsOpenJobs.setText(String.valueOf(openCount));
        }
        if (statsInReview != null) {
            long reviewCount = allCandidats.stream()
                    .filter(c -> "IN_REVIEW".equals(c.getStatus()))
                    .count();
            statsInReview.setText(String.valueOf(reviewCount));
        }
    }
    @FXML
    private void onExportAllCandidatsPDF() {
        if (allCandidats == null || allCandidats.isEmpty()) {
            showAlert("Aucun candidat à exporter.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le rapport PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));

        // Définir un nom de fichier avec date
        String fileName = "candidats_" + LocalDate.now() + ".pdf";
        fileChooser.setInitialFileName(fileName);

        // Suggérer le dossier Téléchargements ou Documents par défaut
        String userHome = System.getProperty("user.home");
        File defaultDirectory = new File(userHome + "/Downloads");
        if (!defaultDirectory.exists()) {
            defaultDirectory = new File(userHome + "/Documents");
        }
        fileChooser.setInitialDirectory(defaultDirectory);

        File file = fileChooser.showSaveDialog(candidatTable.getScene().getWindow());

        if (file != null) {
            try {
                PdfExportService.exportAllCandidats(new ArrayList<>(allCandidats), file.getAbsolutePath());
                showInfo("PDF exporté avec succès : " + file.getName() + "\nEmplacement : " + file.getParent());

                // Optionnel : proposer d'ouvrir le dossier contenant le fichier
                openFileLocation(file);

            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur lors de l'export PDF : " + e.getMessage());
            }
        }
    }

    private void openFileLocation(File file) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("PDF créé");
        alert.setHeaderText("Le PDF a été créé avec succès !");
        alert.setContentText("Voulez-vous ouvrir le dossier contenant le fichier ?");

        ButtonType openButton = new ButtonType("Ouvrir le dossier");
        ButtonType cancelButton = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(openButton, cancelButton);

        alert.showAndWait().ifPresent(response -> {
            if (response == openButton) {
                try {
                    java.awt.Desktop.getDesktop().open(file.getParentFile());
                } catch (Exception e) {
                    showAlert("Impossible d'ouvrir le dossier : " + e.getMessage());
                }
            }
        });
    }
    private void updateCandidatsForSelectedJob() {
        ObservableList<Candidat> candidatsForJob;

        if (selectedJob == null) {
            candidatsForJob = FXCollections.observableArrayList();
            lblForJob.setText("Sélectionnez une offre pour voir ses candidats");
            System.out.println("Aucun job sélectionné, table des candidats vide");
        } else {
            List<Candidat> filteredList = allCandidats.stream()
                    .filter(c -> c.getJobPosition() != null &&
                            c.getJobPosition().getIdJob() == selectedJob.getIdJob())
                    .collect(Collectors.toList());

            candidatsForJob = FXCollections.observableArrayList(filteredList);

            System.out.println("Candidats pour le job " + selectedJob.getTitle() + ": " + filteredList.size());

            if (filteredList.isEmpty()) {
                lblForJob.setText("Aucun candidat pour: " + selectedJob.getTitle());
            } else {
                lblForJob.setText("Candidats pour: " + selectedJob.getTitle() + " (" + filteredList.size() + ")");
            }
        }

        candidatFiltered = new FilteredList<>(candidatsForJob, p -> true);
        SortedList<Candidat> sorted = new SortedList<>(candidatFiltered);
        sorted.comparatorProperty().bind(candidatTable.comparatorProperty());

        candidatTable.setItems(sorted);
        setupSearch();
    }

    private void setupSearch() {
        if (tfSearchCand == null || candidatFiltered == null) return;

        tfSearchCand.textProperty().addListener((obs, oldV, newV) -> {
            String key = (newV == null) ? "" : newV.trim().toLowerCase();

            candidatFiltered.setPredicate(c -> {
                if (key.isEmpty()) return true;

                String fn = (c.getFirstName() == null) ? "" : c.getFirstName().toLowerCase();
                String ln = (c.getLastName() == null) ? "" : c.getLastName().toLowerCase();
                String email = (c.getEmail() == null) ? "" : c.getEmail().toLowerCase();
                String full = (fn + " " + ln).trim();

                return fn.contains(key) || ln.contains(key) || full.contains(key) || email.contains(key);
            });
        });
    }

    @FXML
    private void onClearSearch() {
        tfSearchCand.clear();
    }

    // ================= JOB ACTIONS =================

    @FXML
    private void onAddJob() {
        openJobForm(null);
    }

    @FXML
    private void onEditJob() {
        if (selectedJob == null) {
            showAlert("Veuillez sélectionner une offre d'emploi.");
            return;
        }
        openJobForm(selectedJob);
    }

    @FXML
    private void onDeleteJob() {
        if (selectedJob == null) {
            showAlert("Veuillez sélectionner une offre d'emploi.");
            return;
        }

        boolean hasCandidates = allCandidats.stream()
                .anyMatch(c -> c.getJobPosition() != null &&
                        c.getJobPosition().getIdJob() == selectedJob.getIdJob());

        if (hasCandidates) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation");
            confirm.setHeaderText("Cette offre a des candidats associés");
            confirm.setContentText("La suppression de l'offre dissociera les candidats. Continuer ?");

            if (confirm.showAndWait().get() != ButtonType.OK) {
                return;
            }
        }

        try {
            jobService.delete(selectedJob.getIdJob());
            loadJobs();
            loadAllCandidats();
            selectedJob = null;
            updateCandidatsForSelectedJob();
        } catch (Exception e) {
            showAlert("Erreur lors de la suppression: " + e.getMessage());
        }
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
                    // Re-sélectionner le même job si possible
                    JobPosition refreshedJob = jobMaster.stream()
                            .filter(j -> j.getIdJob() == selectedJob.getIdJob())
                            .findFirst()
                            .orElse(null);
                    onJobSelected(refreshedJob);
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

    // ================= CANDIDAT ACTIONS =================

    @FXML
    private void onAddCandidat() {
        if (selectedJob == null) {
            showAlert("Veuillez sélectionner une offre d'emploi d'abord.");
            return;
        }
        openCandidatForm(null, selectedJob);
    }

    @FXML
    private void onEditCandidat() {
        Candidat selected = candidatTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Veuillez sélectionner un candidat.");
            return;
        }
        openCandidatForm(selected, selected.getJobPosition());
    }

    @FXML
    private void onDeleteCandidat() {
        Candidat selected = candidatTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Veuillez sélectionner un candidat.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le candidat");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer ce candidat ?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                candidatService.delete(selected.getId());
                loadAllCandidats();
                updateCandidatsForSelectedJob();
            } catch (Exception e) {
                showAlert("Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }

    private void openCandidatForm(Candidat candidat, JobPosition job) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/candidat_form.fxml"));
            Parent root = loader.load();

            candidatForm controller = loader.getController();

            if (candidat != null) {
                controller.setCandidatToEdit(candidat);
            } else {
                controller.setSelectedJob(job);
            }

            Stage stage = new Stage();
            stage.setTitle(candidat == null ? "Ajouter un candidat" : "Modifier le candidat");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Rafraîchir les données
            loadAllCandidats();
            updateCandidatsForSelectedJob();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur lors de l'ouverture du formulaire: " + e.getMessage());
        }
    }

    // ================= ACTIONS SUR LES CANDIDATS =================

    @FXML
    private void onUpdateStatus() {
        Candidat selected = candidatTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Veuillez sélectionner un candidat.");
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(selected.getStatus(),
                "NEW", "IN_REVIEW", "ACCEPTED", "REJECTED");
        dialog.setTitle("Mettre à jour le statut");
        dialog.setHeaderText("Changer le statut de " + selected.getFirstName() + " " + selected.getLastName());
        dialog.setContentText("Nouveau statut:");

        dialog.showAndWait().ifPresent(status -> {
            selected.setStatus(status);
            candidatService.update(selected);
            loadAllCandidats();
            updateCandidatsForSelectedJob();
            updateDetails(selected);
        });
    }

    @FXML
    private void onViewCv() {
        Candidat selected = candidatTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Veuillez sélectionner un candidat.");
            return;
        }

        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("CV - " + selected.getFullName());
        info.setHeaderText("Compétences du candidat");
        info.setContentText(selected.getSkills() != null ? selected.getSkills() : "Aucune compétence renseignée");
        info.showAndWait();
    }

    @FXML
    private void onReject() {
        Candidat selected = candidatTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Veuillez sélectionner un candidat.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Rejeter la candidature");
        confirm.setHeaderText("Rejeter " + selected.getFullName());
        confirm.setContentText("Êtes-vous sûr de vouloir rejeter ce candidat ?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            selected.setStatus("REJECTED");
            candidatService.update(selected);
            loadAllCandidats();
            updateCandidatsForSelectedJob();
            updateDetails(selected);
        }
    }

    @FXML
    private void onLogout() {
        System.exit(0);
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Attention");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}