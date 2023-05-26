package app.ui;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.groupdocs.metadata.internal.c.a.pd.internal.html.dom.canvas.le;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.util.Duration;

import app.communication.EventQueues;
import app.communication.Msg;
import app.communication.MsgToSMType;
import app.communication.MsgToUIType;
import app.communication.SonifiableFilter;
import dataRepo.Sonifiable;
import dataRepo.DataRepo.FilterFlag;

public class MainSceneController implements Initializable {
    // WARNING: Kommentare werden noch normalisiert
    @FXML
    private Label inst1EcLabel;
    @FXML
    private Label inst1VoLabel;
    @FXML
    private Label inst1PiLabel;
    @FXML
    private Label inst1HiLabel;
    @FXML
    private ChoiceBox inst1EcChoice;
    @FXML
    private ChoiceBox inst1VoChoice;
    @FXML
    private ChoiceBox inst1PiChoice;
    @FXML
    private ChoiceBox inst1HiChoice;
    @FXML
    private AnchorPane anchor;
    @FXML
    private TextField searchBar;
    @FXML
    private Button startBtn;
    @FXML
    private VBox paneBoxSonifiables;
    @FXML
    private VBox instBox;
    @FXML
    private Label headerTitle;
    @FXML
    private Label instPitchShare;
    @FXML
    private ChoiceBox<String> categorieChoice;
    @FXML
    private ChoiceBox<String> locationChoice;
    @FXML
    private ChoiceBox<String> delayReverbChoice;
    @FXML
    private ChoiceBox<String> feedbackReverbChoice;
    @FXML
    private ChoiceBox<String> cutoffChoice;
    @FXML
    private ChoiceBox<String> filterChoice;
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

    private CheckEQService checkEQService;
    private ArrayList<String> shareCheckName = new ArrayList<>();
    private String[][] setArray = new String[10][4];
    private int countArray = 0;
    private Stage stage;
    private Scene scene;
    private Parent root;

    private static String[] categoryKeys = { "Alle Kategorien", "Aktien", "ETFs", "Indizes" };
    private static FilterFlag[] categoryValues = { FilterFlag.ALL, FilterFlag.STOCK, FilterFlag.ETF, FilterFlag.INDEX };
    static {
        assert categoryKeys.length == categoryValues.length : "categoryKeys & categoryValues are not in sync";
    }

    private String[] locations = { "Deutschland" };
    private String[] prices = { "Option 1", "Option 2" };
    private String[] trends = { "Option 1", "Option 2" };
    private String[] derivate = { "Option 1", "Option 2" };

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) { // Initialisierung mit den Optionen
        categorieChoice.getItems().addAll(MainSceneController.categoryKeys);
        locationChoice.getItems().addAll(locations);
        // priceChoice.getItems().addAll(prices);
        // trendLineBreaksChoice.getItems().addAll(trends);
        // derivateChoice.getItems().addAll(derivate);
        enableBtn();

        checkEQService = new CheckEQService();
        checkEQService.setPeriod(Duration.millis(100));
        checkEQService.setOnSucceeded((event) -> {
            List<Msg<MsgToUIType>> messages = checkEQService.getValue();
            for (Msg<MsgToUIType> msg : messages) {
                switch (msg.type) {
                    case FILTERED_SONIFIABLES -> {
                        clearCheckList();
                        List<Sonifiable> sonifiables = (List<Sonifiable>) msg.data;
                        for (Sonifiable s : sonifiables) {
                            addToCheckList(s.getName());
                        }
                    }
                    default -> System.out.println("ERROR: Msg-Type " + msg.type + " not yet implemented");
                }
            }
        });
        displayError("Testing", "Test");
        checkEQService.start();

        categorieChoice.getSelectionModel().selectedIndexProperty().addListener((observable, oldIdx, newIdx) -> {
            EventQueues.toSM.add(new Msg<>(MsgToSMType.FILTERED_SONIFIABLES,
                    new SonifiableFilter(searchBar.getText(), categoryValues[(int) newIdx])));
        });
        categorieChoice.getSelectionModel().selectFirst();

        searchBar.textProperty().addListener((observable, oldVal, newVal) -> {
            EventQueues.toSM.add(new Msg<>(MsgToSMType.FILTERED_SONIFIABLES, new SonifiableFilter(newVal,
                    categoryValues[categorieChoice.getSelectionModel().getSelectedIndex()])));
        });
    }

    @FXML
    public void switchToMusicScene(ActionEvent event) throws IOException { // Wechsel auf die Music Scene
        root = FXMLLoader.load(getClass().getResource("/MusicScene.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);

        stage.show();
    }

    @FXML
    private void displayError(String errorMessage, String errorTitle) {
        Pane errorPane = new Pane();
        errorPane.setId("errorPane");
        errorPane.setLayoutX(542);
        errorPane.setLayoutY(200);
        errorPane.setPrefHeight(500);
        errorPane.toFront();
        errorPane.setPrefWidth(500);
        Label errorMes = new Label(errorMessage);
        errorMes.setId("errorMessage");
        errorMes.setLayoutY(50);
        errorMes.setLayoutX(125);
        Label errorTit = new Label(errorTitle);
        errorTit.setId("errorTitle");
        errorTit.setLayoutY(20);
        errorTit.setLayoutX(20);
        Button close = new Button("Schließen");
        close.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                anchor.getChildren().remove(errorPane);
            }
        });
        close.setLayoutX(330);
        close.setLayoutY(20);
        close.setId("closeBtn");
        errorPane.getChildren().addAll(errorMes, close, errorTit);
        anchor.getChildren().add(errorPane);

    }

    void clearCheckList() {
        checkVBox.getChildren().clear();
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
                    if (paneBoxSonifiables.getChildren().size() < 10) {
                        addToPaneBox(cBox.getText());
                        shareCheckName.add(cBox.getText());
                    } else {
                        cBox.setSelected(false);
                    }

                } else {
                    for (int i = 0; i < 10; i++) {
                        if (setArray[i][0] == cBox.getText()) {
                            setArray[i][0] = null;
                            setArray[i][1] = null;
                            setArray[i][2] = null;
                            enableBtn();
                            setArray[i][3] = null;
                        }
                    }
                    int i = shareCheckName.indexOf(cBox.getText());
                    shareCheckName.remove(cBox.getText());
                    paneBoxSonifiables.getChildren().remove(i);
                    paneBoxSonifiables.prefHeight(paneBoxSonifiables.getChildren().size() * 477.0);
                }
            }
        });
        checkVBox.setPrefHeight((checkVBox.getChildren().size()) * 74.0);
        checkVBox.getChildren().add(cBox);
        if (checkVBox.getChildren().size() == 10) {
            Button loadBtn = new Button("Nächste laden");
            loadBtn.setOnAction(event -> {
                loadNew();
            });
            loadBtn.setId("loadBtn");
            checkVBox.setPrefHeight((checkVBox.getChildren().size()) * 74.0);
            checkVBox.getChildren().add(loadBtn);
        }
    }

    private void loadNew() {
        // Reload sonif i ables
    }

    @FXML
    public void addToPaneBox(String txt) { // add a Sharepanel to the Panel Box if there are less than 10 Sharepanel
        paneBoxSonifiables.getChildren().add(createSharePane(txt));
        paneBoxSonifiables.setPrefHeight((paneBoxSonifiables.getChildren().size()) * 800.0);
    }

    private Pane createSharePane(String name) { // initialize and dek the Share Pane
        for (int x = 0; x < 10; x++) {
            if (setArray[x][0] == null) {
                setArray[x][0] = name;
                break;
            }
        }
        Pane examplePane = new Pane();
        examplePane.setId("expPane");
        TextField tField = new TextField();
        tField.setText(name);
        tField.setId("txtField");
        tField.setLayoutX(168);
        tField.setLayoutY(8);
        examplePane.getChildren().add(tField);
        Line topLine = new Line();
        topLine.setLayoutX(174);
        topLine.setLayoutY(53);
        topLine.setStartX(0);
        topLine.setStartY(0);
        topLine.setEndX(391);
        topLine.setEndY(0);
        examplePane.getChildren().add(topLine);
        Line leftLine = new Line();
        leftLine.setLayoutX(306);
        leftLine.setLayoutY(168);
        leftLine.setId("pinkLine");
        leftLine.setStartX(-100);
        leftLine.setStartY(-60);
        leftLine.setEndX(-100);
        leftLine.setEndY(263);
        examplePane.getChildren().add(leftLine);
        Line rightLine = new Line();
        rightLine.setLayoutX(512);
        rightLine.setLayoutY(177);
        rightLine.setId("pinkLine");
        rightLine.setStartX(-100);
        rightLine.setStartY(-60);
        rightLine.setEndX(-100);
        rightLine.setEndY(263);
        examplePane.getChildren().add(rightLine);
        Label pLabel = new Label();
        pLabel.setId("paneShareLabel");
        pLabel.setText("Price");
        pLabel.setLayoutX(14);
        pLabel.setLayoutY(80);
        Label tLBLabel = new Label();
        tLBLabel.setId("paneShareLabel");
        tLBLabel.setText("Trend Line Break");
        tLBLabel.setLayoutX(14);
        tLBLabel.setLayoutY(215);
        examplePane.getChildren().add(tLBLabel);
        Label dLabel = new Label();
        dLabel.setId("paneShareLabel");
        dLabel.setText("Derivate");
        dLabel.setLayoutX(14);
        dLabel.setLayoutY(350);

        Label flagLabel = new Label();
        flagLabel.setId("paneShareLabel");
        flagLabel.setText("Flag");
        flagLabel.setLayoutX(226);
        flagLabel.setLayoutY(80);
        examplePane.getChildren().add(flagLabel);
        Label triLabel = new Label();
        triLabel.setId("paneShareLabel");
        triLabel.setText("Triangle");
        triLabel.setLayoutX(226);
        triLabel.setLayoutY(215);
        examplePane.getChildren().add(triLabel);
        Label VformLabel = new Label();
        VformLabel.setId("paneShareLabel");
        VformLabel.setText("Vform");
        VformLabel.setLayoutX(226);
        VformLabel.setLayoutY(350);
        examplePane.getChildren().add(VformLabel);

        Label trendLabel = new Label();
        trendLabel.setId("paneShareLabel");
        trendLabel.setText("Trend-");
        trendLabel.setLayoutX(422);
        trendLabel.setLayoutY(80);
        examplePane.getChildren().add(trendLabel);
        Label breakLabel = new Label();
        breakLabel.setId("paneShareLabel");
        breakLabel.setText("break");
        breakLabel.setLayoutX(422);
        breakLabel.setLayoutY(119);
        examplePane.getChildren().add(breakLabel);
        Label movinLabel = new Label();
        movinLabel.setId("paneShareLabel");
        movinLabel.setText("Movin");
        movinLabel.setLayoutX(422);
        movinLabel.setLayoutY(203);
        examplePane.getChildren().add(movinLabel);
        Label supLabel = new Label();
        supLabel.setId("paneShareLabel");
        supLabel.setText("Support");
        supLabel.setLayoutX(422);
        supLabel.setLayoutY(305);
        examplePane.getChildren().add(supLabel);
        Label resLabel = new Label();
        resLabel.setId("paneShareLabel");
        resLabel.setText("Resist");
        resLabel.setLayoutX(422);
        resLabel.setLayoutY(410);
        examplePane.getChildren().add(resLabel);
        
        ChoiceBox pinstChoiceBox = new ChoiceBox<>();
        ChoiceBox pChoiceBox = new ChoiceBox<>();
        pChoiceBox.getItems().addAll("Inst1", "Inst2");
        pChoiceBox.setLayoutX(14);
        pChoiceBox.setLayoutY(115);
        pChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                while (setArray[countArray][0] != name) {
                    countArray++;
                }

                pinstChoiceBox.disableProperty().set(false);
                setArray[countArray][1] = prices[(int) number2];
                countArray = 0;
                enableBtn();
            }
        });

        pinstChoiceBox.getItems().addAll(prices);
        pinstChoiceBox.setLayoutX(14);
        pinstChoiceBox.disableProperty().set(true);
        pinstChoiceBox.setLayoutY(160);
        pinstChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                if (pChoiceBox.getValue().toString() == "Inst1") {
                    switch ((int) number) {
                        case 0:
                            inst1VoLabel.setText("Kein Kurs");
                            inst1VoChoice.disableProperty().set(true);
                            break;
                        case 1:
                            inst1EcLabel.setText("Kein Kurs");
                            inst1EcChoice.disableProperty().set(true);
                            break;
                        case 2:
                            inst1PiLabel.setText("Kein Kurs");
                            inst1PiChoice.disableProperty().set(true);
                            break;
                        case 3:
                            inst1HiLabel.setText("Kein Kurs");
                            inst1HiChoice.disableProperty().set(true);
                            break;
                    }
                    switch ((int) number2) {
                        case 0:
                            inst1VoLabel.setText(tField.getText());
                            inst1VoChoice.disableProperty().set(false);
                            break;
                        case 1:
                            inst1EcLabel.setText(tField.getText());
                            inst1EcChoice.disableProperty().set(false);
                            break;
                        case 2:
                            inst1PiLabel.setText(tField.getText());
                            inst1PiChoice.disableProperty().set(false);
                            break;
                        case 3:
                            inst1HiLabel.setText(tField.getText());
                            inst1HiChoice.disableProperty().set(false);
                            break;
                    }
                }
            }
        });
        ChoiceBox tLBChoiceBox = new ChoiceBox<>();
        tLBChoiceBox.getItems().addAll(trends);
        tLBChoiceBox.setLayoutX(14);
        tLBChoiceBox.setLayoutY(250);
        tLBChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                while (setArray[countArray][0] != name) {
                    countArray++;
                }
                setArray[countArray][2] = prices[(int) number2];
                countArray = 0;
                enableBtn();
            }
        });
        ChoiceBox tLBinstChoiceBox = new ChoiceBox<>();
        tLBinstChoiceBox.getItems().addAll(trends);
        tLBinstChoiceBox.setLayoutX(14);
        tLBinstChoiceBox.setLayoutY(295);
        tLBinstChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                if (pChoiceBox.getValue().toString() == "Inst1") {
                    switch ((int) number) {
                        case 0:
                            inst1VoLabel.setText("Kein Kurs");
                            inst1VoChoice.disableProperty().set(true);
                            break;
                        case 1:
                            inst1EcLabel.setText("Kein Kurs");
                            inst1EcChoice.disableProperty().set(true);
                            break;
                        case 2:
                            inst1PiLabel.setText("Kein Kurs");
                            inst1PiChoice.disableProperty().set(true);
                            break;
                        case 3:
                            inst1HiLabel.setText("Kein Kurs");
                            inst1HiChoice.disableProperty().set(true);
                            break;
                    }
                    switch ((int) number2) {
                        case 0:
                            inst1VoLabel.setText(tField.getText());
                            inst1VoChoice.disableProperty().set(false);
                            break;
                        case 1:
                            inst1EcLabel.setText(tField.getText());
                            inst1EcChoice.disableProperty().set(false);
                            break;
                        case 2:
                            inst1PiLabel.setText(tField.getText());
                            inst1PiChoice.disableProperty().set(false);
                            break;
                        case 3:
                            inst1HiLabel.setText(tField.getText());
                            inst1HiChoice.disableProperty().set(false);
                            break;
                    }
                }
            }
        });
        ChoiceBox dChoiceBox = new ChoiceBox<>();
        dChoiceBox.getItems().addAll(derivate);
        dChoiceBox.setLayoutX(14);
        dChoiceBox.setLayoutY(385);
        dChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                while (setArray[countArray][0] != name) {
                    countArray++;
                }
                setArray[countArray][3] = prices[(int) number2]; // Klappt nicht so wie es soll
                countArray = 0;
                enableBtn();
            }
        });
        ChoiceBox dinstChoiceBox = new ChoiceBox<>();
        dinstChoiceBox.getItems().addAll(derivate);
        dinstChoiceBox.setLayoutX(14);
        dinstChoiceBox.setLayoutY(430);
        dinstChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                while (setArray[countArray][0] != name) {
                    countArray++;
                }
                setArray[countArray][3] = prices[(int) number2]; // Klappt nicht so wie es soll
                countArray = 0;
                enableBtn();
            }
        });

        ChoiceBox flaginstChoiceBox = new ChoiceBox<>();
        flaginstChoiceBox.getItems().addAll(derivate);
        flaginstChoiceBox.setLayoutX(226);
        flaginstChoiceBox.setLayoutY(115);
        flaginstChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                enableBtn();
            }
        });
        ChoiceBox flagChoiceBox = new ChoiceBox<>();
        flagChoiceBox.getItems().addAll(derivate);
        flagChoiceBox.setLayoutX(226);
        flagChoiceBox.setLayoutY(160);
        flagChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                enableBtn();
            }
        });
        ChoiceBox triinstChoiceBox = new ChoiceBox<>();
        triinstChoiceBox.getItems().addAll(derivate);
        triinstChoiceBox.setLayoutX(226);
        triinstChoiceBox.setLayoutY(250);
        triinstChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                enableBtn();
            }
        });
        ChoiceBox triChoiceBox = new ChoiceBox<>();
        triChoiceBox.getItems().addAll(derivate);
        triChoiceBox.setLayoutX(226);
        triChoiceBox.setLayoutY(295);
        triChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                enableBtn();
            }
        });
        ChoiceBox VforminstChoiceBox = new ChoiceBox<>();
        VforminstChoiceBox.getItems().addAll(derivate);
        VforminstChoiceBox.setLayoutX(226);
        VforminstChoiceBox.setLayoutY(385);
        VforminstChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                enableBtn();
            }
        });
        ChoiceBox VformChoiceBox = new ChoiceBox<>();
        VformChoiceBox.getItems().addAll(derivate);
        VformChoiceBox.setLayoutX(226);
        VformChoiceBox.setLayoutY(430);
        VformChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                enableBtn();
            }
        });

        ChoiceBox trendinstChoiceBox = new ChoiceBox<>();
        trendinstChoiceBox.getItems().addAll(derivate);
        trendinstChoiceBox.setLayoutX(500);
        trendinstChoiceBox.setLayoutY(70);
        trendinstChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                enableBtn();
            }
        });
        ChoiceBox trendChoiceBox = new ChoiceBox<>();
        trendChoiceBox.getItems().addAll(derivate);
        trendChoiceBox.setLayoutX(500);
        trendChoiceBox.setLayoutY(115);
        trendChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                enableBtn();
            }
        });
        ChoiceBox movininstChoiceBox = new ChoiceBox<>();
        movininstChoiceBox.getItems().addAll(derivate);
        movininstChoiceBox.setLayoutX(500);
        movininstChoiceBox.setLayoutY(175);
        movininstChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                enableBtn();
            }
        });
        ChoiceBox movinChoiceBox = new ChoiceBox<>();
        movinChoiceBox.getItems().addAll(derivate);
        movinChoiceBox.setLayoutX(500);
        movinChoiceBox.setLayoutY(220);
        movinChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                enableBtn();
            }
        });
        ChoiceBox supinstChoiceBox = new ChoiceBox<>();
        supinstChoiceBox.getItems().addAll(derivate);
        supinstChoiceBox.setLayoutX(500);
        supinstChoiceBox.setLayoutY(280);
        supinstChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                enableBtn();
            }
        });
        ChoiceBox supChoiceBox = new ChoiceBox<>();
        supChoiceBox.getItems().addAll(derivate);
        supChoiceBox.setLayoutX(500);
        supChoiceBox.setLayoutY(325);
        supChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                enableBtn();
            }
        });
        ChoiceBox resinstChoiceBox = new ChoiceBox<>();
        resinstChoiceBox.getItems().addAll(derivate);
        resinstChoiceBox.setLayoutX(500);
        resinstChoiceBox.setLayoutY(385);
        resinstChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                enableBtn();
            }
        });
        ChoiceBox resChoiceBox = new ChoiceBox<>();
        resChoiceBox.getItems().addAll(derivate);
        resChoiceBox.setLayoutX(500);
        resChoiceBox.setLayoutY(430);
        resChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                enableBtn();
            }
        });
        examplePane.getChildren().add(pChoiceBox);
        examplePane.getChildren().add(pinstChoiceBox);
        examplePane.getChildren().add(tLBChoiceBox);
        examplePane.getChildren().add(tLBinstChoiceBox);
        examplePane.getChildren().add(dChoiceBox);
        examplePane.getChildren().add(dinstChoiceBox);
        examplePane.getChildren().add(dLabel);
        examplePane.getChildren().add(pLabel);
        
        examplePane.getChildren().add(flagChoiceBox);
        examplePane.getChildren().add(flaginstChoiceBox);
        examplePane.getChildren().add(triChoiceBox);
        examplePane.getChildren().add(triinstChoiceBox);
        examplePane.getChildren().add(VformChoiceBox);
        examplePane.getChildren().add(VforminstChoiceBox);
        examplePane.getChildren().add(trendChoiceBox);
        examplePane.getChildren().add(trendinstChoiceBox);
        examplePane.getChildren().add(movininstChoiceBox);
        examplePane.getChildren().add(movinChoiceBox);
        examplePane.getChildren().add(supinstChoiceBox);
        examplePane.getChildren().add(supChoiceBox);
        examplePane.getChildren().add(resinstChoiceBox);
        examplePane.getChildren().add(resChoiceBox);
        return examplePane;
    }

    LocalDate minDateStart = LocalDate.of(2023, 4, 16);
    LocalDate maxDateStart = LocalDate.now();

    private void updateStartPicker() { // Datum Blockers WARNING: Müsen schauen dass wir die angegebenen Daten bei
                                       // Änderung der Aktien überprüfen
        startPicker.setDayCellFactory(d -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setDisable(item.isAfter(maxDateStart) || item.isBefore(minDateStart));
            }
        });
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
        System.out.println("countArray" + countArray);
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
}
