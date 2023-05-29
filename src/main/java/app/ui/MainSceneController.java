package app.ui;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
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
import javafx.scene.control.Control;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.Calendar;
import java.time.Instant;

import app.AppError;
import app.communication.EventQueues;
import app.communication.Msg;
import app.communication.MsgToSMType;
import app.communication.MsgToUIType;
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

    private Stage stage;
    private Scene scene;
    private Parent root;

    private CheckEQService checkEQService;
    private Mapping mapping = new Mapping();
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
                    default -> System.out.println("ERROR: Msg-Type " + msg.type + " not yet implemented");
                }
            }
        });
        checkEQService.start();

        // @nocheckin Uncomment this before committing
        displayError("Testing", "Test");

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

        audioLength.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                Integer newVal = Integer.parseInt(newValue);
                if(Integer.parseInt(audioLength.getText()) <= 59){
                    if(audioLength1.getText() != null ){
                        Integer minValue = Integer.parseInt(audioLength1.getText());
                        newValue += (minValue *60);
                        mapping.setSoundLength(newVal);         
                    }
                }
                else{
                    //falsche Eingabe
                    audioLength.setText(null);
                    audioLength.setPromptText("0-59");
                }
                enableBtnIfValid();
            } catch (Exception e) {
                // TODO: Error Handling
            }
        });
        audioLength1.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                Integer newVal = Integer.parseInt(newValue)*60;
                if(Integer.parseInt(audioLength1.getText()) <= 5){
                    if(Integer.parseInt(audioLength1.getText()) == 5){
                        audioLength.setText("0");
                        audioLength.setDisable(true);
                    }
                    else{
                        audioLength.setDisable(false);
                    }
                 if(audioLength.getText() != null){
                    Integer secValue = Integer.parseInt(audioLength.getText());
                    newValue += (secValue);
                    mapping.setSoundLength(newVal);
                    enableBtnIfValid();   
                 }  
                }
                else{
                    //Error zu hoch eingestellt
                    audioLength1.setText(null);
                    audioLength1.setPromptText("0-5");
                }
            } catch (Exception e) {
                // TODO: Error Handling
            }
        });
        startBtn.setOnAction(ev -> {
            try {
                EventQueues.toSM.add(new Msg<>(MsgToSMType.START, mapping));
               switchToMusicScene(ev);
            } catch (Exception e) {
                e.printStackTrace();
                // TODO: Error handling
            }
        });
    }

    public void switchToMusicScene(ActionEvent event) throws IOException {
		root = FXMLLoader.load(getClass().getResource("/MusicScene.fxml"));
		stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		scene = new Scene(root);
		String css = this.getClass().getResource("/choice.css").toExternalForm();
        // Set the stylesheet after the scene creation
        scene.getStylesheets().add(css);
		stage.setScene(scene);
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

            instCB.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                SelectionModel<InstrParam> paramCBSelect = paramCB.getSelectionModel();
                // Mapping isn't effected if the parameter ChoiceBox isn't selected yet
                if (!paramCBSelect.isEmpty()) {
                    try {
                        mapping.setParam(newValue, sonifiableId, paramCBSelect.getSelectedItem(), eparam);
                        if (oldValue != null)
                            mapping.rmParam(sonifiableId, oldValue, paramCBSelect.getSelectedItem());
                        enableBtnIfValid();
                    } catch (AppError e) {
                        instCB.getSelectionModel().select(oldValue);
                        displayError(e.getMessage(), "Interner Fehler");
                    }
                }
            });

            paramCB.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                SelectionModel<InstrumentEnum> instCBSelect = instCB.getSelectionModel();
                if (!instCBSelect.isEmpty()) {
                    try {
                        mapping.setParam(instCBSelect.getSelectedItem(), sonifiableId, newValue, eparam);
                        if (oldValue != null)
                            mapping.rmParam(sonifiableId, instCBSelect.getSelectedItem(), oldValue);
                        enableBtnIfValid();
                    } catch (AppError e) {
                        paramCB.getSelectionModel().select(oldValue);
                        displayError(e.getMessage(), "Interner Fehler");
                    }
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
       // else
            //startBtn.setDisable(true);
    }
}
