package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class MainFx extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        URL url = getClass().getResource("/manage_recruitment.fxml");
        if (url == null) {
            throw new RuntimeException(
                    "FXML introuvable: /manage_recruitment.fxml\n" +
                            "➡ Mets le fichier dans: src/main/resources/manage_recruitment.fxml\n" +
                            "➡ Et mark: src/main/resources as Resources Root"
            );
        }

        FXMLLoader loader = new FXMLLoader(url);
        Scene scene = new Scene(loader.load());

        stage.setTitle("INTEGRA - Recruitment");
        stage.setScene(scene);
        stage.setWidth(1200);
        stage.setHeight(700);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
