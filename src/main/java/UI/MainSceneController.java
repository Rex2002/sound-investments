package UI;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;

public class MainSceneController implements Initializable {

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
    void btnOkClicked(ActionEvent event) {
    }

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

}