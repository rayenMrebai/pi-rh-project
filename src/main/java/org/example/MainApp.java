package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // ✅ Charger la page d'accueil (Home.fxml) en premier
        Parent root = FXMLLoader.load(getClass().getResource("/Home.fxml"));

        Scene scene = new Scene(root);
        primaryStage.setTitle("Système de Gestion RH - Accueil");
        primaryStage.setScene(scene);
        primaryStage.show();  // Fenêtre normale
        // primaryStage.setMaximized(true);  // Décommentez pour ouvrir en plein écran
    }

    public static void main(String[] args) {
        launch(args);
    }
}