package org.example.Controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.example.enums.SalaireStatus;
import org.example.services.excel.ExcelExportService;
import org.example.services.excel.ExportFilter;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ExportExcelDialogController {

    // Période
    @FXML private RadioButton rbTous;
    @FXML private RadioButton rbMois;
    @FXML private RadioButton rbAnnee;
    @FXML private RadioButton rbPersonnalisee;
    @FXML private ToggleGroup periodeGroup;

    @FXML private ComboBox<String> cmbMois;
    @FXML private ComboBox<Integer> cmbAnneeMois;
    @FXML private ComboBox<Integer> cmbAnnee;
    @FXML private DatePicker dpDebut;
    @FXML private DatePicker dpFin;

    // Filtres
    @FXML private CheckBox chkPaye;
    @FXML private CheckBox chkEnCours;
    @FXML private CheckBox chkCree;
    @FXML private CheckBox chkFilterMontant;
    @FXML private TextField txtMontantMin;

    // Options
    @FXML private CheckBox chkStatistiques;
    @FXML private CheckBox chkBonus;
    @FXML private CheckBox chkFormatage;

    // Fichier
    @FXML private TextField txtFilename;

    // Boutons
    @FXML private Button btnAnnuler;
    @FXML private Button btnExporter;

    private ExcelExportService excelService;
    private String defaultSavePath = "C:\\Users\\MSI\\Downloads\\pi_test";

    @FXML
    public void initialize() {
        excelService = new ExcelExportService();

        setupComboBoxes();
        setupListeners();
        updateFilename();
    }

    /**
     * Configure les ComboBox
     */
    private void setupComboBoxes() {
        // Mois
        cmbMois.setItems(FXCollections.observableArrayList(
                "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
        ));
        cmbMois.getSelectionModel().select(LocalDate.now().getMonthValue() - 1);

        // Années
        int currentYear = LocalDate.now().getYear();
        List<Integer> years = new ArrayList<>();
        for (int i = currentYear - 5; i <= currentYear + 1; i++) {
            years.add(i);
        }

        cmbAnneeMois.setItems(FXCollections.observableArrayList(years));
        cmbAnneeMois.getSelectionModel().select((Integer) currentYear);

        cmbAnnee.setItems(FXCollections.observableArrayList(years));
        cmbAnnee.getSelectionModel().select((Integer) currentYear);

        // DatePickers
        dpDebut.setValue(LocalDate.now().withDayOfMonth(1));
        dpFin.setValue(LocalDate.now());
    }

    /**
     * Configure les listeners
     */
    private void setupListeners() {
        // Activer/désactiver les contrôles selon le radio button sélectionné
        periodeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                boolean isMois = newVal == rbMois;
                boolean isAnnee = newVal == rbAnnee;
                boolean isPerso = newVal == rbPersonnalisee;

                cmbMois.setDisable(!isMois);
                cmbAnneeMois.setDisable(!isMois);
                cmbAnnee.setDisable(!isAnnee);
                dpDebut.setDisable(!isPerso);
                dpFin.setDisable(!isPerso);

                updateFilename();
            }
        });

        // Listener pour mise à jour automatique du nom de fichier
        cmbMois.setOnAction(e -> updateFilename());
        cmbAnneeMois.setOnAction(e -> updateFilename());
        cmbAnnee.setOnAction(e -> updateFilename());
        dpDebut.setOnAction(e -> updateFilename());
        dpFin.setOnAction(e -> updateFilename());

        // Activer/désactiver le champ montant
        chkFilterMontant.selectedProperty().addListener((obs, oldVal, newVal) -> {
            txtMontantMin.setDisable(!newVal);
        });

        // Validation montant minimum (uniquement nombres)
        txtMontantMin.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                txtMontantMin.setText(oldVal);
            }
        });
    }

    /**
     * Met à jour automatiquement le nom du fichier selon la période
     */
    private void updateFilename() {
        StringBuilder filename = new StringBuilder("salaires_");

        if (rbMois.isSelected() && cmbMois.getValue() != null && cmbAnneeMois.getValue() != null) {
            String mois = cmbMois.getValue().toLowerCase()
                    .replace("é", "e")
                    .replace("û", "u")
                    .replace("ô", "o");
            filename.append(mois).append("_").append(cmbAnneeMois.getValue());
        }
        else if (rbAnnee.isSelected() && cmbAnnee.getValue() != null) {
            filename.append(cmbAnnee.getValue());
        }
        else if (rbPersonnalisee.isSelected() && dpDebut.getValue() != null && dpFin.getValue() != null) {
            filename.append(dpDebut.getValue().toString())
                    .append("_au_")
                    .append(dpFin.getValue().toString());
        }
        else {
            filename.append("complet_").append(LocalDate.now().toString());
        }

        filename.append(".xlsx");
        txtFilename.setText(filename.toString());
    }

    /**
     * Gère le clic sur Exporter
     */
    @FXML
    private void handleExporter() {
        try {
            // Validation
            if (!validateInputs()) {
                return;
            }

            // ⭐ Choix du répertoire de sauvegarde
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Choisir le dossier de sauvegarde");

            // Répertoire par défaut
            File defaultDir = new File(defaultSavePath);
            if (defaultDir.exists()) {
                directoryChooser.setInitialDirectory(defaultDir);
            } else {
                directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            }

            Stage stage = (Stage) btnExporter.getScene().getWindow();
            File selectedDirectory = directoryChooser.showDialog(stage);

            if (selectedDirectory == null) {
                // Utilisateur a annulé
                return;
            }

            // Créer le filtre
            ExportFilter filter = createFilter();

            // Exporter
            String filePath = excelService.exportToExcel(filter, selectedDirectory.getAbsolutePath());

            if (filePath != null) {
                showAlert(
                        "Succès",
                        "Export Excel réussi !\n\nFichier sauvegardé :\n" + filePath,
                        Alert.AlertType.INFORMATION
                );
                closeDialog();
            } else {
                showAlert(
                        "Erreur",
                        "Aucune donnée à exporter avec les filtres sélectionnés.",
                        Alert.AlertType.WARNING
                );
            }

        } catch (Exception e) {
            showAlert(
                    "Erreur",
                    "Erreur lors de l'export : " + e.getMessage(),
                    Alert.AlertType.ERROR
            );
            e.printStackTrace();
        }
    }

    /**
     * Crée l'objet ExportFilter à partir des sélections de l'interface
     */
    private ExportFilter createFilter() {
        ExportFilter filter = new ExportFilter();

        // Période
        if (rbTous.isSelected()) {
            filter.setPeriodeType("TOUS");
        }
        else if (rbMois.isSelected()) {
            filter.setPeriodeType("MOIS");
            filter.setMois(cmbMois.getSelectionModel().getSelectedIndex() + 1);
            filter.setAnnee(cmbAnneeMois.getValue());
        }
        else if (rbAnnee.isSelected()) {
            filter.setPeriodeType("ANNEE");
            filter.setAnnee(cmbAnnee.getValue());
        }
        else if (rbPersonnalisee.isSelected()) {
            filter.setPeriodeType("PERSONNALISEE");
            filter.setDateDebut(dpDebut.getValue());
            filter.setDateFin(dpFin.getValue());
        }

        // Filtres statut
        List<SalaireStatus> statusList = new ArrayList<>();
        if (chkPaye.isSelected()) statusList.add(SalaireStatus.PAYÉ);
        if (chkEnCours.isSelected()) statusList.add(SalaireStatus.EN_COURS);
        if (chkCree.isSelected()) statusList.add(SalaireStatus.CREÉ);

        if (!statusList.isEmpty()) {
            filter.setStatusFilters(statusList);
        }

        // Filtre montant
        if (chkFilterMontant.isSelected() && !txtMontantMin.getText().isEmpty()) {
            try {
                double montant = Double.parseDouble(txtMontantMin.getText());
                filter.setMontantMin(montant);
            } catch (NumberFormatException e) {
                // Ignorer si invalide
            }
        }

        // Options
        filter.setInclureStatistiques(chkStatistiques != null && chkStatistiques.isSelected());
        filter.setInclureBonus(chkBonus != null && chkBonus.isSelected());
        filter.setAppliquerFormatage(chkFormatage != null && chkFormatage.isSelected());

        return filter;
    }

    /**
     * Valide les entrées
     */
    private boolean validateInputs() {
        // Vérifier que le nom du fichier n'est pas vide
        if (txtFilename.getText().trim().isEmpty()) {
            showAlert("Validation", "Le nom du fichier est obligatoire", Alert.AlertType.WARNING);
            return false;
        }

        // Vérifier période personnalisée
        if (rbPersonnalisee.isSelected()) {
            if (dpDebut.getValue() == null || dpFin.getValue() == null) {
                showAlert("Validation", "Veuillez sélectionner les deux dates", Alert.AlertType.WARNING);
                return false;
            }
            if (dpDebut.getValue().isAfter(dpFin.getValue())) {
                showAlert("Validation", "La date de début doit être avant la date de fin", Alert.AlertType.WARNING);
                return false;
            }
        }

        // Vérifier qu'au moins un statut est sélectionné
        if (!chkPaye.isSelected() && !chkEnCours.isSelected() && !chkCree.isSelected()) {
            showAlert("Validation", "Veuillez sélectionner au moins un statut", Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    /**
     * Gère le clic sur Annuler
     */
    @FXML
    private void handleAnnuler() {
        closeDialog();
    }

    /**
     * Ferme la fenêtre de dialogue
     */
    private void closeDialog() {
        Stage stage = (Stage) btnAnnuler.getScene().getWindow();
        stage.close();
    }

    /**
     * Affiche une alerte
     */
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}