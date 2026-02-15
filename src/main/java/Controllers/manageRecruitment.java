package Controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.model.recrutement.Candidat;
import org.example.model.recrutement.JobPosition;
import org.example.services.recrutement.CandidatService;
import org.example.services.recrutement.JobPositionService;

import java.io.IOException;
import java.util.List;

public class manageRecruitment {

    // ====== JOB TABLE ======
    @FXML private TableView<JobPosition> jobTable;
    @FXML private TableColumn<JobPosition, Integer> colJobId;
    @FXML private TableColumn<JobPosition, String> colJobTitle;
    @FXML private TableColumn<JobPosition, String> colJobDept;
    @FXML private TableColumn<JobPosition, String> colJobType;
    @FXML private TableColumn<JobPosition, String> colJobStatus;
    @FXML private TableColumn<JobPosition, Object> colJobPostedAt;

    // ====== CANDIDAT TABLE ======
    @FXML private TableView<Candidat> candidatTable;
    @FXML private TableColumn<Candidat, Integer> colCandId;
    @FXML private TableColumn<Candidat, String> colCandFullName;
    @FXML private TableColumn<Candidat, String> colCandEmail;
    @FXML private TableColumn<Candidat, String> colCandEducation;
    @FXML private TableColumn<Candidat, String> colCandStatus;

    // ====== DETAILS ======
    @FXML private Label dFullName;
    @FXML private Label dEmail;
    @FXML private Label dPhone;
    @FXML private Label dEducation;
    @FXML private TextArea dSkills;

    private final JobPositionService jobService = new JobPositionService();
    private final CandidatService candidatService = new CandidatService();

    @FXML
    public void initialize() {
        initJobColumns();
        initCandColumns();
        initSelectionListeners();

        loadJobs();
        loadCandidats();
    }

    private void initJobColumns() {
        colJobId.setCellValueFactory(new PropertyValueFactory<>("idJob"));
        colJobTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colJobDept.setCellValueFactory(new PropertyValueFactory<>("departement"));
        colJobType.setCellValueFactory(new PropertyValueFactory<>("employeeType"));
        colJobStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colJobPostedAt.setCellValueFactory(new PropertyValueFactory<>("postedAt"));
    }

    private void initCandColumns() {
        colCandId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCandEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colCandEducation.setCellValueFactory(new PropertyValueFactory<>("educationLevel"));
        colCandStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Full name = firstName + lastName
        colCandFullName.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getFirstName() + " " + data.getValue().getLastName()
                )
        );
    }

    private void initSelectionListeners() {
        candidatTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, c) -> {
            if (c == null) {
                clearDetails();
                return;
            }
            dFullName.setText(c.getFirstName() + " " + c.getLastName());
            dEmail.setText(c.getEmail());
            dPhone.setText(String.valueOf(c.getPhone()));
            dEducation.setText(c.getEducationLevel());
            dSkills.setText(c.getSkills() == null ? "" : c.getSkills());
        });
    }

    private void clearDetails() {
        dFullName.setText("-");
        dEmail.setText("-");
        dPhone.setText("-");
        dEducation.setText("-");
        dSkills.setText("");
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
    private void onEditJob() {
        JobPosition selected = jobTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select a job first.");
            return;
        }

        openPopupWithData("/job_form.fxml", "Edit Job", controller -> {
            if (controller instanceof jobForm j) {
                j.setJobToEdit(selected);
            }
        });

        loadJobs();
    }




    @FXML
    private void onDeleteJob() {
        JobPosition selected = jobTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select a job first.");
            return;
        }
        jobService.delete(selected.getIdJob());
        loadJobs();
    }




    // ================= CANDIDAT =================

    @FXML
    private void onAddCandidat() {
        openPopup("/candidat_form.fxml", "Add Candidate");
        loadCandidats();
    }

    @FXML
    private void onDeleteCandidat() {
        Candidat selected = candidatTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select a candidate first.");
            return;
        }
        candidatService.delete(selected.getId());
        loadCandidats();
    }

    @FXML
    private void onEditCandidat() {
        Candidat selected = candidatTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select a candidate first.");
            return;
        }

        openPopupWithData("/candidat_form.fxml", "Edit Candidate", controller -> {
            if (controller instanceof candidatForm cf) {
                cf.setCandidatToEdit(selected);
            }
        });

        loadCandidats();
    }

    // ================= ACTIONS =================
    @FXML private void onUpdateStatus() {}
    @FXML private void onViewCv() {}
    @FXML private void onReject() {}
    @FXML private void onLogout() { System.exit(0); }

    // ================= POPUP UTILS =================

    private void openPopup(String fxml, String title) {
        openPopupWithData(fxml, title, null);
    }

    private void openPopupWithData(String fxml, String title, ControllerConsumer consumer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            if (consumer != null) consumer.accept(loader.getController());

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Warning");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    @FunctionalInterface
    interface ControllerConsumer {
        void accept(Object controller);
    }
}
