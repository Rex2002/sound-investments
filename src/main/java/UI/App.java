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
        List<String> v = service.getValue();
        if (!v.isEmpty()) {
          for (String s : v) {
            System.out.print(s + " ");
          }
          System.out.print("\n");
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

  private static class CheckEQService extends ScheduledService<List<String>> {
    static List<String> l = new ArrayList<>(10);

    @Override
    protected Task<List<String>> createTask() {
      return new Task<>() {
        @Override
        protected List<String> call() throws Exception {
          l.clear();
          EventQueues.toUI.drainTo(l);
          return l;
        }
      };
    }
  }
}