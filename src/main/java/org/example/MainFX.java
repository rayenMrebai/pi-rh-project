package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {

    @Override
    public void start(Stage stage) {
        try {
            // Charger le fichier FXML principal
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SalaireManagement.fxml"));
            Parent root = loader.load();

            // Configurer la scène
            Scene scene = new Scene(root);

            // Configurer la fenêtre
            stage.setTitle("INTEGRA - Gestion des Salaires");
            stage.setScene(scene);
            stage.setMaximized(true); // Fenêtre maximisée
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur au chargement : " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
