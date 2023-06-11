package dhbw.si.app.ui;

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
            primaryStage.setTitle("Sound Investments");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UI/MainScene.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            String css = getClass().getResource("/UI/choice.css").toExternalForm();
            scene.getStylesheets().add(css);
            primaryStage.setScene(scene);

            primaryStage.show();

            MainSceneController controller = loader.getController();
            controller.scene = scene;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        launch(args);
    }
}