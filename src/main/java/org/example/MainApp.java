package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Charger la vue principale (Liste des compétences)
        Parent root = FXMLLoader.load(getClass().getResource("/ListSkills.fxml"));

        Scene scene = new Scene(root);
        primaryStage.setTitle("Système de Gestion RH - Compétences");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}