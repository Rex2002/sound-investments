package app.ui;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
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
import javafx.scene.control.SelectionModel;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.util.Duration;
import app.AppError;
import app.communication.EventQueues;
import app.communication.Msg;
import app.communication.MsgToSMType;
import app.communication.MsgToUIType;
import app.communication.MusicData;
import app.communication.SonifiableFilter;
import app.mapping.ExchangeParam;
import app.mapping.InstrParam;
import app.mapping.LineData;
import app.mapping.Mapping;
import app.mapping.PointData;
import audio.synth.InstrumentEnum;
import dataRepo.DateUtil;
import dataRepo.Sonifiable;
import dataRepo.SonifiableID;
import dataRepo.DataRepo.FilterFlag;

public class MainSceneController implements Initializable {
    // WARNING: Kommentare werden noch normalisiert
    @FXML
    private AnchorPane anchor;
    @FXML
    private TextField searchBar;
    @FXML
    private Button startBtn;
    @FXML
    private VBox paneBoxSonifiables;
    @FXML
    private Label headerTitle;
    @FXML
    private ChoiceBox<String> categoriesChoice;
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
    private DatePicker startPicker;
    @FXML
    private DatePicker endPicker;
    @FXML
    private TextField audioLength;
    @FXML
    private TextField audioLength1;
    @FXML
    private VBox checkVBox;
    @FXML
    private VBox instCheckBox;
    @FXML
    private double duration;

    private ImageView loading;
    private CheckEQService checkEQService;
    private Mapping mapping = new Mapping();
    private boolean currentlyUpdatingCB = false;
    private String[] locations = { "Deutschland" };
    private static String[] categoryKeys = { "Alle Kategorien", "Aktien", "ETFs", "Indizes" };
    private static FilterFlag[] categoryValues = { FilterFlag.ALL, FilterFlag.STOCK, FilterFlag.ETF, FilterFlag.INDEX };
    static {
        assert categoryKeys.length == categoryValues.length : "categoryKeys & categoryValues are not in sync";
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) { // Initialisierung mit den Optionen
        categoriesChoice.getItems().addAll(MainSceneController.categoryKeys);
        locationChoice.getItems().addAll(locations);
        enableBtnIfValid();

        checkEQService = new CheckEQService();
        checkEQService.setPeriod(Duration.millis(100));
        checkEQService.setOnSucceeded((event) -> {
            List<Msg<MsgToUIType>> messages = checkEQService.getValue();
            for (Msg<MsgToUIType> msg : messages) {
                switch (msg.type) {
                    case FILTERED_SONIFIABLES -> {
                        clearCheckList();
                        List<Sonifiable> sonifiables = (List<Sonifiable>) msg.data;
                        // TODO: Decide whether we want to show all found sonifiables immediately or
                        // only like 10 at once, unless prompted by the user to show more
                        for (Sonifiable s : sonifiables) {
                            addToCheckList(s);
                        }
                    }
                    case LOADABLE_MAPPINGS -> System.out.println("ERROR: Msg-Type LOADABLE_MAPPINGS is not yet implemented");
                    case ERROR -> displayError((String) msg.data, "Interner Fehler");
                    case VALIDATION_DONE -> System.out.println("ERROR: Msg-Type VALIDATION_DONE is not yet implemented");
                    case VALIDATION_ERROR -> displayError((String) msg.data, "Ungültiges Mapping");
                    case FINISHED -> switchToMusicScene((MusicData) msg.data);
                }
            }
        });
        checkEQService.start();

        categoriesChoice.getSelectionModel().selectedIndexProperty().addListener((observable, oldIdx, newIdx) -> {
            EventQueues.toSM.add(new Msg<>(MsgToSMType.FILTERED_SONIFIABLES,
                    new SonifiableFilter(searchBar.getText(), categoryValues[(int) newIdx])));
        });
        categoriesChoice.getSelectionModel().selectFirst();

        searchBar.textProperty().addListener((observable, oldVal, newVal) -> {
            EventQueues.toSM.add(new Msg<>(MsgToSMType.FILTERED_SONIFIABLES, new SonifiableFilter(newVal,
                    categoryValues[categoriesChoice.getSelectionModel().getSelectedIndex()])));
        });

        startPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            try {
                mapping.setStartDate(DateUtil.localDateToCalendar(newValue));
                enableBtnIfValid();
            } catch (Exception e) {
                // TODO: Error Handling
            }
        });
        endPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            try {
                mapping.setEndDate(DateUtil.localDateToCalendar(newValue));
                enableBtnIfValid();
            } catch (Exception e) {
                // TODO: Error Handling
            }
        });
        // Set default values
        startPicker.valueProperty().setValue(LocalDate.now().minusMonths(1));
        endPicker.valueProperty().setValue(LocalDate.now());

        audioLength.textProperty().addListener((o, oldVal, newVal) -> updateSoundLength());
        audioLength1.textProperty().addListener((o, oldVal, newVal) -> updateSoundLength());
        // Set default values
        audioLength.textProperty().setValue("30");
        audioLength1.textProperty().setValue("0");

        startBtn.setOnAction(ev -> {
            try {
                EventQueues.toSM.add(new Msg<>(MsgToSMType.START, mapping));
                // startBtn.setDisable(true);
                // Show loading image
                loading = new ImageView(new Image(getClass().getResource("/loading.png").toExternalForm()));
                double loadingWidth = 150;
                double loadingHeight = 150;
                loading.setFitWidth(loadingWidth);
                loading.setFitHeight(loadingHeight);
                loading.setLayoutX(anchor.getScene().getWidth() / 2 - loadingWidth / 2);
                loading.setLayoutY(anchor.getScene().getHeight() / 2 - loadingHeight / 2);
                anchor.getChildren().add(loading);
                // Animate loading image
                Timer loadingAnimTimer = new Timer();
                int nextFrameInMs = 60;
                loadingAnimTimer.scheduleAtFixedRate(new TimerTask() {
                    private int counter = 1;
                    public void run() {
                        loading.setRotate(360 * counter / 12);
                        counter = (counter + 1) % 12;
                    }
                }, nextFrameInMs, nextFrameInMs);
            } catch (Exception e) {
                e.printStackTrace();
                // TODO: Error handling
            }
        });
    }

    private void updateSoundLength() {
        if (Integer.parseInt(audioLength.getText()) <= 59) {
            if (audioLength1.getText() != null) {
                Integer minValue = 0;
                try {
                    minValue = Integer.parseInt(audioLength1.getText());
                } catch (NumberFormatException e) {
                }

                Integer passValue = minValue * 60;
                try {
                    passValue += Integer.parseInt(audioLength.getText());
                } catch (NumberFormatException e) {
                }

                mapping.setSoundLength(passValue);
                duration = passValue;
            }
        } else {
            // falsche Eingabe
            audioLength.setText(null);
            audioLength.setPromptText("0-59");
        }
        enableBtnIfValid();
    }

    public void switchToMusicScene(MusicData musicData) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MusicScene.fxml"));
            Parent root = loader.load();
            MusicSceneController controller = loader.getController();
            controller.passData(musicData);
            Stage stage = (Stage) startBtn.getScene().getWindow();
            Scene scene = new Scene(root);
            String css = this.getClass().getResource("/choice.css").toExternalForm();
            // Set the stylesheet after the scene creation
            scene.getStylesheets().add(css);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            displayError("Fehler beim Laden de nächsten UI-Szene", "Interner Fehler");
        }
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

    public void addToCheckList(Sonifiable sonifiable) {
        CheckBox cBox = new CheckBox(sonifiable.getName());
        cBox.setUserData(sonifiable);

        if (mapping.hasSonifiable(sonifiable.getId())) {
            cBox.setSelected(true);
        }
        cBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                if (paneBoxSonifiables.getChildren().size() < Mapping.MAX_SONIFIABLES_AMOUNT) {
                    addToPaneBox((Sonifiable) cBox.getUserData());
                } else {
                    displayError(
                            "Zu viele Börsenkurse gewählt. Es dürfen höchstens "
                                    + Integer.toString(Mapping.MAX_SONIFIABLES_AMOUNT) + " Börsenkurse gewählt werden.",
                            "Zu viele Börsenkurse");
                    cBox.setSelected(false);
                }
            } else {
                rmSonifiable(((Sonifiable) cBox.getUserData()).getId());
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

    private void rmSonifiable(SonifiableID id) {
        mapping.rmSonifiable(id);
        ObservableList<Node> children = paneBoxSonifiables.getChildren();
        int idx = 0;
        while (idx < children.size() && !id.equals(children.get(idx).getUserData()))
            idx++;
        assert idx != children.size() : "rmSonifiable was called on " + id + " which couldn't be found in SceneTree.";
        children.remove(idx);
        paneBoxSonifiables.prefHeight(children.size() * 477.0);
    }

    private void loadNew() {
        // Reload sonifiables
    }

    @FXML
    public void addToPaneBox(Sonifiable sonifiable) {
        // add a Sharepanel to the Panel Box
        // Checking whether the maximum of sharePanels has already been reached must be
        // done before calling this function
        paneBoxSonifiables.getChildren().add(createSharePane(sonifiable));
        paneBoxSonifiables.setPrefHeight((paneBoxSonifiables.getChildren().size()) * 800.0);
    }

    private void addLine(String cssClass, int layoutX, int layoutY, int startX, int startY, int endX, int endY,
            ObservableList<Node> children) {
        Line line = new Line();
        if (cssClass != null)
            line.getStyleClass().add(cssClass);
        line.setLayoutX(layoutX);
        line.setLayoutY(layoutY);
        line.setStartX(startX);
        line.setStartY(startY);
        line.setEndX(endX);
        line.setEndY(endY);
        children.add(line);
    }

    private void addStockParamToPane(String text, String cssClass, int labelX, int labelY, int cb1X, int cb1Y, int cb2X,
            int cb2Y, SonifiableID sonifiableId,
            ExchangeParam eparam,
            ObservableList<Node> children) {
        Label label = new Label(text);
        label.getStyleClass().add(cssClass);
        label.setLayoutX(labelX);
        label.setLayoutY(labelY);
        children.add(label);

        if (eparam instanceof PointData) {
            // TODO: Add ChoiceBox for Event-Instrument
        } else {
            InstrParam[] iparams = (eparam instanceof LineData) ? InstrParam.LineDataParams
                    : InstrParam.RangeDataParams;

            ChoiceBox<InstrumentEnum> instCB = new ChoiceBox<>();
            instCB.getItems().addAll(InstrumentEnum.values());
            instCB.setLayoutX(cb1X);
            instCB.setLayoutY(cb1Y);

            ChoiceBox<InstrParam> paramCB = new ChoiceBox<>();
            paramCB.getItems().addAll(iparams);
            paramCB.setLayoutX(cb2X);
            paramCB.setLayoutY(cb2Y);
            paramCB.setDisable(true);
            instCB.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    // If the new instrument already has the selected parameter mapped or if newValue is null
                    // then we need to remove the parameter in the UI
                    // otherwise we also need to set the parameter in the mapping
                    // in any case, we need to remove the old mapping with the old instrument
                    if (currentlyUpdatingCB) return;
                    currentlyUpdatingCB = true;
                    SingleSelectionModel<InstrParam> paramCBSelect = paramCB.getSelectionModel();
                    InstrParam paramVal = paramCBSelect.getSelectedItem();

                    paramCB.setDisable(newValue == null);
                    paramCBSelect.select(null);
                    paramCB.getItems().clear();
                    if (newValue != null && paramVal != null) {
                        mapping.rmParam(oldValue, sonifiableId, paramVal);

                        if (!mapping.isMapped(newValue, paramVal)) {
                            paramCB.getItems().addAll(mapping.getEmptyInstrumentParams(newValue, paramVal));
                            paramCBSelect.select(paramVal);
                            mapping.setParam(newValue, sonifiableId, paramVal, eparam);
                        } else {
                            paramCB.getItems().addAll(mapping.getEmptyInstrumentParams(newValue, null));
                        }
                    } else {
                        paramCB.getItems().addAll(mapping.getEmptyInstrumentParams(newValue, null));
                    }
                    enableBtnIfValid();
                    currentlyUpdatingCB = false;
                } catch (AppError e) {
                    displayError(e.getMessage(), "Interner Fehler");
                }
            });
            // paramCB.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            //     @Override
            //     public void handle(MouseEvent event) {
            //         paramCB.getItems().clear();
            //         paramCB.getItems().addAll(mapping.getEmptyInstrumentParams(instCB.getValue(), paramCB.getValue()));
            //     }
            // });
            // paramCB.setOnMouseClicked(event -> {
            //     paramCB.getItems().clear();
            //     paramCB.getItems().addAll(mapping.getEmptyInstrumentParams(instCB.getValue(), paramCB.getValue()));
            // });
            paramCB.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    if (currentlyUpdatingCB) return;
                    currentlyUpdatingCB = true;
                    SelectionModel<InstrumentEnum> instCBSelect = instCB.getSelectionModel();
                    if (!instCBSelect.isEmpty()) {
                        if (oldValue != null)
                            mapping.rmParam(instCBSelect.getSelectedItem(), sonifiableId, oldValue);
                        if (newValue != null)
                            mapping.setParam(instCBSelect.getSelectedItem(), sonifiableId, newValue, eparam);
                        enableBtnIfValid();
                    }
                    currentlyUpdatingCB = false;
                } catch (AppError e) {
                    paramCB.getSelectionModel().select(oldValue);
                    displayError(e.getMessage(), "Interner Fehler");
                }
            });

            children.add(instCB);
            children.add(paramCB);
        }
    }

    private Pane createSharePane(Sonifiable sonifiable) { // initialize and dek the Share Pane
        mapping.addSonifiable(sonifiable.getId());

        Pane stockPane = new Pane();
        stockPane.setUserData(sonifiable.getId());
        stockPane.getStyleClass().add("stockPane");
        TextField tField = new TextField();
        tField.setText(sonifiable.getName());
        tField.getStyleClass().add("txtField");
        tField.setLayoutX(168);
        tField.setLayoutY(8);
        stockPane.getChildren().add(tField);

        addLine(null, 174, 53, 0, 0, 391, 0, stockPane.getChildren());
        addLine("pinkline", 306, 168, -100, -60, -100, 263, stockPane.getChildren());
        addLine("pinkline", 512, 177, -100, -60, -100, 263, stockPane.getChildren());

        addStockParamToPane("Price", "paneShareLabel", 14, 80, 14, 115, 14, 160, sonifiable.getId(), LineData.PRICE,
                stockPane.getChildren());
        addStockParamToPane("Trend Line Break", "paneShareLabel", 14, 215, 14, 250, 14, 295, sonifiable.getId(),
                LineData.PRICE, stockPane.getChildren());
        addStockParamToPane("Derivate", "paneShareLabel", 14, 350, 14, 385, 14, 430, sonifiable.getId(), LineData.PRICE,
                stockPane.getChildren());
        addStockParamToPane("Flag", "paneShareLabel", 226, 80, 226, 115, 226, 160, sonifiable.getId(), LineData.PRICE,
                stockPane.getChildren());
        addStockParamToPane("Triangle", "paneShareLabel", 226, 215, 226, 250, 226, 295, sonifiable.getId(),
                LineData.PRICE,
                stockPane.getChildren());
        addStockParamToPane("Vform", "paneShareLabel", 226, 350, 226, 385, 226, 430, sonifiable.getId(), LineData.PRICE,
                stockPane.getChildren());
        addStockParamToPane("Trend-", "paneShareLabel", 422, 80, 500, 70, 500, 115, sonifiable.getId(), LineData.PRICE,
                stockPane.getChildren());
        Label label = new Label("Break");
        label.getStyleClass().add("paneShareLabel");
        label.setLayoutX(422);
        label.setLayoutY(100);
        stockPane.getChildren().add(label);
        addStockParamToPane("Movin", "paneShareLabel", 422, 203, 500, 175, 500, 220, sonifiable.getId(), LineData.PRICE,
                stockPane.getChildren());
        addStockParamToPane("Support", "paneShareLabel", 422, 305, 500, 280, 500, 325, sonifiable.getId(),
                LineData.PRICE, stockPane.getChildren());
        addStockParamToPane("Resist", "paneShareLabel", 422, 410, 500, 385, 500, 430, sonifiable.getId(),
                LineData.PRICE, stockPane.getChildren());

        return stockPane;
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

    public void enableBtnIfValid() {
        if (mapping.isValid())
            startBtn.setDisable(false);
        else
            startBtn.setDisable(true);
    }
}