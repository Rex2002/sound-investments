package UI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import dataRepo.Sonifiable;
import state.EventQueues;

// For a wonderful explanation of periodic tasks in javaFX see here:
// https://stackoverflow.com/a/60685975/13764271

public class App extends Application {
    // maintain a strong reference to the service
    private CheckEQService service;

    @Override
    public void start(Stage primaryStage) {
        try {
            service = new CheckEQService();
            service.setPeriod(Duration.millis(100));
            service.setOnSucceeded(e -> {
                List<Sonifiable> v = service.getValue();
                if (!v.isEmpty()) {
                    for (Sonifiable s : v) {
                        System.out.println(s);
                    }
                }
            });

            Parent root = FXMLLoader.load(getClass().getResource("MainScene.fxml"));
            Scene scene = new Scene(root);
            // scene.getStylesheets().add(getClass().getResource("Label.css").toExternalForm());
            String css = this.getClass().getResource("choice.css").toExternalForm();
            scene.getStylesheets().add(css);

            primaryStage.setScene(scene);
            primaryStage.show();

            service.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        launch(args);
    }

    private static class CheckEQService extends ScheduledService<List<Sonifiable>> {
        static List<Sonifiable> l = new ArrayList<>(10);

        @Override
        protected Task<List<Sonifiable>> createTask() {
            return new Task<>() {
                @Override
                protected List<Sonifiable> call() throws Exception {
                    l.clear();
                    while (!EventQueues.toUI.isEmpty())
                        l = EventQueues.toUI.poll();
                    return l;
                }
            };
        }
    }
}