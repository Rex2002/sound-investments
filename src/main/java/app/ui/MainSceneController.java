package app.ui;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import app.AppError;
import app.communication.*;
import app.mapping.*;
import audio.synth.EvInstrEnum;
import audio.synth.InstrumentEnum;
import dataRepo.FilterFlag;
import dataRepo.Sonifiable;
import dataRepo.SonifiableID;
import util.ArrayFunctions;
import util.DateUtil;
import util.Maths;
import util.UnorderedList;

public class MainSceneController implements Initializable {
    @FXML private AnchorPane        anchor;
    @FXML private Rectangle         headerRect;
    @FXML private Label             headerTitle;
    @FXML private Pane              searchPane;
    @FXML private Label             searchPaneTitle;
    @FXML private Line              searchPaneTitleLine;
    @FXML private TextField         searchBar;
    @FXML private Label             locationLabel;
    @FXML private ChoiceBox<String> locationCB;
    @FXML private Label             categoriesLabel;
    @FXML private ChoiceBox<String> categoriesCB;
    @FXML private ScrollPane        resultsScrollPane;
    @FXML private VBox              resultsVBox;
    @FXML private Pane              sonifiablesPane;
    @FXML private Label             sonifiablesPaneTitle;
    @FXML private Line              sonifiablesPaneTitleLine;
    @FXML private ScrollPane        sonifiablesScrollPane;
    @FXML private VBox              sonifiablesVBox;
    @FXML private Pane              settingsPane;
    @FXML private Label             settingsPaneTitle;
    @FXML private Line              settingsPaneTitleLine;
    @FXML private Label             startDateLabel;
    @FXML private DatePicker        startDatePicker;
    @FXML private Label             endDateLabel;
    @FXML private DatePicker        endDatePicker;
    @FXML private Label             lengthLabel;
    @FXML private TextField         lengthMinField;
    @FXML private TextField         lengthSecField;
    @FXML private Label             lengthSep;
    @FXML private ScrollPane        instScrollPane;
    @FXML private VBox              instVBox;
    @FXML private Label             filterLabel;
    @FXML private ChoiceBox<String> filterCB;
    @FXML private double            duration;
    @FXML private Button            startBtn;

    // These variables are initialized externally
    public Window window;
    public Stage stage;
    public Scene scene;

    private List<Label>     titles;
    private List<Label>     paneTitles;
    private List<Label>     minorTitles;
    private List<Label>     defaultValuesLabels;
    private List<TextField> defaultValuesTFs;
    private List<TextField> searchBars;
    private List<Button>    btns;

    // We are using arrays of size 1 to practically have pointers at those values
    // which means we can effectively pass the values by reference instead of by value
    private Font[] headerFont       = { new Font("Rage Italic", 70) };
    private Font[] paneTitleFont    = { new Font("System Bold", 30) };
    private Font[] minorTitleFont   = { new Font("System", 25)      };
    private Font[] defaultValueFont = { new Font("System", 20)      };
    private Font[] searchBarFont    = { new Font("System", 20)      };
    private Font[] btnFont          = { new Font("System", 40)      };

    // The actual height is apparently stage.getHeight() - heightOffset
    // I have no idea why that's the case, but I through experimentation
    // I found that we need some offset like that, and that its value
    // is approximately 38 (at least on my machine)
    private final double heightOffset = 38;
    private double winSidePad;           // Side-Padding of Window
    private double paneTBMargin;         // Top-Bottom margin of panes
    private double paneLRMargin;         // Left-Right margin of panes
    private double paneY;                // Y position of panes
    private double paneHeight;           // Height of panes
    private double paneTitleHeight;      // Height of the pane titles
    private double paneTitleTBPad;       // Top-Bottom padding for pane titles
    private double paneTitleLineXOffset; // offset in x-direction that the paneTitleLine has relative to the paneTitle
    private double btnHeight;            // Height of the start button
    private double btnPad;               // Padding in the start button
    private double btnPaneMargin;        // Margin between Button and Pane
    private double valueBoxHeight;
    private double valueBoxPad;

    private LocalDate minDateStart = LocalDate.now().minusMonths(3);
    private LocalDate maxDateStart = LocalDate.now().minusDays(3);
    private LocalDate minDateEnd   = LocalDate.now().minusMonths(3).plusDays(3);
    private LocalDate maxDateEnd   = LocalDate.now();

    private Timer loadingAnimTimer;
    private ImageView loading;
    private Image closeImg;
    private CheckEQService checkEQService;
    private Mapping mapping                = new Mapping();
    private boolean currentlyUpdatingCB    = false;
    private boolean startedSonification    = false;

    private String[] locations = { "Deutschland" }; // TODO: Get available locations from StateManager

    // Choice-Box String->Value Maps
    private static String[]         categoryKeys   = { "Alle Kategorien", "Aktien", "ETFs", "Indizes" };
    private static FilterFlag[]     categoryValues = { FilterFlag.ALL, FilterFlag.STOCK, FilterFlag.ETF, FilterFlag.INDEX };
    private static String[]         filterKeys     = { "Low-Pass", "High-Pass" };
    private static boolean[]        filterValues   = { false, true };
    private static String[]         instKeys;
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
    @SuppressWarnings("unchecked")
    public void initialize(URL arg0, ResourceBundle arg1) {
        Platform.runLater(() -> {
            // @Performance Buffer resizing listeners
            initFontList();
            layout(stage.getWidth(), stage.getHeight() - heightOffset);
            stage.widthProperty().addListener((obs, oldWidth, newWidth) -> layout(newWidth.doubleValue(), stage.getHeight() - heightOffset));
            stage.heightProperty().addListener((obs, oldHeight, newHeight) -> layout(stage.getWidth(), newHeight.doubleValue() - heightOffset));
            // @Perfomance Since not all preliminary values needed for proper layouting may have been computed by JavaFX before
            // we run the layouting twice in the beginning. This might decrease startup-speed, but doesn't seem too bad at the moment
            Platform.runLater(() -> layout(stage.getWidth(), stage.getHeight() - heightOffset));
        });

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
                        int categoryIdx = ArrayFunctions.findIndex(categoryValues,
                                c -> c.equals(filter.categoryFilter));
                        categoriesCB.getSelectionModel().select(categoryIdx);
                    }
                    case ERROR -> {
                        startedSonification = false;
                        CommonController.displayError(anchor, (String) msg.data, "Interner Fehler");
                        if (loadingAnimTimer != null)
                            loadingAnimTimer.cancel();
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

        setDatePickerListeners(startDatePicker, true);
        setDatePickerListeners(endDatePicker, false);
        // Set default values
        startDatePicker.valueProperty().setValue(LocalDate.now().minusMonths(1));
        endDatePicker.valueProperty().setValue(LocalDate.now());

        lengthMinField.textProperty().addListener((o, oldVal, newVal) -> updateSoundLength());
        lengthSecField.textProperty().addListener((o, oldVal, newVal) -> updateSoundLength());
        // Set default values
        lengthMinField.textProperty().setValue("30");
        lengthSecField.textProperty().setValue("0");

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
                loadingAnimTimer = new Timer();
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

        filterCB.setCursor(Cursor.HAND);
        locationCB.setCursor(Cursor.HAND);
        categoriesCB.setCursor(Cursor.HAND);

        mapping.setOnInstrAdded(inst -> instAdded(inst.toString()));
        mapping.setOnEvInstrAdded(inst -> instAdded(inst.toString()));
        mapping.setOnInstrRemoved(inst -> instRemoved(inst.toString()));
        mapping.setOnEvInstrRemoved(inst -> instRemoved(inst.toString()));
    }

    private void initFontList() {
        titles              = new UnorderedList<>(headerTitle);
        paneTitles          = new UnorderedList<>(searchPaneTitle, sonifiablesPaneTitle, settingsPaneTitle);
        minorTitles         = new UnorderedList<>(locationLabel, categoriesLabel, startDateLabel, endDateLabel, lengthLabel, filterLabel);
        defaultValuesLabels = new UnorderedList<>(lengthSep);
        defaultValuesTFs    = new UnorderedList<>(lengthMinField, lengthSecField);
        searchBars          = new UnorderedList<>(searchBar);
        btns                = new UnorderedList<>(startBtn);
    }

    private void setFonts() {
        for (Label title : titles)
            title.setFont(headerFont[0]);
        for (Label paneTitle : paneTitles)
            paneTitle.setFont(paneTitleFont[0]);
        for (Label minorTitle : minorTitles)
            minorTitle.setFont(minorTitleFont[0]);
        for (Label defaultValue : defaultValuesLabels)
            defaultValue.setFont(defaultValueFont[0]);
        for (TextField defaultValue : defaultValuesTFs)
            defaultValue.setFont(defaultValueFont[0]);
        for (TextField searchBar : searchBars)
            searchBar.setFont(searchBarFont[0]);
        for (Button btn : btns)
            btn.setFont(btnFont[0]);
    }

    // font is expected to be an array of size 1, to have it passed by reference
    // See also https://stackoverflow.com/a/1068786/13764271 on that topic
    private void setFontSize(Font[] font, double size) {
        font[0] = new Font(font[0].getName(), size);
    }

    private void layout(double width, double height) {
        anchor.setPrefSize(width, height);
        double headerHeight      = Maths.clamp(height/6d, 30, 100);
        double headerPad         = Maths.clamp(headerHeight/6d, 0, 20);
        double headerTitleHeight = headerHeight - 2*headerPad;
        setFontSize(headerFont, headerTitleHeight);
        headerRect.setWidth(width);
        headerRect.setHeight(headerHeight);
        headerTitle.setLayoutX(headerPad);
        headerTitle.setLayoutY(Math.max(0, headerHeight - headerTitle.getHeight()) / 2);

        winSidePad           = Maths.clamp(width /80d, 0, 40);
        paneTBMargin         = Maths.clamp(height/50d, 0, 40);
        paneLRMargin         = Maths.clamp(width /70d, 5, 40);
        paneY                = headerHeight + paneTBMargin;
        paneHeight           = height - headerHeight - 2*paneTBMargin;
        paneTitleHeight      = Maths.clamp(paneHeight       /15d,  10, 40);
        paneTitleTBPad       = Maths.clamp(paneTitleHeight  /2d,   5,  40);
        paneTitleLineXOffset = Maths.clamp(width            /300d, 0,  10);
        btnHeight            = Maths.clamp(paneHeight       /8d,   30, 80);
        btnPad               = Maths.clamp(btnHeight        /8d,   2,  10);
        btnPaneMargin        = Maths.clamp(paneLRMargin,           2,  15);
        valueBoxHeight       = Maths.clamp(paneTitleHeight*3/2d,   10, 50);
        valueBoxPad          = Maths.clamp(valueBoxHeight   /5d,   2,   8);

        layoutPane(searchPane,      searchPaneTitle,      searchPaneTitleLine,       0d/4d, 1d/4d, 0d,                          width);
        layoutPane(sonifiablesPane, sonifiablesPaneTitle, sonifiablesPaneTitleLine,  1d/4d, 2d/4d, 0d,                          width);
        layoutPane(settingsPane,    settingsPaneTitle,    settingsPaneTitleLine,     3d/4d, 1d/4d, btnHeight + 2*btnPaneMargin, width);

        startBtn.relocate(settingsPane.getLayoutX(), settingsPane.getLayoutY() + settingsPane.getPrefHeight() + btnPaneMargin);
        startBtn.setPrefSize(settingsPane.getPrefWidth(), btnHeight);

        double searchPaneWidth     = searchPane.getPrefWidth();
        double searchPaneLRPad     = Maths.clamp(searchPaneWidth/10d, 2, 20);
        double searchPaneValTBPad  = Maths.clamp(paneHeight     /10d, 2, 25);
        searchBar.relocate   (searchPaneLRPad,                     searchPaneTitleLine.getLayoutY() + paneTitleTBPad);
        searchBar.setPrefSize(searchPaneWidth - 2*searchPaneLRPad, valueBoxHeight*4/3);
        double searchFilterCBX     = searchPaneWidth/2 + searchPaneLRPad/2;
        double searchFilterCBWidth = searchPaneWidth/2 - 1.5*searchPaneLRPad;
        locationLabel.  relocate(searchPaneLRPad, searchBar.getLayoutY()     + searchBar.getPrefHeight()     + searchPaneValTBPad*2/3);
        categoriesLabel.relocate(searchPaneLRPad, locationLabel.getLayoutY() + locationLabel.getPrefHeight() + searchPaneValTBPad);
        locationCB.     relocate(searchFilterCBX, locationLabel.getLayoutY());
        categoriesCB.   relocate(searchFilterCBX, categoriesLabel.getLayoutY());
        locationCB.  setPrefSize(searchFilterCBWidth, valueBoxHeight);
        categoriesCB.setPrefSize(searchFilterCBWidth, valueBoxHeight);
        locationCB.  setMinSize(locationCB.  getPrefWidth(), locationCB.  getPrefHeight());
        locationCB.  setMaxSize(locationCB.  getPrefWidth(), locationCB.  getPrefHeight());
        categoriesCB.setMinSize(categoriesCB.getPrefWidth(), categoriesCB.getPrefHeight());
        categoriesCB.setMaxSize(categoriesCB.getPrefWidth(), categoriesCB.getPrefHeight());
        resultsScrollPane.relocate(searchPaneLRPad, categoriesCB.getLayoutY() + categoriesCB.getPrefHeight() + searchPaneValTBPad);
        resultsScrollPane.setPrefSize(searchPaneWidth - 2*searchPaneLRPad, paneHeight - resultsScrollPane.getLayoutY() - searchPaneValTBPad);
        // resultsScrollPane.setPrefSize();
        // resultsScrollPane.relocate();
        // resultsVBox.setPrefSize();
        // resultsVBox.relocate();
        // sonifiablesPane.setPrefSize();
        // sonifiablesPane.relocate();
        // sonifiablesPaneTitle.setPrefSize();
        // sonifiablesPaneTitle.relocate();
        // sonifiablesPaneTitleLine.setPrefSize();
        // sonifiablesPaneTitleLine.relocate();
        // sonifiablesScrollPane.setPrefSize();
        // sonifiablesScrollPane.relocate();
        // sonifiablesVBox.setPrefSize();
        // sonifiablesVBox.relocate();
        // settingsPane.setPrefSize();
        // settingsPane.relocate();
        // settingsPaneTitle.setPrefSize();
        // settingsPaneTitle.relocate();
        // settingsPaneTitleLine.setPrefSize();
        // settingsPaneTitleLine.relocate();
        // startDateLabel.setPrefSize();
        // startDateLabel.relocate();
        // startDatePicker.setPrefSize();
        // startDatePicker.relocate();
        // endDateLabel.setPrefSize();
        // endDateLabel.relocate();
        // endDatePicker.setPrefSize();
        // endDatePicker.relocate();
        // lengthLabel.setPrefSize();
        // lengthLabel.relocate();
        // lengthMinField.setPrefSize();
        // lengthMinField.relocate();
        // lengthSecField.setPrefSize();
        // lengthSecField.relocate();
        // lengthSep.setPrefSize();
        // lengthSep.relocate();
        // instScrollPane.setPrefSize();
        // instScrollPane.relocate();
        // instVBox.setPrefSize();
        // instVBox.relocate();
        // filterLabel.setPrefSize();
        // filterLabel.relocate();
        // filterCB.setPrefSize();
        // filterCB.relocate();
        // duration.setPrefSize();
        // duration.relocate();
        // startBtn.setPrefSize();
        // startBtn.relocate();

        setFontSize(btnFont,          btnHeight - 2*btnPad);
        setFontSize(paneTitleFont,    paneTitleHeight);
        setFontSize(defaultValueFont, valueBoxHeight - 2*valueBoxPad);
        setFontSize(searchBarFont,    searchBar.getPrefHeight() - 2*valueBoxPad);
        setFonts();
    }

    // relXPos, relWidth are in relation to the total window width,
    // e.g. relXPos == 1/4 would put the start x-position in the second quarter of the window,
    // while relWidth == 1/4 would make it a quarter of the total width wide
    // both of these calculations are actually a bit more complex, as they involve padding as well
    // heightSub gives the exact amount of pixels that should be subtracted from the default pane-height
    private void layoutPane(Pane pane, Label paneTitle, Line paneTitleLine, double relXPos, double relWidth, double heightSub, double totalWidth) {
        double x = winSidePad + (totalWidth - 2 * winSidePad) * relXPos;
        double width = (totalWidth - 2 * winSidePad) * relWidth - paneLRMargin;
        pane.relocate(x, paneY);
        pane.setPrefWidth(width);
        pane.setPrefHeight(paneHeight - heightSub);

        double paneTitleX  = width/2 - paneTitle.getWidth()/2;
        double lineYOffset = Maths.clamp(paneTitleHeight /5, 2, 15);
        double lineY       = paneTitleTBPad + paneTitleHeight + lineYOffset;
        paneTitle.relocate(paneTitleX, paneTitleTBPad);
        paneTitle.setPrefHeight(paneTitleHeight);
        paneTitleLine.setLayoutX(paneTitleX - paneTitleLineXOffset);
        paneTitleLine.setLayoutY(lineY);
        paneTitleLine.setEndX(width/2 + paneTitle.getWidth()/2 + paneTitleLineXOffset - paneTitleLine.getLayoutX());
    }

    private void setDatePickerListeners(DatePicker datePicker, boolean isStartDate) {
        datePicker.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                LocalDate date = DateUtil.localDateFromGermanDateStr(newValue);
                datePicker.setValue(date);
            } catch (ParseException e) {}
        });
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (isStartDate)
                    mapping.setStartDate(DateUtil.localDateToCalendar(newValue));
                else
                    mapping.setEndDate(DateUtil.localDateToCalendar(newValue));
                enableBtnIfValid();
            } catch (Exception e) {
                // TODO: Error Handling
            }
        });
    }

    private void updateSoundLength() {
        if (Integer.parseInt(lengthMinField.getText()) <= 59) {
            if (lengthSecField.getText() != null) {
                Integer minValue = 0;
                try {
                    minValue = Integer.parseInt(lengthSecField.getText());
                } catch (NumberFormatException e) {
                }

                Integer passValue = minValue * 60;
                try {
                    passValue += Integer.parseInt(lengthMinField.getText());
                } catch (NumberFormatException e) {
                }

                mapping.setSoundLength(passValue);
                duration = passValue;
            }
        } else {
            // falsche Eingabe
            lengthMinField.setText(null);
            lengthMinField.setPromptText("0-59");
        }
        enableBtnIfValid();
    }

    public void switchToMusicScene(MusicData musicData) {
        try {
            checkEQService.cancel();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MusicScene.fxml"));
            Parent root = loader.load();
            MusicSceneController controller = loader.getController();
            controller.passData(musicData);
            controller.window = window;
            controller.scene = scene;
            controller.stage = stage;
            scene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            CommonController.displayError(anchor, "Fehler beim Laden der nächsten UI-Szene", "Interner Fehler");
        }
    }

    void clearCheckList() {
        resultsVBox.getChildren().clear();
    }

    public void addToCheckList(Sonifiable sonifiable) {
        CheckBox cBox = new CheckBox(sonifiable.getName());
        cBox.setCursor(Cursor.HAND);
        cBox.setUserData(sonifiable);

        if (mapping.hasSonifiable(sonifiable.getId())) {
            cBox.setSelected(true);
        }
        cBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                if (sonifiablesVBox.getChildren().size() < Mapping.MAX_SONIFIABLES_AMOUNT) {
                    addToPaneBox((Sonifiable) cBox.getUserData());
                } else {
                    CommonController.displayError(anchor,
                            "Zu viele Börsenkurse gewählt. Es dürfen höchstens "
                                    + Mapping.MAX_SONIFIABLES_AMOUNT + " Börsenkurse gewählt werden.",
                            "Zu viele Börsenkurse");
                    cBox.setSelected(false);
                }
            } else {
                rmSonifiable(((Sonifiable) cBox.getUserData()).getId(), false);
            }
        });
        resultsVBox.setPrefHeight((resultsVBox.getChildren().size()) * 42.0);
        resultsVBox.getChildren().add(cBox);
    }

    private void rmSonifiable(SonifiableID id, boolean updateSearchResult) {
        ObservableList<Node> children = sonifiablesVBox.getChildren();
        int idx = 0;
        while (idx < children.size() && !id.equals(children.get(idx).getUserData()))
            idx++;
        if (idx == children.size()) {
            System.out.println("rmSonifiable was called on " + id + " which couldn't be found in SceneTree.");
            return;
        }
        rmSonifiable(id, idx, updateSearchResult);
    }

    private void rmSonifiable(SonifiableID id, Pane stockPane, boolean updateSearchResult) {
        rmSonifiable(id, sonifiablesVBox.getChildren().indexOf(stockPane), updateSearchResult);
    }

    private void rmSonifiable(SonifiableID id, int paneIdx, boolean updateSearchResult) {
        mapping.rmSonifiable(id);
        enableBtnIfValid();

        sonifiablesVBox.prefHeight(sonifiablesVBox.getChildren().size() * 511.0);
        sonifiablesVBox.getChildren().remove(paneIdx);
        if (updateSearchResult) {
            ObservableList<Node> checkBoxes = resultsVBox.getChildren();
            for (int i = 0; i < checkBoxes.size(); i++) {
                try {
                    CheckBox c = (CheckBox) checkBoxes.get(i);
                    if (((Sonifiable) c.getUserData()).getId() == id) {
                        c.setSelected(false);
                        break;
                    }
                } catch (ClassCastException e) {
                }
            }
        }
    }

    public Pane addToPaneBox(Sonifiable sonifiable) {
        return addToPaneBox(sonifiable, false);
    }

    public Pane addToPaneBox(Sonifiable sonifiable, boolean showMapping) {
        // add a Sharepanel to the Panel Box
        // Checking whether the maximum of sharePanels has already been reached must be
        // done before calling this function
        Pane sonifiablePane = createSharePane(sonifiable, showMapping);
        sonifiablesVBox.getChildren().add(sonifiablePane);
        sonifiablesVBox.setPrefHeight((sonifiablesVBox.getChildren().size()) * 511.0);
        return sonifiablePane;
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
            int cb2Y, Sonifiable sonifiable, ExchangeParam eparam, ObservableList<Node> children, boolean showMapping) {
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
            evInstCB.setCursor(Cursor.HAND);
            if (showMapping) {
                for (EvInstrMapping evInstMap : mapping.getEventInstruments()) {
                    if (evInstMap.getData().getData().equals(eparam)
                            && evInstMap.getData().getId().equals(sonifiable.getId())) {
                        evInstCB.getSelectionModel().select(evInstMap.getInstrument().toString());
                        break;
                    }
                }
            }
            evInstCB.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    if (oldValue != null)
                        mapping.rmEvInstr(sonifiable.getId(), (PointData) eparam);
                    if (newValue != null)
                        mapping.addEvInstr(EvInstrEnum.fromString(newValue), sonifiable, (PointData) eparam);
                } catch (AppError e) {
                    CommonController.displayError(anchor, e.getMessage(), "Interner Fehler");
                }
            });
            children.add(evInstCB);

        } else {
            boolean isLineParam = (eparam instanceof LineData);

            ChoiceBox<String> instCB = new ChoiceBox<>();
            instCB.getItems().addAll(instKeys);
            instCB.setLayoutX(cb1X);
            instCB.setLayoutY(cb1Y);
            instCB.setCursor(Cursor.HAND);

            ChoiceBox<String> paramCB = new ChoiceBox<>();
            paramCB.setLayoutX(cb2X);
            paramCB.setLayoutY(cb2Y);
            paramCB.setCursor(Cursor.HAND);
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
                    // If the new instrument already has the selected parameter mapped or if
                    // newValue is null
                    // then we need to remove the parameter in the UI
                    // otherwise we also need to set the parameter in the mapping
                    // in any case, we need to remove the old mapping with the old instrument
                    if (currentlyUpdatingCB)
                        return;
                    currentlyUpdatingCB = true;
                    InstrumentEnum newValue = instVals[Math.max(newIdx.intValue(), 0)];
                    InstrumentEnum oldValue = instVals[Math.max(oldIdx.intValue(), 0)];
                    SingleSelectionModel<String> paramCBSelect = paramCB.getSelectionModel();
                    InstrParam paramVal = paramCBSelect.getSelectedItem() == null ? null
                            : InstrParam.fromString(paramCBSelect.getSelectedItem());

                    paramCB.setDisable(newIdx.intValue() <= 0);
                    refreshParamOpts(paramCB, newValue, isLineParam, true);
                    if (newValue != null && paramVal != null) {
                        mapping.rmParam(oldValue, paramVal);
                        if (!mapping.isMapped(newValue, paramVal))
                            mapping.setParam(newValue, sonifiable, paramVal, eparam);
                    }
                    enableBtnIfValid();
                    currentlyUpdatingCB = false;
                } catch (AppError e) {
                    CommonController.displayError(anchor, e.getMessage(), "Interner Fehler");
                }
            });
            paramCB.setOnMouseClicked(ev -> {
                if (currentlyUpdatingCB)
                    return;
                currentlyUpdatingCB = true;
                refreshParamOpts(paramCB, instVals[Math.max(instCB.getSelectionModel().getSelectedIndex(), 0)],
                        isLineParam, false);
                currentlyUpdatingCB = false;
                paramCB.show();
            });
            paramCB.getSelectionModel().selectedItemProperty().addListener((observable, oldStr, newStr) -> {
                try {
                    if (currentlyUpdatingCB)
                        return;
                    currentlyUpdatingCB = true;
                    SelectionModel<String> instCBSelect = instCB.getSelectionModel();
                    InstrumentEnum inst = instVals[Math.max(0, instCBSelect.getSelectedIndex())];
                    InstrParam oldVal = InstrParam.fromString(oldStr);
                    InstrParam newVal = InstrParam.fromString(newStr);

                    if (!instCBSelect.isEmpty()) {
                        if (oldVal != null) {
                            if (inst != null)
                                mapping.rmParam(inst, oldVal);
                            else
                                mapping.rmParam(oldVal);
                        }
                        if (newVal != null) {
                            if (inst != null)
                                mapping.setParam(inst, sonifiable, newVal, eparam);
                            else
                                mapping.setParam(sonifiable, newVal, eparam);
                        }
                        enableBtnIfValid();
                    }
                    currentlyUpdatingCB = false;
                } catch (AppError e) {
                    paramCB.getSelectionModel().select(oldStr);
                    CommonController.displayError(anchor, e.getMessage(), "Interner Fehler");
                }
            });

            children.add(instCB);
            children.add(paramCB);
        }
    }

    private void refreshParamOpts(ChoiceBox<String> paramCB, InstrumentEnum instVal, boolean isLineParam,
            boolean checkForMapping) {
        try {
            SingleSelectionModel<String> paramCBSelect = paramCB.getSelectionModel();
            InstrParam paramVal = paramCBSelect.getSelectedItem() == null ? null
                    : InstrParam.fromString(paramCBSelect.getSelectedItem());
            paramCBSelect.select(null);
            paramCB.getItems().clear();
            InstrParam[] newOpts;
            Function<InstrParam, InstrParam[]> getOpts = (pv) -> isLineParam ? mapping.getEmptyLineParams(instVal, pv)
                    : mapping.getEmptyRangeParams(instVal, pv);
            boolean flag = instVal != null && paramVal != null
                    && (!checkForMapping || !mapping.isMapped(instVal, paramVal));
            if (flag)
                newOpts = getOpts.apply(paramVal);
            else
                newOpts = getOpts.apply(null);

            paramCB.getItems().add(null);
            for (InstrParam opt : newOpts)
                paramCB.getItems().add(opt.toString());

            if (flag)
                paramCBSelect.select(paramVal.toString());
            // System.out.println("Selected: " + paramCBSelect.getSelectedItem());
        } catch (AppError e) {
            CommonController.displayError(anchor, e.getMessage(), "Interner Fehler");
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
        addStockParamToPane("Preis", "paneShareLabel", 14, 80, 14, 115, 14, 160, sonifiable, LineData.PRICE,
                stockPane.getChildren(), showMapping);
        addStockParamToPane("Gleitender Durchschnitt", "paneShareLabel", 14, 215, 14, 250, 14, 295, sonifiable,
                LineData.MOVINGAVG, stockPane.getChildren(), showMapping);
        addStockParamToPane("Steigungsgrad", "paneShareLabel", 14, 350, 14, 385, 14, 430, sonifiable,
                LineData.RELCHANGE, stockPane.getChildren(), showMapping);
        addStockParamToPane("Flagge", "paneShareLabel", 226, 80, 226, 115, 226, 160, sonifiable, RangeData.FLAG,
                stockPane.getChildren(), showMapping);
        addStockParamToPane("Dreieck", "paneShareLabel", 226, 215, 226, 250, 226, 295, sonifiable, RangeData.TRIANGLE,
                stockPane.getChildren(), showMapping);
        addStockParamToPane("V-Form", "paneShareLabel", 226, 350, 226, 385, 226, 430, sonifiable, RangeData.VFORM,
                stockPane.getChildren(), showMapping);
        addStockParamToPane("Trendbrüche", "paneShareLabel", 422, 80, 422, 115, 0, 0, sonifiable, PointData.TRENDBREAK,
                stockPane.getChildren(), showMapping);
        addStockParamToPane("EQMOVINGAVG", "paneShareLabel", 422, 180, 422, 215, 0, 0, sonifiable,
                PointData.EQMOVINGAVG, stockPane.getChildren(), showMapping);
        addStockParamToPane("EQSUPPORT", "paneShareLabel", 422, 280, 422, 315, 0, 0, sonifiable, PointData.EQSUPPORT,
                stockPane.getChildren(), showMapping);
        addStockParamToPane("EQRESIST", "paneShareLabel", 422, 380, 422, 415, 0, 0, sonifiable, PointData.EQRESIST,
                stockPane.getChildren(), showMapping);

        return stockPane;
    }

    private void showMapping() {
        for (Sonifiable s : mapping.getSonifiables()) {
            addToPaneBox(s, true);
        }
        for (String name : mapping.getMappedInstrNames()) {
            instAdded(name);
        }

        startDatePicker.setValue(DateUtil.calendarToLocalDate(mapping.getStartDate()));
        endDatePicker.setValue(DateUtil.calendarToLocalDate(mapping.getEndDate()));
        System.out.println("SoundLength: " + mapping.getSoundLength());

        String min = Integer.toString((int) (mapping.getSoundLength() / 60));
        String sec = Integer.toString(mapping.getSoundLength() % 60);
        lengthSecField.setText(min);
        lengthMinField.setText(sec);
        assert filterValues[0] == false;
        filterCB.getSelectionModel().select(mapping.getHighPass() ? 1 : 0);
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
        startDatePicker.setDayCellFactory(d -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setDisable(item.isAfter(maxDateStart) || item.isBefore(minDateStart));
            }
        });
    }

    private void updateEndPicker() {
        endDatePicker.setDayCellFactory(d -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setDisable(item.isAfter(maxDateEnd) || item.isBefore(minDateEnd));
            }
        });
    }

    private void enableBtnIfValid() {
        startBtn.setDisable(startedSonification || !mapping.isValid());
    }

    private void instAdded(String name) {
        Label label = new Label(name);
        label.setId("insLabel");
        instVBox.getChildren().add(label);
    }

    private void instRemoved(String name) {
        int idx = 0;
        for (Node child : instVBox.getChildren()) {
            try {
                if (((Label) child).getText() == name)
                    break;
            } catch (Exception e) {
            }
            idx++;
        }
        if (idx < instVBox.getChildren().size())
            instVBox.getChildren().remove(idx);
    }
}