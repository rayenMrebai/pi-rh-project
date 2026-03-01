package org.example.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.model.recrutement.Candidat;
import org.example.model.recrutement.JobPosition;
import org.example.services.recrutement.CandidatService;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public class candidatForm {

    // ── Champs FXML ──
    @FXML private TextField     tfFirstName;
    @FXML private TextField     tfLastName;
    @FXML private TextField     tfEmail;
    @FXML private TextField     tfPhone;
    @FXML private TextField     tfEducationLevel;
    @FXML private TextArea      taSkills;
    @FXML private ComboBox<String> cbStatus;
    @FXML private Label         lblJobInfo;

    // ── Email vérification ──
    @FXML private Button btnVerifyEmail;
    @FXML private HBox   emailStatusBox;
    @FXML private Label  lblEmailIcon;
    @FXML private Label  lblEmailStatus;
    @FXML private Label  lblEmailDetail;

    // ── Boutons ──
    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    // ── Services & état ──
    private final CandidatService service = new CandidatService();
    private Candidat   candidatToEdit = null;
    private JobPosition selectedJob   = null;
    private boolean     emailVerified = false;

    // ══════════════════════════════════════════════════════════════
    // 🔑  Clé API AbstractAPI Email Validation
    //     Gratuit : 100 vérifications/jour
    //     Inscription : https://www.abstractapi.com/api/email-verification-validation-api
    //     Remplacez par votre clé personnelle ↓
    // ══════════════════════════════════════════════════════════════
    private static final String ABSTRACT_API_KEY = "dcda6220e779470e9fb8aa661690c3fd";
    // ✅ URL correcte confirmée depuis le dashboard
    private static final String ABSTRACT_API_URL =
            "https://emailreputation.abstractapi.com/v1/?api_key=%s&email=%s";

    // ══════════════════════════════════════════════════════════════
    @FXML
    public void initialize() {
        cbStatus.getItems().addAll("NEW", "IN_REVIEW", "ACCEPTED", "REJECTED");
        cbStatus.getSelectionModel().selectFirst();

        // Téléphone : chiffres uniquement
        tfPhone.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("\\d*") ? change : null));

        // Nom / Prénom : lettres uniquement
        onlyLetters(tfFirstName);
        onlyLetters(tfLastName);

        // Niveau d'études : alphanumérique
        tfEducationLevel.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("[a-zA-ZÀ-ÿ0-9 \\-+/]*") ? change : null));

        // Désactiver le bouton Vérifier si champ vide
        btnVerifyEmail.setDisable(true);
        tfEmail.textProperty().addListener((obs, oldVal, newVal) -> {
            emailVerified = false;
            resetEmailUI();
            btnVerifyEmail.setDisable(newVal == null || newVal.trim().isEmpty());
        });
    }

    // ══════════════════════════════════════════════════════════════
    // ✅ VÉRIFICATION EMAIL — appelée par le bouton "Vérifier"
    // ══════════════════════════════════════════════════════════════
    @FXML
    private void onVerifyEmail() {
        String email = tfEmail.getText().trim();
        if (email.isEmpty()) return;

        // 1. Vérification de format basique (rapide, sans API)
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            showEmailResult(false, "❌ Format invalide",
                    "L'adresse ne respecte pas le format attendu.", "#ef4444");
            return;
        }

        // 2. Appel API AbstractAPI (asynchrone)
        setVerifyingState(true);

        String url = String.format(ABSTRACT_API_URL, ABSTRACT_API_KEY,
                java.net.URLEncoder.encode(email, java.nio.charset.StandardCharsets.UTF_8));

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .timeout(Duration.ofSeconds(15))
                .build();

        System.out.println("🔍 Vérification email: " + email);

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> Platform.runLater(() -> {
                    setVerifyingState(false);
                    int code = response.statusCode();
                    System.out.println("📥 AbstractAPI HTTP " + code + " → " + response.body());

                    if (code == 200) {
                        parseAndDisplayResult(response.body(), email);
                    } else if (code == 401) {
                        // Clé API invalide ou non configurée → fallback validation locale
                        fallbackLocalValidation(email);
                    } else {
                        // Erreur réseau/API → fallback
                        fallbackLocalValidation(email);
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        setVerifyingState(false);
                        System.err.println("❌ Erreur réseau: " + ex.getMessage());
                        // Pas de réseau → fallback validation locale
                        fallbackLocalValidation(email);
                    });
                    return null;
                });
    }

    // ══════════════════════════════════════════════════════════════
    // Parser la réponse JSON de AbstractAPI
    // ══════════════════════════════════════════════════════════════
    private void parseAndDisplayResult(String json, String email) {
        try {
            // ══════════════════════════════════════════════════════
            // Structure réelle AbstractAPI Email Reputation :
            // {
            //   "email_deliverability": {
            //     "status": "deliverable",
            //     "is_format_valid": true,
            //     "is_smtp_valid": true,
            //     "is_mx_valid": true
            //   },
            //   "email_domain": { "domain": "gmail.com" },
            //   "email_quality": {
            //     "score": 0.95,
            //     "is_free_email": true,
            //     "is_disposable": false,
            //     "is_catchall": false
            //   },
            //   "email_risk": {
            //     "address_risk_status": "low"
            //   },
            //   "email_breaches": { "total_breaches": 1 },
            //   "email_sender": { "email_provider_name": "Google" }
            // }
            // ══════════════════════════════════════════════════════

            // --- email_deliverability ---
            String delivSection = getSection(json, "email_deliverability");
            String status       = getString(delivSection, "status");         // "deliverable" / "undeliverable" / "risky"
            boolean isFormatValid = getBoolDirect(delivSection, "is_format_valid");
            boolean isMxValid     = getBoolDirect(delivSection, "is_mx_valid");
            boolean isSmtpValid   = getBoolDirect(delivSection, "is_smtp_valid");

            // --- email_quality ---
            String qualSection  = getSection(json, "email_quality");
            boolean isDisposable = getBoolDirect(qualSection, "is_disposable");
            boolean isFreeEmail  = getBoolDirect(qualSection, "is_free_email");
            boolean isCatchAll   = getBoolDirect(qualSection, "is_catchall");
            String  scoreStr     = getString(qualSection, "score");

            // --- email_domain ---
            String domSection = getSection(json, "email_domain");
            String domain     = getString(domSection, "domain");

            // --- email_risk ---
            String riskSection      = getSection(json, "email_risk");
            String addressRisk      = getString(riskSection, "address_risk_status"); // "low"/"medium"/"high"

            // --- email_breaches ---
            String breachSection = getSection(json, "email_breaches");
            String totalBreaches = getString(breachSection, "total_breaches");
            int    breachCount   = 0;
            try { breachCount = (int) Double.parseDouble(totalBreaches); } catch (Exception ignored) {}

            // --- email_sender ---
            String senderSection = getSection(json, "email_sender");
            String provider      = getString(senderSection, "email_provider_name");

            System.out.println("  status=" + status + " format=" + isFormatValid
                    + " mx=" + isMxValid + " smtp=" + isSmtpValid
                    + " disposable=" + isDisposable + " risk=" + addressRisk
                    + " breaches=" + breachCount + " score=" + scoreStr);

            // ══════════════════════════════════════════════════════
            // LOGIQUE DE DÉCISION
            // ══════════════════════════════════════════════════════

            // 1. Format invalide
            if (!isFormatValid) {
                showEmailResult(false, "❌ Format invalide",
                        "Le format de l'adresse est incorrect.", "#ef4444");
                emailVerified = false;
                updateEmailFieldStyle(false);
                return;
            }

            // 2. Adresse jetable (disposable)
            if (isDisposable) {
                showEmailResult(false, "🚫 Adresse jetable détectée",
                        "Domaine: " + domain + " — Adresses temporaires non acceptées.", "#ef4444");
                emailVerified = false;
                updateEmailFieldStyle(false);
                return;
            }

            // 3. Domaine MX invalide
            if (!isMxValid) {
                showEmailResult(false, "❌ Domaine inexistant",
                        "Le domaine '" + domain + "' n'a pas de serveur mail.", "#ef4444");
                emailVerified = false;
                updateEmailFieldStyle(false);
                return;
            }

            // 4. Risque élevé
            if ("high".equals(addressRisk)) {
                showEmailResult(false, "⛔ Adresse à haut risque",
                        "Cette adresse est signalée comme risquée (@" + domain + ").", "#ef4444");
                emailVerified = false;
                updateEmailFieldStyle(false);
                return;
            }

            // 5. Email DELIVERABLE ✅
            if ("deliverable".equals(status)) {
                StringBuilder detail = new StringBuilder();
                detail.append("@").append(domain);
                if (!provider.isEmpty()) detail.append(" · ").append(provider);
                if (isFreeEmail)  detail.append(" · Email gratuit");
                if (isCatchAll)   detail.append(" · Catch-all");
                if (!scoreStr.isEmpty()) {
                    try {
                        double score = Double.parseDouble(scoreStr);
                        detail.append(" · Qualité: ").append(String.format("%.0f%%", score * 100));
                    } catch (Exception ignored) {}
                }
                // Avertissement breaches
                if (breachCount > 0) {
                    detail.append("\n⚠️ Cette adresse apparaît dans ")
                            .append(breachCount).append(" fuite(s) de données connue(s).");
                }

                showEmailResult(true, "✅ Email valide et actif", detail.toString(), "#0FA36B");
                emailVerified = true;
                updateEmailFieldStyle(true);

                // Popup si fuite de données détectée
                if (breachCount > 0) {
                    showBreachWarning(breachCount);
                }
                return;
            }

            // 6. Email RISKY → accepté avec avertissement orange
            if ("risky".equals(status) || "medium".equals(addressRisk)) {
                showEmailResult(true, "⚠️ Email accepté avec réserves",
                        "Risque modéré détecté sur @" + domain + ". Score: " + scoreStr, "#f59e0b");
                emailVerified = true;
                updateEmailFieldStyle(null);
                return;
            }

            // 7. UNDELIVERABLE ❌
            showEmailResult(false, "❌ Email invalide ou inexistant",
                    "Le serveur a rejeté cette adresse (@" + domain + ").", "#ef4444");
            emailVerified = false;
            updateEmailFieldStyle(false);

        } catch (Exception e) {
            e.printStackTrace();
            fallbackLocalValidation(email);
        }
    }

    /** Affiche un avertissement si l'email a été trouvé dans des fuites de données */
    private void showBreachWarning(int count) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("⚠️ Fuite de données détectée");
        alert.setHeaderText("Cet email apparaît dans " + count + " fuite(s) de données");
        alert.setContentText(
                "L'email a été compromis dans des fuites de données connues.\n" +
                        "Il reste utilisable, mais signalez-le au candidat.");
        alert.showAndWait();
    }

    // ══════════════════════════════════════════════════════════════
    // Fallback : validation locale si API indisponible
    // ══════════════════════════════════════════════════════════════
    private void fallbackLocalValidation(String email) {
        System.out.println("⚠️ Fallback validation locale pour: " + email);

        boolean formatOk = email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

        // Domaines jetables connus
        List<String> disposableDomains = List.of(
                "tempmail.com", "guerrillamail.com", "mailinator.com",
                "10minutemail.com", "throwaway.email", "yopmail.com",
                "maildrop.cc", "sharklasers.com", "dispostable.com",
                "trashmail.com", "fakeinbox.com", "spamgourmet.com"
        );

        String domain = email.contains("@") ? email.split("@")[1].toLowerCase() : "";
        boolean isDisposable = disposableDomains.contains(domain);

        if (!formatOk) {
            showEmailResult(false, "❌ Format invalide",
                    "Vérifiez le format de l'adresse.", "#ef4444");
            emailVerified = false;
        } else if (isDisposable) {
            showEmailResult(false, "🚫 Adresse jetable",
                    "Le domaine '" + domain + "' est connu comme temporaire.", "#ef4444");
            emailVerified = false;
        } else {
            showEmailResult(true, "✅ Format valide",
                    "Vérification API indisponible — format OK, domaine non confirmé.", "#f59e0b");
            emailVerified = true;
        }
        updateEmailFieldStyle(emailVerified);
    }

    // ══════════════════════════════════════════════════════════════
    // UI helpers
    // ══════════════════════════════════════════════════════════════

    /** Affiche le bloc statut email */
    private void showEmailResult(boolean valid, String status, String detail, String color) {
        emailStatusBox.setVisible(true);
        emailStatusBox.setManaged(true);
        lblEmailIcon.setText(valid ? "●" : "●");
        lblEmailIcon.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12;");
        lblEmailStatus.setText(status);
        lblEmailStatus.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 11; -fx-font-weight: bold;");

        if (!detail.isEmpty()) {
            lblEmailDetail.setText(detail);
            lblEmailDetail.setVisible(true);
            lblEmailDetail.setManaged(true);
        } else {
            lblEmailDetail.setVisible(false);
            lblEmailDetail.setManaged(false);
        }
    }

    /** Remet à zéro l'UI email */
    private void resetEmailUI() {
        emailStatusBox.setVisible(false);
        emailStatusBox.setManaged(false);
        lblEmailDetail.setVisible(false);
        lblEmailDetail.setManaged(false);
        tfEmail.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; " +
                "-fx-border-color: #e5e7eb; -fx-padding: 9;");
    }

    /** Colorie la bordure du champ email selon le résultat */
    private void updateEmailFieldStyle(Boolean valid) {
        String color = valid == null ? "#f59e0b" : (valid ? "#0FA36B" : "#ef4444");
        tfEmail.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; " +
                "-fx-border-color: " + color + "; -fx-border-width: 2; -fx-padding: 9;");
    }

    /** Active/désactive le bouton avec feedback "En cours..." */
    private void setVerifyingState(boolean verifying) {
        btnVerifyEmail.setDisable(verifying);
        btnVerifyEmail.setText(verifying ? "⏳" : "Vérifier");
    }

    // ══════════════════════════════════════════════════════════════
    // Helpers parsing JSON manuel (structure imbriquée AbstractAPI)
    // ══════════════════════════════════════════════════════════════

    /**
     * Extrait le contenu d'un objet JSON imbriqué.
     * Ex: getSection(json, "email_deliverability") →
     *   {"status":"deliverable","is_format_valid":true,...}
     */
    private String getSection(String json, String sectionKey) {
        try {
            int idx = json.indexOf("\"" + sectionKey + "\"");
            if (idx == -1) return "{}";
            int start = json.indexOf("{", idx);
            if (start == -1) return "{}";
            int depth = 0, end = -1;
            for (int i = start; i < json.length(); i++) {
                if (json.charAt(i) == '{') depth++;
                else if (json.charAt(i) == '}') {
                    depth--;
                    if (depth == 0) { end = i; break; }
                }
            }
            return end != -1 ? json.substring(start, end + 1) : "{}";
        } catch (Exception e) { return "{}"; }
    }

    /**
     * Extrait un boolean direct (pas encapsulé dans {"value": ...}).
     * Ex: "is_format_valid":true  →  true
     */
    private boolean getBoolDirect(String json, String key) {
        try {
            int idx = json.indexOf("\"" + key + "\"");
            if (idx == -1) return false;
            String sub = json.substring(idx + key.length() + 2).trim();
            if (sub.startsWith(":")) sub = sub.substring(1).trim();
            return sub.startsWith("true");
        } catch (Exception e) { return false; }
    }

    /**
     * Extrait un boolean encapsulé dans {"value": true/false}.
     * Ex: "is_valid_format":{"value":true}  →  true
     * (utilisé par l'ancien format AbstractAPI Email Validation)
     */
    private boolean getBool(String json, String key) {
        try {
            int idx = json.indexOf("\"" + key + "\"");
            if (idx == -1) return false;
            String sub = json.substring(idx);
            if (sub.contains("\"value\"")) {
                int vi = sub.indexOf("\"value\"");
                String val = sub.substring(vi + 7).replaceFirst("^\\s*:\\s*", "").trim();
                return val.startsWith("true");
            }
            String val = sub.substring(sub.indexOf(":") + 1).trim();
            return val.startsWith("true");
        } catch (Exception e) { return false; }
    }

    /**
     * Extrait une valeur string ou numérique.
     * Ex: "status":"deliverable"  →  "deliverable"
     *     "score":0.95            →  "0.95"
     *     "total_breaches":1      →  "1"
     */
    private String getString(String json, String key) {
        try {
            int idx = json.indexOf("\"" + key + "\"");
            if (idx == -1) return "";
            String sub = json.substring(idx + key.length() + 2).trim();
            if (sub.startsWith(":")) sub = sub.substring(1).trim();
            if (sub.startsWith("\"")) {
                // Valeur string entre guillemets
                int end = sub.indexOf("\"", 1);
                return end != -1 ? sub.substring(1, end) : "";
            } else {
                // Valeur numérique ou booléenne → lire jusqu'à , ou } ou ]
                int end = sub.length();
                for (int i = 0; i < sub.length(); i++) {
                    char c = sub.charAt(i);
                    if (c == ',' || c == '}' || c == ']' || c == '\n') { end = i; break; }
                }
                return sub.substring(0, end).trim();
            }
        } catch (Exception e) { return ""; }
    }

    // ══════════════════════════════════════════════════════════════
    // Helpers lettres uniquement
    // ══════════════════════════════════════════════════════════════
    private void onlyLetters(TextField tf) {
        tf.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("[a-zA-ZÀ-ÿ ]*") ? change : null));
    }

    // ══════════════════════════════════════════════════════════════
    // Setters appelés depuis le parent
    // ══════════════════════════════════════════════════════════════
    public void setSelectedJob(JobPosition job) {
        this.selectedJob = job;
        if (lblJobInfo != null && job != null)
            lblJobInfo.setText("Candidature pour : " + job.getTitle());
    }

    public void setCandidatToEdit(Candidat c) {
        this.candidatToEdit = c;
        this.selectedJob    = c.getJobPosition();
        tfFirstName.setText(c.getFirstName());
        tfLastName.setText(c.getLastName());
        tfEmail.setText(c.getEmail());
        tfPhone.setText(String.valueOf(c.getPhone()));
        tfEducationLevel.setText(c.getEducationLevel());
        taSkills.setText(c.getSkills());
        cbStatus.setValue(c.getStatus());
        emailVerified = true; // Déjà validé à la création

        // Afficher un badge vert discret pour l'email existant
        showEmailResult(true, "✅ Email existant", "", "#0FA36B");
        updateEmailFieldStyle(true);

        if (lblJobInfo != null && selectedJob != null)
            lblJobInfo.setText("Poste : " + selectedJob.getTitle());
    }

    // ══════════════════════════════════════════════════════════════
    // Upload CV
    // ══════════════════════════════════════════════════════════════
    @FXML
    private void onUploadCV() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/upload_cv.fxml"));
            Parent root = loader.load();
            UploadCVController controller = loader.getController();
            controller.setSelectedJob(selectedJob);
            controller.setOnCvExtractedListener(data -> {
                tfFirstName.setText(data.getFirstName());
                tfLastName.setText(data.getLastName());
                tfEmail.setText(data.getEmail());
                tfPhone.setText(data.getPhone());
                tfEducationLevel.setText(data.getEducationLevel());
                taSkills.setText(data.getSkills());
                cbStatus.setValue(data.getStatus() != null ? data.getStatus() : "NEW");
                // Réinitialiser la vérification email après remplissage auto
                emailVerified = false;
                resetEmailUI();
                btnVerifyEmail.setDisable(data.getEmail() == null || data.getEmail().isEmpty());
            });
            Stage stage = new Stage();
            stage.setTitle("Uploader un CV");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            alert("Erreur ouverture fenêtre upload : " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════
    // Sauvegarde
    // ══════════════════════════════════════════════════════════════
    @FXML
    private void onSave() {
        if (tfFirstName.getText().trim().isEmpty() || tfLastName.getText().trim().isEmpty()) {
            alert("Prénom et Nom sont obligatoires !");
            return;
        }
        if (tfEmail.getText().trim().isEmpty()) {
            alert("L'email est obligatoire !");
            return;
        }
        if (selectedJob == null && candidatToEdit == null) {
            alert("Aucun poste sélectionné pour ce candidat !");
            return;
        }

        // Avertir si email non vérifié (nouveau candidat uniquement)
        if (!emailVerified && candidatToEdit == null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Email non vérifié");
            confirm.setHeaderText("⚠️ L'email n'a pas été vérifié");
            confirm.setContentText("Voulez-vous quand même enregistrer ce candidat ?");
            confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) return;
        }

        int phone;
        try {
            phone = Integer.parseInt(tfPhone.getText().trim());
        } catch (Exception e) {
            alert("Le téléphone doit être un nombre !");
            return;
        }

        Candidat candidat = candidatToEdit != null ? candidatToEdit : new Candidat();
        candidat.setFirstName(tfFirstName.getText().trim());
        candidat.setLastName(tfLastName.getText().trim());
        candidat.setEmail(tfEmail.getText().trim());
        candidat.setPhone(phone);
        candidat.setEducationLevel(tfEducationLevel.getText().trim());
        candidat.setSkills(taSkills.getText().trim());
        candidat.setStatus(cbStatus.getValue());
        if (candidatToEdit == null) candidat.setJobPosition(selectedJob);

        // Vérification des doublons
        List<Candidat> duplicates = service.findDuplicates(candidat);
        if (!duplicates.isEmpty()) {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION);
            a.setTitle("Doublon potentiel");
            a.setHeaderText("Des candidats similaires existent :");
            StringBuilder sb = new StringBuilder();
            duplicates.forEach(d -> sb.append("• ").append(d.getFirstName())
                    .append(" ").append(d.getLastName())
                    .append(" (").append(d.getEmail()).append(")\n"));
            sb.append("\nEnregistrer quand même ?");
            a.setContentText(sb.toString());
            a.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            if (a.showAndWait().orElse(ButtonType.NO) == ButtonType.NO) return;
        }

        if (candidatToEdit == null) service.create(candidat);
        else service.update(candidatToEdit);

        close();
    }

    @FXML private void onCancel() { close(); }

    private void close() {
        Stage stage = (Stage) tfFirstName.getScene().getWindow();
        stage.close();
    }

    private void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
