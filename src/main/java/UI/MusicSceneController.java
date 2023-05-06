package UI;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.Node;
import javafx.stage.Stage;

public class MusicSceneController implements Initializable {

    @FXML
    private TextField headerTitle;
    private Stage stage;
    private Scene scene;
    private Parent root;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
       
    }
    public void switchToMainScene(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("MainScene.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
       }

}
