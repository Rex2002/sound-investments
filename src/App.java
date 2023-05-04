import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
 
public class App extends Application {
    @Override
    public void start(Stage primaryStage) {
      
  try{
  Parent root = FXMLLoader.load(getClass().getResource("MainScene.fxml"));
  Scene scene = new Scene(root);
  //scene.getStylesheets().add(getClass().getResource("Label.css").toExternalForm());
    String css = this.getClass().getResource("choice.css").toExternalForm();
    scene.getStylesheets().add(css);

      primaryStage.setScene(scene);
       primaryStage.show();
  }
  catch(IOException e){
    e.printStackTrace();
  }
  
    }
 
 public static void main(String[] args) {
        launch(args);
    }
}