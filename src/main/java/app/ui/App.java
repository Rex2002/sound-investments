package app.ui;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

// For a wonderful explanation of periodic tasks in javaFX see here:
// https://stackoverflow.com/a/60685975/13764271

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/UI/MainScene.fxml"));
            Scene scene = new Scene(root);
            // scene.getStylesheets().add(getClass().getResource("/UI/Label.css").toExternalForm());
            String css = getClass().getResource("/UI/choice.css").toExternalForm();
            scene.getStylesheets().add(css);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        launch(args);
    }
}