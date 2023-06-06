package app.ui;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Function;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.util.Duration;
import util.ArrayFunctions;
import util.DateUtil;
import app.AppError;
import app.communication.EventQueues;
import app.communication.Msg;
import app.communication.MsgToSMType;
import app.communication.MsgToUIType;
import app.communication.MusicData;
import app.communication.SonifiableFilter;
import app.mapping.EvInstrMapping;
import app.mapping.ExchangeData;
import app.mapping.ExchangeParam;
import app.mapping.InstrParam;
import app.mapping.LineData;
import app.mapping.MappedInstr;
import app.mapping.Mapping;
import app.mapping.PointData;
import app.mapping.RangeData;
import audio.synth.EvInstrEnum;
import audio.synth.InstrumentEnum;
import dataRepo.Sonifiable;
import dataRepo.SonifiableID;
import dataRepo.FilterFlag;

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
    private ChoiceBox<String> categoriesCB;
    @FXML
    private ChoiceBox<String> locationCB;
    @FXML
    private ChoiceBox<String> filterCB;
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
    private VBox instBox;
    @FXML
    private double duration;

    private LocalDate minDateStart = LocalDate.now().minusMonths(3);
    private LocalDate maxDateStart = LocalDate.now().minusDays(3);
    private LocalDate minDateEnd = LocalDate.now().minusMonths(3).plusDays(3);
    private LocalDate maxDateEnd = LocalDate.now();

    private Timer loadingAnimTimer;
    private ImageView loading;
    private Image closeImg;
    private CheckEQService checkEQService;
    private Mapping mapping = new Mapping();
    private boolean currentlyUpdatingCB = false;
    private boolean startedSonification = true;

    private String[] locations = { "Deutschland" }; // TODO: Get available locations from StateManager

    // Choice-Box String->Value Maps
    private static String[] categoryKeys = { "Alle Kategorien", "Aktien", "ETFs", "Indizes" };
    private static FilterFlag[] categoryValues = { FilterFlag.ALL, FilterFlag.STOCK, FilterFlag.ETF, FilterFlag.INDEX };
    private static String[] filterKeys = { "Low-Pass", "High-Pass" };
    private static boolean[] filterValues = { false, true };
    private static String[] instKeys;
    private static InstrumentEnum[] instVals;
    private static void setInstMap() {
        InstrumentEnum[] insts = InstrumentEnum.values();
        instKeys = new String[insts.length + 2];
        instVals = new InstrumentEnum[insts.length + 2];
        instKeys[0] = "";
        instVals[0] = null;
        instKeys[1] = "Globale Audio-Parameter";
        instVals[0] = null;
        for (int i = 0; i < insts.length; i++) {
            instKeys[i + 2] = insts[i].toString();
            instVals[i + 2] = insts[i];
        }
    }
    static {
        setInstMap();
        assert instKeys.length == instVals.length : "instKeys & instValues are not in sync";
        assert categoryKeys.length == categoryValues.length : "categoryKeys & categoryValues are not in sync";
        assert filterKeys.length == filterValues.length : "filterKeys & filterValues are not in sync";
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) { // Initialisierung mit den Optionen
        categoriesCB.getItems().addAll(MainSceneController.categoryKeys);
        locationCB.getItems().addAll(locations);
        enableBtnIfValid();

        closeImg = new Image(getClass().getResource("/close_icon.png").toExternalForm());

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
                    case SONIFIABLE_FILTER -> {
                        SonifiableFilter filter = (SonifiableFilter) msg.data;
                        searchBar.setText(filter.prefix);
                        int categoryIdx = ArrayFunctions.findIndex(categoryValues, c -> c.equals(filter.categoryFilter));
                        categoriesCB.getSelectionModel().select(categoryIdx);
                    }
                    case ERROR -> {
                        startedSonification = false;
                        displayError((String) msg.data, "Interner Fehler");
                        if (loadingAnimTimer != null) loadingAnimTimer.cancel();
                        if (loading != null) {
                            anchor.getChildren().remove(loading);
                            loading = null;
                        }
                    }
                    case MAPPING -> {
                        mapping = (Mapping) msg.data;
                        showMapping();
                        enableBtnIfValid();
                    }
                    case FINISHED -> {
                        startedSonification = false;
                        switchToMusicScene((MusicData) msg.data);
                    }
                }
            }
        });
        checkEQService.start();

        categoriesCB.getSelectionModel().selectedIndexProperty().addListener((observable, oldIdx, newIdx) -> {
            EventQueues.toSM.add(new Msg<>(MsgToSMType.FILTERED_SONIFIABLES,
                    new SonifiableFilter(searchBar.getText(), categoryValues[(int) newIdx])));
        });
        categoriesCB.getSelectionModel().selectFirst();

        searchBar.textProperty().addListener((observable, oldVal, newVal) -> {
            EventQueues.toSM.add(new Msg<>(MsgToSMType.FILTERED_SONIFIABLES, new SonifiableFilter(newVal,
                    categoryValues[categoriesCB.getSelectionModel().getSelectedIndex()])));
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

        filterCB.getItems().addAll(filterKeys);
        filterCB.getSelectionModel().select(0);
        filterCB.getSelectionModel().selectedIndexProperty().addListener((observable, o, n) -> {
            int idx = n.intValue();
            if (idx < 0) {
                idx = 0;
                filterCB.getSelectionModel().select(0);
            }
            mapping.setHighPass(filterValues[idx]);
        });

        startBtn.setOnAction(ev -> {
            try {
                startBtn.setDisable(true);
                startedSonification = true;
                EventQueues.toSM.add(new Msg<>(MsgToSMType.START, mapping));
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
                loadingAnimTimer = new Timer();
                int nextFrameInMs = 60;
                loadingAnimTimer.cancel(); // In case the animation was already playing
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

        mapping.setOnInstrAdded(inst -> instAdded(inst.toString()));
        mapping.setOnEvInstrAdded(inst -> instAdded(inst.toString()));
        mapping.setOnInstrRemoved(inst -> instRemoved(inst.toString()));
        mapping.setOnEvInstrRemoved(inst -> instRemoved(inst.toString()));
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
        // TODO: Make prettier & make text actually readable
        System.out.println(errorMessage);
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
        errorMes.setWrapText(true);

        errorMes.setLayoutX(20);
        Label errorTit = new Label(errorTitle);
        errorTit.setId("errorTitle");
        errorTit.setLayoutY(20);
        errorTit.setLayoutX(20);

        errorTit.setWrapText(true);
        Button close = new Button("Schließen");
        close.setOnMouseClicked(event ->{
                anchor.getChildren().remove(errorPane);
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
        Pane[] stockPane = new Pane[1];
        stockPane[0] = new Pane();
        cBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                if (paneBoxSonifiables.getChildren().size() < Mapping.MAX_SONIFIABLES_AMOUNT) {
                    stockPane[0]=  addToPaneBox((Sonifiable) cBox.getUserData());
                } else {
                    displayError(
                            "Zu viele Börsenkurse gewählt. Es dürfen höchstens "
                                    + Integer.toString(Mapping.MAX_SONIFIABLES_AMOUNT) + " Börsenkurse gewählt werden.",
                            "Zu viele Börsenkurse");
                    cBox.setSelected(false);
                }
            } else {
                rmSonifiable(((Sonifiable) cBox.getUserData()).getId(), stockPane[0], false);


            }
        });
        checkVBox.setPrefHeight((checkVBox.getChildren().size()) * 42.0);
        checkVBox.getChildren().add(cBox);
    }

    private void rmSonifiable(SonifiableID id, Pane stockPane, boolean updateSearchResult) {
        mapping.rmSonifiable(id);
        enableBtnIfValid();

        ObservableList<Node> children = paneBoxSonifiables.getChildren();
        int idx = 0;
        while (idx < children.size() && !id.equals(children.get(idx).getUserData()))
            idx++;
        assert idx != children.size() : "rmSonifiable was called on " + id + " which couldn't be found in SceneTree.";
        paneBoxSonifiables.getChildren().remove(stockPane);
        paneBoxSonifiables.prefHeight(children.size() * 511.0);

        if (updateSearchResult) {
            ObservableList<Node> checkBoxes = checkVBox.getChildren();
            for (int i = 0; i < checkBoxes.size(); i++) {
                try {
                    CheckBox c = (CheckBox) checkBoxes.get(i);
                    if (((Sonifiable) c.getUserData()).getId() == id){
                        c.setSelected(false);
                        break;
                    }
                } catch (ClassCastException e) {}
            }
        }
    }

    @FXML
    public Pane addToPaneBox(Sonifiable sonifiable) {
        return addToPaneBox(sonifiable, false);
    }

    public Pane addToPaneBox(Sonifiable sonifiable, boolean showMapping) {
        // add a Sharepanel to the Panel Box
        // Checking whether the maximum of sharePanels has already been reached must be
        // done before calling this function
        Pane test  = createSharePane(sonifiable, showMapping);
        paneBoxSonifiables.getChildren().add(test);
        paneBoxSonifiables.setPrefHeight((paneBoxSonifiables.getChildren().size()) * 511.0);
        return test;
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

    private void addStockParamToPane(String text, String cssClass, int labelX, int labelY, int cb1X, int cb1Y, int cb2X, int cb2Y, Sonifiable sonifiable, ExchangeParam eparam, ObservableList<Node> children, boolean showMapping) {
        Label label = new Label(text);
        label.getStyleClass().add(cssClass);
        label.setLayoutX(labelX);
        label.setLayoutY(labelY);
        children.add(label);

        if (eparam instanceof PointData) {
            String[] evInsts = EvInstrEnum.displayVals;
            ChoiceBox<String> evInstCB = new ChoiceBox<>();
            evInstCB.getItems().add("");
            evInstCB.getItems().addAll(evInsts);
            evInstCB.setLayoutX(cb1X);
            evInstCB.setLayoutY(cb1Y);
            if (showMapping) {
                for (EvInstrMapping evInstMap : mapping.getEventInstruments()) {
                    if (evInstMap.getData().getData().equals(eparam) && evInstMap.getData().getId().equals(sonifiable.getId())) {
                        evInstCB.getSelectionModel().select(evInstMap.getInstrument().toString());
                        break;
                    }
                }
            }
            evInstCB.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    if (oldValue != null) mapping.rmEvInstr(sonifiable.getId(), (PointData) eparam);
                    if (newValue != null) mapping.addEvInstr(EvInstrEnum.fromString(newValue), sonifiable, (PointData) eparam);
                } catch (AppError e) {
                    displayError(e.getMessage(), "Interner Fehler");
                }
            });
            children.add(evInstCB);

        } else {
            boolean isLineParam = (eparam instanceof LineData);

            ChoiceBox<String> instCB = new ChoiceBox<>();
            instCB.getItems().addAll(instKeys);
            instCB.setLayoutX(cb1X);
            instCB.setLayoutY(cb1Y);

            ChoiceBox<String> paramCB = new ChoiceBox<>();
            paramCB.setLayoutX(cb2X);
            paramCB.setLayoutY(cb2Y);
            paramCB.setDisable(true);
            if (showMapping) {
                MappedInstr mi = mapping.get(new ExchangeData<>(sonifiable.getId(), eparam));
                if (mi != null) {
                    instCB.getSelectionModel().select(mi.instr.toString());
                    paramCB.getSelectionModel().select(mi.param.toString());
                    paramCB.setDisable(false);
                }
            }
            instCB.getSelectionModel().selectedIndexProperty().addListener((observable, oldIdx, newIdx) -> {
                try {
                    // If the new instrument already has the selected parameter mapped or if newValue is null
                    // then we need to remove the parameter in the UI
                    // otherwise we also need to set the parameter in the mapping
                    // in any case, we need to remove the old mapping with the old instrument
                    if (currentlyUpdatingCB) return;
                    currentlyUpdatingCB = true;
                    InstrumentEnum newValue = instVals[Math.max(newIdx.intValue(), 0)];
                    InstrumentEnum oldValue = instVals[Math.max(oldIdx.intValue(), 0)];
                    SingleSelectionModel<String> paramCBSelect = paramCB.getSelectionModel();
                    InstrParam paramVal = paramCBSelect.getSelectedItem() == null ? null : InstrParam.fromString(paramCBSelect.getSelectedItem());

                    paramCB.setDisable(newIdx.intValue() <= 0);
                    paramCBSelect.select(null);
                    paramCB.getItems().clear();
                    InstrParam[] newOpts;
                    Function<InstrParam, InstrParam[]> getOpts = (pv) -> isLineParam ? mapping.getEmptyLineParams(newValue, pv) : mapping.getEmptyRangeParams(newValue, pv);
                    if (newValue != null && paramVal != null) {
                        mapping.rmParam(oldValue, paramVal);

                        if (!mapping.isMapped(newValue, paramVal)) {
                            newOpts = getOpts.apply(paramVal);
                            paramCBSelect.select(paramVal.toString());
                            mapping.setParam(newValue, sonifiable, paramVal, eparam);
                        } else {
                            newOpts = getOpts.apply(null);
                        }
                    } else {
                        newOpts = getOpts.apply(null);
                    }
                    paramCB.getItems().add(null);
                    for (InstrParam opt : newOpts) paramCB.getItems().add(opt.toString());
                    enableBtnIfValid();
                    currentlyUpdatingCB = false;
                } catch (AppError e) {
                    displayError(e.getMessage(), "Interner Fehler");
                }
            });
            paramCB.getSelectionModel().selectedItemProperty().addListener((observable, oldStr, newStr) -> {
                try {
                    if (currentlyUpdatingCB) return;
                    currentlyUpdatingCB = true;
                    SelectionModel<String> instCBSelect = instCB.getSelectionModel();
                    InstrumentEnum inst = instVals[Math.max(0, instCBSelect.getSelectedIndex())];
                    InstrParam oldVal = InstrParam.fromString(oldStr);
                    InstrParam newVal = InstrParam.fromString(newStr);

                    if (!instCBSelect.isEmpty()) {
                        if (oldVal !=  null) {
                            if (inst != null) mapping.rmParam(inst, oldVal);
                            else              mapping.rmParam(oldVal);
                        }
                        if (newVal != null) {
                            if (inst != null) mapping.setParam(inst, sonifiable, newVal, eparam);
                            else              mapping.setParam(sonifiable, newVal, eparam);
                        }
                        enableBtnIfValid();
                    }
                    currentlyUpdatingCB = false;
                } catch (AppError e) {
                    paramCB.getSelectionModel().select(oldStr);
                    displayError(e.getMessage(), "Interner Fehler");
                }
            });

            children.add(instCB);
            children.add(paramCB);
        }
    }

    private Pane createSharePane(Sonifiable sonifiable, boolean showMapping) { // initialize and dek the Share Pane
        mapping.addSonifiable(sonifiable);
        updateDateRange();

        Pane stockPane = new Pane();
        stockPane.getStyleClass().add("stockPane");
        stockPane.setUserData(sonifiable.getId());
        Label tField = new Label();
        tField.setText(sonifiable.getName());
        tField.getStyleClass().add("txtField");
        tField.setLayoutX(168);
        tField.setLayoutY(8);
        stockPane.getChildren().add(tField);

        ImageView closeIcon = new ImageView(closeImg);
        double paneWidth = 738; // see css for width value
        double iconSideLen = 30;
        double iconMargin = 15;
        closeIcon.setFitHeight(iconSideLen);
        closeIcon.setFitWidth(iconSideLen);
        closeIcon.setLayoutX(paneWidth - iconSideLen - iconMargin);
        closeIcon.setLayoutY(iconMargin);
        closeIcon.setCursor(Cursor.HAND);
        closeIcon.setOnMouseClicked(ev -> rmSonifiable(sonifiable.getId(), stockPane, true));
        stockPane.getChildren().add(closeIcon);

        addLine(null, 174, 53, 0, 0, 391, 0, stockPane.getChildren());
        addLine("pinkline", 306, 168, -100, -60, -100, 263, stockPane.getChildren());
        addLine("pinkline", 512, 177, -100, -60, -100, 263, stockPane.getChildren());
        addStockParamToPane("Preis", "paneShareLabel", 14, 80, 14, 115, 14, 160, sonifiable, LineData.PRICE, stockPane.getChildren(), showMapping);
        addStockParamToPane("Gleitender Durchschnitt", "paneShareLabel", 14, 215, 14, 250, 14, 295, sonifiable, LineData.MOVINGAVG, stockPane.getChildren(), showMapping);
        addStockParamToPane("Steigungsgrad", "paneShareLabel", 14, 350, 14, 385, 14, 430, sonifiable, LineData.RELCHANGE, stockPane.getChildren(), showMapping);
        addStockParamToPane("Flagge", "paneShareLabel", 226, 80, 226, 115, 226, 160, sonifiable, RangeData.FLAG, stockPane.getChildren(), showMapping);
        addStockParamToPane("Dreieck", "paneShareLabel", 226, 215, 226, 250, 226, 295, sonifiable, RangeData.TRIANGLE, stockPane.getChildren(), showMapping);
        addStockParamToPane("V-Form", "paneShareLabel", 226, 350, 226, 385, 226, 430, sonifiable, RangeData.VFORM, stockPane.getChildren(), showMapping);
        addStockParamToPane("Trendbrüche", "paneShareLabel", 422, 80, 422, 115, 0, 0, sonifiable, PointData.TRENDBREAK, stockPane.getChildren(), showMapping);
        addStockParamToPane("EQMOVINGAVG", "paneShareLabel", 422, 180, 422, 215, 0, 0, sonifiable, PointData.EQMOVINGAVG, stockPane.getChildren(), showMapping);
        addStockParamToPane("EQSUPPORT", "paneShareLabel", 422, 280, 422, 315, 0, 0, sonifiable, PointData.EQSUPPORT, stockPane.getChildren(), showMapping);
        addStockParamToPane("EQRESIST", "paneShareLabel", 422, 380, 422, 415, 0, 0, sonifiable, PointData.EQRESIST, stockPane.getChildren(), showMapping);

        return stockPane;
    }

    private void showMapping() {
        for (Sonifiable s : mapping.getSonifiables()) {
            addToPaneBox(s, true);
        }
        for (String name : mapping.getMappedInstrNames()) {
            instAdded(name);
        }
    }

    private void updateDateRange() {
        Calendar[] minMaxDates = mapping.getDateRange();
        minDateStart = DateUtil.calendarToLocalDate(minMaxDates[0]);
        maxDateEnd = DateUtil.calendarToLocalDate(minMaxDates[1]);
        maxDateStart = maxDateEnd.minusDays(3);
        minDateEnd = minDateStart.minusDays(3);
        updateStartPicker();
        updateEndPicker();
    }

    private void updateStartPicker() {
        startPicker.setDayCellFactory(d -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setDisable(item.isAfter(maxDateStart) || item.isBefore(minDateStart));
            }
        });
    }

    private void updateEndPicker() {
        endPicker.setDayCellFactory(d -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setDisable(item.isAfter(maxDateEnd) || item.isBefore(minDateEnd));
            }
        });
    }

    private void enableBtnIfValid() {
        if (!startedSonification && mapping.isValid())
            startBtn.setDisable(false);
        else
            startBtn.setDisable(true);
    }

    private void instAdded(String name) {
        Label label = new Label(name);
        label.setId("insLabel");
        instBox.getChildren().add(label);
    }

    private void instRemoved(String name) {
        int idx = 0;
        for (Node child : instBox.getChildren()) {
            try {
                if (((Label) child).getText() == name) break;
            } catch (Exception e) {
            }
            idx++;
        }
        if (idx < instBox.getChildren().size())
            instBox.getChildren().remove(idx);
    }
}