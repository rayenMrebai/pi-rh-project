package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        // ✅ Changer "ManageTraining" en "UserTrainingList" pour afficher la page user
        // ✅ Changer en "ManageTraining" pour revenir à la page admin
        Parent root = FXMLLoader.load(getClass().getResource("/UserTrainingList.fxml"));

        Scene scene = new Scene(root);

        primaryStage.setTitle("INTEGRA – Formations");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(620);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}