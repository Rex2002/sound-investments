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
    private VBox instBox;
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
                       for(int i = 0; i<10; i++){
                        if(setArray[i][0] == cBox.getText()){
                            setArray[i][0] = null;
                            setArray[i][1] = null;
                            setArray[i][2] = null;
                            enableBtn();
                            setArray[i][3] = null;
                        }
                       }
                        int i = shareCheckName.indexOf(cBox.getText());
                        shareCheckName.remove(cBox.getText());
                        paneBox.getChildren().remove(i);
                        paneBox.prefHeight(paneBox.getChildren().size() * 477.0);
                    }
                }
            });
            checkVBox.setPrefHeight((checkVBox.getChildren().size()) * 74.0);
            checkVBox.getChildren().add(cBox);
         if(checkVBox.getChildren().size() == 10){
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
    public void addInstrumenet(){    
        instBox.getChildren().add(createInstPane("test"));
        instBox.setPrefHeight(((instBox.getChildren().size())+1) * 685.0);
    }
    public Pane createInstPane(String name){
        Pane instPane = new Pane();
        instPane.setId("instPane");
        TextField txtField = new TextField();
        txtField.setText(name);
        txtField.setId("txtField");
        instPane.getChildren().add(txtField);
        Label vLabel = new Label();
        vLabel.setId("paneShareLabel");
        vLabel.setText("Volume");
        vLabel.setLayoutX(14);
        vLabel.setLayoutY(87.2);
        instPane.getChildren().add(vLabel);
        Label pitLabel = new Label();
        pitLabel.setId("paneShareLabel");
        pitLabel.setText("Pitch");
        pitLabel.setLayoutX(14);
        pitLabel.setLayoutY(199.2);
        instPane.getChildren().add(pitLabel);
        Label echoLabel = new Label();
        echoLabel.setId("paneShareLabel");
        echoLabel.setText("Echo");
        echoLabel.setLayoutX(14);
        echoLabel.setLayoutY(311.2);
        instPane.getChildren().add(echoLabel);
        Label highLabel = new Label();
        highLabel.setId("paneShareLabel");
        highLabel.setText("High-");
        highLabel.setLayoutX(14);
        highLabel.setLayoutY(407.2);
        instPane.getChildren().add(highLabel);
        Label passLabel = new Label();
        passLabel.setId("paneShareLabel");
        passLabel.setText("pass");
        passLabel.setLayoutX(14);
        passLabel.setLayoutY(455.2);
        instPane.getChildren().add(passLabel);
        ChoiceBox v1ChoiceBox = new ChoiceBox<>();
        v1ChoiceBox.setId("instChoice");
        v1ChoiceBox.getItems().addAll(prices);
        v1ChoiceBox.setLayoutX(122.4);
        v1ChoiceBox.setLayoutY(59.2);
        v1ChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                //make something
            }
        });
        ChoiceBox v2ChoiceBox = new ChoiceBox<>();
        v2ChoiceBox.setId("instChoice");
        v2ChoiceBox.getItems().addAll(prices);
        v2ChoiceBox.setLayoutX(122.4);
        v2ChoiceBox.setLayoutY(112.8);
        v2ChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                //make something
            }
        });
        ChoiceBox p1ChoiceBox = new ChoiceBox<>();
        p1ChoiceBox.setId("instChoice");
        p1ChoiceBox.getItems().addAll(prices);
        p1ChoiceBox.setLayoutX(122.4);
        p1ChoiceBox.setLayoutY(173.6);
        p1ChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                //make something
            }
        });
        ChoiceBox p2ChoiceBox = new ChoiceBox<>();
        p2ChoiceBox.setId("instChoice");
        p2ChoiceBox.getItems().addAll(prices);
        p2ChoiceBox.setLayoutX(122.4);
        p2ChoiceBox.setLayoutY(227.2);
        p2ChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                //make something
            }
        });
        ChoiceBox e1ChoiceBox = new ChoiceBox<>();
        e1ChoiceBox.setId("instChoice");
        e1ChoiceBox.getItems().addAll(prices);
        e1ChoiceBox.setLayoutX(122.4);
        e1ChoiceBox.setLayoutY(288);
        e1ChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                //make something
            }
        });
        ChoiceBox e2ChoiceBox = new ChoiceBox<>();
        e2ChoiceBox.setId("instChoice");
        e2ChoiceBox.getItems().addAll(prices);
        e2ChoiceBox.setLayoutX(122.4);
        e2ChoiceBox.setLayoutY(341.6);
        e2ChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                //make something
            }
        });
        ChoiceBox h1ChoiceBox = new ChoiceBox<>();
        h1ChoiceBox.setId("instChoice");
        h1ChoiceBox.getItems().addAll(prices);
        h1ChoiceBox.setLayoutX(122.4);
        h1ChoiceBox.setLayoutY(402.4);
        v1ChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                //make something
            }
        });
        ChoiceBox h2ChoiceBox = new ChoiceBox<>();
        h2ChoiceBox.setId("instChoice");
        h2ChoiceBox.getItems().addAll(prices);
        h2ChoiceBox.setLayoutX(122.4);
        h2ChoiceBox.setLayoutY(456);
        h2ChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                //make something
            }
        });
        instPane.getChildren().add(p1ChoiceBox);
        instPane.getChildren().add(p2ChoiceBox);
        instPane.getChildren().add(e1ChoiceBox);
        instPane.getChildren().add(e2ChoiceBox);
        instPane.getChildren().add(h1ChoiceBox);
        instPane.getChildren().add(h2ChoiceBox);
        instPane.getChildren().add(v1ChoiceBox);
        instPane.getChildren().add(v2ChoiceBox);
        return instPane;
    }
    @FXML
    public void addToPaneBox(String txt) {
        paneBox.getChildren().add(createSharePane(txt));
        paneBox.setPrefHeight((paneBox.getChildren().size()) * 477.0);
    }

    private Pane createSharePane(String name) { 
        for(int x= 0; x<10; x++){
            if(setArray[x][0]== null){
                setArray[x][0] = name;
                break;
            }
        }
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
        pLabel.setLayoutY(61.6);
        Label tLBLabel = new Label();
        tLBLabel.setId("paneShareLabel");
        tLBLabel.setText("Trend Line Break");
        tLBLabel.setLayoutX(16);
        tLBLabel.setLayoutY(164.8);
        examplePane.getChildren().add(tLBLabel);
        Label dLabel = new Label();
        dLabel.setId("paneShareLabel");
        dLabel.setText("Derivate");
        dLabel.setLayoutX(16);
        dLabel.setLayoutY(260.8);
        ChoiceBox pChoiceBox = new ChoiceBox<>();
        pChoiceBox.getItems().addAll(prices);
        pChoiceBox.setLayoutX(16);
        pChoiceBox.setLayoutY(135);
        pChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                while (setArray[countArray][0] != name) {
                    countArray++;
                }
                setArray[countArray][1] = (String) pChoiceBox.getValue().toString();
                countArray = 0;
                enableBtn();
            }
        });
        ChoiceBox tLBChoiceBox = new ChoiceBox<>();
        tLBChoiceBox.getItems().addAll(trends);
        tLBChoiceBox.setLayoutX(16);
        tLBChoiceBox.setLayoutY(209.6);
        tLBChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                while (setArray[countArray][0] != name) {
                    countArray++;
                }
                setArray[countArray][2] = (String) tLBChoiceBox.getValue().toString();
                countArray = 0;
                enableBtn();
            }
        });
        ChoiceBox dChoiceBox = new ChoiceBox<>();
        dChoiceBox.getItems().addAll(derivate);
        dChoiceBox.setLayoutX(16);
        dChoiceBox.setLayoutY(301.6);
        dChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                while (setArray[countArray][0] != name) {
                    countArray++;
                }
                setArray[countArray][3] = dChoiceBox.getValue().toString(); // Klappt nicht so wie es soll
                countArray = 0;
                enableBtn();
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
        System.out.println("countArray"+ countArray);
        if (countArray == 0) {
            startBtn.setDisable(true);
        } else {
            for (int p = 0; p < countArray; p++) {
                System.out.println(countArray);
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
        countArray = 0;
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
