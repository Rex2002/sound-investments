package UI;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
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
   @FXML
    public  void addToCheckList(){
        CheckBox cBox = new CheckBox("Hi");
       cBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
         @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            if(newValue){
                addToPaneBox(cBox.getText());

            }else{
                
            }
        }
          });
        checkVBox.setPrefHeight((checkVBox.getChildren().size())*74.0);
        checkVBox.getChildren().add(cBox);
    }

    @FXML
    public void addToPaneBox(String txt){
        paneBox.getChildren().add(createSharePane(txt));
        paneBox.setPrefHeight((paneBox.getChildren().size())*477.0);
    }
    private Pane createSharePane(String name){
        Pane examplePane = new Pane();
        examplePane.setId("expPane" );
        TextField tField = new TextField();
        tField.setText(name);
        tField.setId("txtField");
        examplePane.getChildren().add(tField);
        Label pLabel = new Label();
        pLabel.setId("paneShareLabel");
        pLabel.setText("Price");
        pLabel.setLayoutX(26);
        pLabel.setLayoutY(77);
        Label tLBLabel = new Label();
        tLBLabel.setId("paneShareLabel");
        tLBLabel.setText("Trend Line Break");
        tLBLabel.setLayoutX(20);
        tLBLabel.setLayoutY(206);
        examplePane.getChildren().add(tLBLabel);
        Label dLabel = new Label();
        dLabel.setId("paneShareLabel");
        dLabel.setText("Derivate");
        dLabel.setLayoutX(20);
        dLabel.setLayoutY(326);
        ChoiceBox pChoiceBox = new ChoiceBox<>();
        pChoiceBox.getItems().addAll(prices);
        pChoiceBox.setLayoutX(20);
        pChoiceBox.setLayoutY(135);
        ChoiceBox tLBChoiceBox = new ChoiceBox<>();
        tLBChoiceBox.getItems().addAll(trends);
        tLBChoiceBox.setLayoutX(20);
        tLBChoiceBox.setLayoutY(262);
        ChoiceBox dChoiceBox = new ChoiceBox<>();
        dChoiceBox.getItems().addAll(derivate);
        dChoiceBox.setLayoutX(20);
        dChoiceBox.setLayoutY(377);
        examplePane.getChildren().add(pChoiceBox);
        examplePane.getChildren().add(tLBChoiceBox);
        examplePane.getChildren().add(dChoiceBox);
        examplePane.getChildren().add(dLabel);
        examplePane.getChildren().add(pLabel);
        return examplePane;
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
               
