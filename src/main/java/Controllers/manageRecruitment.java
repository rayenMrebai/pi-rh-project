package Controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.model.recrutement.Candidat;
import org.example.model.recrutement.JobPosition;
import org.example.services.recrutement.CandidatService;
import org.example.services.recrutement.JobPositionService;

import java.io.IOException;
import java.util.List;

public class manageRecruitment {

    @FXML private TableView<JobPosition> jobTable;
    @FXML private TableView<Candidat> candidatTable;

    // Colonnes JOB
    @FXML private TableColumn<JobPosition, Integer> colJobId;
    @FXML private TableColumn<JobPosition, String> colJobTitle;
    @FXML private TableColumn<JobPosition, String> colJobDept;
    @FXML private TableColumn<JobPosition, String> colJobType;
    @FXML private TableColumn<JobPosition, String> colJobStatus;
    @FXML private TableColumn<JobPosition, String> colJobPostedAt;

    // Colonnes CANDIDAT
    @FXML private TableColumn<Candidat, Integer> colCandId;
    @FXML private TableColumn<Candidat, String> colCandFullName;
    @FXML private TableColumn<Candidat, String> colCandEmail;
    @FXML private TableColumn<Candidat, String> colCandEducation;
    @FXML private TableColumn<Candidat, String> colCandStatus;

    // Details
    @FXML private Label dFullName;
    @FXML private Label dEmail;
    @FXML private Label dPhone;
    @FXML private Label dEducation;
    @FXML private TextArea dSkills;

    private final JobPositionService jobService = new JobPositionService();
    private final CandidatService candidatService = new CandidatService();

    @FXML
    public void initialize() {

        // Liaison colonnes (adapte si tes getters ont un autre nom)
        colJobId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("idJob"));
        colJobTitle.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("title"));
        colJobDept.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("department"));
        colJobType.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("type"));
        colJobStatus.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("status"));
        colJobPostedAt.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("postedAt"));

        colCandId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        colCandFullName.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("fullName"));
        colCandEmail.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("email"));
        colCandEducation.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("education"));
        colCandStatus.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("status"));

        loadJobs();
        loadCandidats();
    }

    private void loadJobs() {
        List<JobPosition> jobs = jobService.getAll();
        jobTable.setItems(FXCollections.observableArrayList(jobs));
    }

    private void loadCandidats() {
        List<Candidat> candidats = candidatService.getAll();
        candidatTable.setItems(FXCollections.observableArrayList(candidats));
    }

    // ================= JOB =================

    @FXML
    private void onAddJob() {
        openPopup("/job_form.fxml", "Add Job");
        loadJobs();
    }

    @FXML
    private void onDeleteJob() {
        JobPosition selected = jobTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            jobService.delete(selected.getIdJob());
            loadJobs();
        }
    }

    @FXML
    private void onEditJob() {
        System.out.println("Edit Job clicked");
    }

    // ================= CANDIDAT =================

    @FXML
    private void onAddCandidat() {
        openPopup("/candidat_form.fxml", "Add Candidat");
        loadCandidats();
    }

    @FXML
    private void onDeleteCandidat() {
        Candidat selected = candidatTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            candidatService.delete(selected.getId());
            loadCandidats();
        }
    }

    @FXML
    private void onEditCandidat() {
        System.out.println("Edit Candidat clicked");
    }

    // ================= DETAILS ACTIONS =================

    @FXML
    private void onUpdateStatus() {
        System.out.println("Update status clicked");
    }

    @FXML
    private void onViewCv() {
        System.out.println("View CV clicked");
    }

    @FXML
    private void onReject() {
        System.out.println("Reject clicked");
    }

    @FXML
    private void onLogout() {
        System.exit(0);
    }

    // ================= UTILS =================

    private void openPopup(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
