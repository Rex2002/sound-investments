package UI;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class MainSceneController implements Initializable {

    @FXML
    private VBox paneBox;
    @FXML
    private Label headerTitle;
    @FXML
    private ChoiceBox<String> categorieChoice;
    @FXML
    private ChoiceBox<String> locationChoice;
    @FXML
    private ChoiceBox<String> priceChoice;
    @FXML
    private ChoiceBox<String> trendLineBreaksChoice;
    @FXML
    private ChoiceBox<String> derivateChoice;
    @FXML
    private Pane sharePane;
    @FXML
    private DatePicker startPicker;
    @FXML
    private DatePicker endPicker;
    @FXML
    private VBox checkVBox;
    @FXML
    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private String[] categories = { "Option 1", "Option 2" };
    @FXML
    private String[] locations = { "Option 1", "Option 2" };
    @FXML
    private String[] prices = { "Option 1", "Option 2" };
    @FXML
    private String[] trends = { "Option 1", "Option 2" };
    @FXML
    private String[] derivate = { "Option 1", "Option 2" };
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        categorieChoice.getItems().addAll(categories);
        locationChoice.getItems().addAll(locations);
        priceChoice.getItems().addAll(prices);
        trendLineBreaksChoice.getItems().addAll(trends);
        derivateChoice.getItems().addAll(derivate);
    }
    @FXML
    public void switchToMusicScene(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("MusicScene.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    public void addToCheckList(){
        CheckBox cBox = new CheckBox("Hi");
    
        checkVBox.setPrefHeight((checkVBox.getChildren().size())*74.0);
        checkVBox.getChildren().add(cBox);
    }
    @FXML
    public void addToPaneBox(){
        Pane examplePane = new Pane();
        examplePane.getChildren().addAll(sharePane.getChildren());
        examplePane.setPrefHeight(466);
        examplePane.setPrefWidth(350);
        examplePane.setBackground(sharePane.getBackground());
        paneBox.setPrefHeight((paneBox.getChildren().size())*477.0);
        paneBox.getChildren().add(examplePane);
    }
    
    LocalDate minDateStart = LocalDate.of(2023, 4, 16);
    LocalDate maxDateStart = LocalDate.now();
    private void updateStartPicker(){
    startPicker.setDayCellFactory(d ->
            new DateCell() {
               @Override public void updateItem(LocalDate item, boolean empty) {
                      super.updateItem(item, empty);
                   setDisable(item.isAfter(maxDateStart) || item.isBefore(minDateStart));
                  }});
                }
    LocalDate minDateEnd = LocalDate.of(2023, 4, 16);
    LocalDate maxDateEnd = LocalDate.now();
    private void updateEndPicker(){
    endPicker.setDayCellFactory(d ->
            new DateCell() {
               @Override public void updateItem(LocalDate item, boolean empty) {
                      super.updateItem(item, empty);
                   setDisable(item.isAfter(maxDateEnd) || item.isBefore(minDateEnd));
                  }});
                }  
    
    }
               
