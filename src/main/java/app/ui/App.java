package app.ui;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

// For a wonderful explanation of periodic tasks in javaFX see here:
// https://stackoverflow.com/a/60685975/13764271

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            double windowSizeFactor = 9d / 10d;

            primaryStage.setTitle("Sound Investments");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainScene.fxml"));
            Parent root = (Parent) loader.load();
            Scene scene = new Scene(root);
            String css = getClass().getResource("/choice.css").toExternalForm();
            scene.getStylesheets().add(css);
            primaryStage.setScene(scene);

            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            double width = bounds.getWidth() * windowSizeFactor;
            double height = bounds.getHeight() * windowSizeFactor;
            primaryStage.setWidth(width);
            primaryStage.setHeight(height);
            primaryStage.setX(bounds.getMinX() + bounds.getWidth() * (1 - windowSizeFactor)/2);
            primaryStage.setY(bounds.getMinY() + bounds.getHeight() * (1 - windowSizeFactor)/2);

            primaryStage.show();

            MainSceneController controller = (MainSceneController) loader.getController();
            controller.stage = primaryStage;
            controller.scene = scene;
            controller.window = scene.getWindow();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        launch(args);
    }
}