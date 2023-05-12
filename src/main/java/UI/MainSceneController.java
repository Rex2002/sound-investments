package UI;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import dataRepo.Sonifiable;
import state.EventQueues;

public class MainSceneController implements Initializable {
    private CheckEQService service;

    // WARNING: Kommentare werden noch normalisiert
    @FXML
    private Button startBtn;
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
    private ArrayList<String> shareCheckName = new ArrayList<>();
    // @FXML
    // private HashMap<String, String[]> shareSettingList = new HashMap<String,
    // String[]>(); Wharscheinlich Löschen
    @FXML
    private String[][] setArray = new String[10][4];
    @FXML
    private int countArray = 0;
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
    @FXML
    private int counter = 1;

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) { // Initialisierung mit den Optionen
        categorieChoice.getItems().addAll(categories);
        locationChoice.getItems().addAll(locations);

        service = new CheckEQService();
        service.setPeriod(Duration.millis(100));
        service.setOnSucceeded(e -> {
            List<Sonifiable> v = service.getValue();
            if (!v.isEmpty()) {
                for (Sonifiable s : v) {
                    addToCheckList(s.getName());
                }
            }
        });
        service.start();
    }

    @FXML
    public void switchToMusicScene(ActionEvent event) throws IOException { // Wechsel auf die Music Scene
        root = FXMLLoader.load(getClass().getResource("MusicScene.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void clearCheckList() {
        checkVBox.getChildren().removeAll(); // Die Checkliste Liste clearen, NOCH TESTEN
    }

    public void addToCheckList(String name) {
        if (checkVBox.getChildren().size() < counter * 10) {
            CheckBox cBox = new CheckBox(name); // API Text Holen Was bei neu laden einfach weiter hinten abfragen
                                                // könnten die Size als indicator nehmen
            if (shareCheckName.contains(cBox.getText())) {
                cBox.setSelected(true);
            }
            cBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (newValue) {
                        if (paneBox.getChildren().size() < 10) {
                            addToPaneBox(cBox.getText());
                            shareCheckName.add(cBox.getText());
                        } else {
                            cBox.setSelected(false);
                        }

                    } else {
                        int i = shareCheckName.indexOf(cBox.getText());
                        shareCheckName.remove(cBox.getText());
                        paneBox.getChildren().remove(i);
                        paneBox.prefHeight(paneBox.getChildren().size() * 477.0);
                    }
                }
            });
            checkVBox.setPrefHeight((checkVBox.getChildren().size()) * 74.0);
            checkVBox.getChildren().add(cBox);
        } else {
            Button loadBtn = new Button("Nächste laden");
            loadBtn.setOnAction(event -> {
                loadNew();
            });
            loadBtn.setId("loadBtn");
            checkVBox.setPrefHeight((checkVBox.getChildren().size()) * 74.0);
            checkVBox.getChildren().add(loadBtn);
        }
    }

    private void loadNew() { // Nachladen der Aktien
        counter++; // Notfallplan: clearCheckList und dann einfac alle neu laden -> belastend NOCH
                   // TESTEN
        addToCheckList("Hi");
    }

    @FXML
    public void addToPaneBox(String txt) {
        paneBox.getChildren().add(createSharePane(txt));
        paneBox.setPrefHeight((paneBox.getChildren().size()) * 477.0);
    }

    private Pane createSharePane(String name) { // geht das irgdwie hübscher ? Wahrscheinlich in CSS Auslagern Teile
                                                // Davon
        Pane examplePane = new Pane();
        examplePane.setId("expPane");
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
        pChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                while (setArray[countArray][0] != tField.getText()) {
                    countArray++;
                }
                setArray[countArray][1] = (String) pChoiceBox.getSelectionModel().getSelectedItem();
                countArray = 0;
            }
        });
        ChoiceBox tLBChoiceBox = new ChoiceBox<>();
        tLBChoiceBox.getItems().addAll(trends);
        tLBChoiceBox.setLayoutX(20);
        tLBChoiceBox.setLayoutY(262);
        tLBChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                while (setArray[countArray][0] != tField.getText()) {
                    countArray++;
                }
                setArray[countArray][2] = (String) tLBChoiceBox.getSelectionModel().getSelectedItem();
                countArray = 0;
            }
        });
        ChoiceBox dChoiceBox = new ChoiceBox<>();
        dChoiceBox.getItems().addAll(derivate);
        dChoiceBox.setLayoutX(20);
        dChoiceBox.setLayoutY(377);
        dChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                while (setArray[countArray][0] != tField.getText()) {
                    countArray++;
                }
                setArray[countArray][3] = (String) dChoiceBox.getSelectionModel().getSelectedItem();
                countArray = 0;
            }
        });
        examplePane.getChildren().add(pChoiceBox);
        examplePane.getChildren().add(tLBChoiceBox);
        examplePane.getChildren().add(dChoiceBox);
        examplePane.getChildren().add(dLabel);
        examplePane.getChildren().add(pLabel);
        return examplePane;
    }

    LocalDate minDateStart = LocalDate.of(2023, 4, 16);
    LocalDate maxDateStart = LocalDate.now();

    private void updateStartPicker(){   //Datum Blockers WARNING: Müsen schauen dass wir die angegebenen Daten bei änderung der Aktien überprüfen
    startPicker.setDayCellFactory(d ->
            new DateCell() {
               @Override public void updateItem(LocalDate item, boolean empty) {
                      super.updateItem(item, empty);
                   setDisable(item.isAfter(maxDateStart) || item.isBefore(minDateStart));
                  }});
                }
    LocalDate minDateEnd = LocalDate.of(2023, 4, 16);
    LocalDate maxDateEnd = LocalDate.now();

    private void updateEndPicker() { // Datum Blockers
        endPicker.setDayCellFactory(d -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setDisable(item.isAfter(maxDateEnd) || item.isBefore(minDateEnd));
            }
        });
    }

    public void enableBtn() {
        int[] checkArray = new int[10];
        startBtn.setDisable(false);
        for (int x = 0; x < 10; x++) {
            if (setArray[x][0] != null) {
                checkArray[countArray] = x;
                countArray++;
            }
        }
        if (countArray == 0) {
            startBtn.setDisable(true);
        } else {
            for (int p = 0; p < countArray; p++) {
                for (int c = 1; c < 4; c++) {
                    if (setArray[p][c] == null) {
                        startBtn.setDisable(true);
                        break;
                    }
                }
            }
        }
        if (endPicker.getValue() == null || startPicker.getValue() == null) {
            startBtn.setDisable(true);
        }

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
