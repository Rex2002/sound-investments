package app.ui;

import app.AppError;
import app.communication.*;
import audio.synth.playback.PlaybackController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import util.ArrayFunctions;
import util.DateUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class MusicSceneController implements Initializable {
	// Colors have to be kept in sync with colors in css file
	// In css the colors are specified under .default-color-<x>.chart-series-line
	// TODO: Find a way to set the colors of the chart lines without css
	// so we don't need to keep both places updated
	private static Paint[] colors = {
		Paint.valueOf("#ff1fec"), Paint.valueOf("#071d32"), Paint.valueOf("#3b4854"), Paint.valueOf("#ff5a1f"),
		Paint.valueOf("#e3ff1f"), Paint.valueOf("#7a4d69"), Paint.valueOf("#1fff43"), Paint.valueOf("#ff2121"),
		Paint.valueOf("#891fff"), Paint.valueOf("#07321d")
	};

	@FXML
	private AnchorPane anchor;
	@FXML
	private LineChart<Integer, Double> lineChart;
	@FXML
	private NumberAxis xAxis;
	@FXML
	private NumberAxis yAxis;
	@FXML
	private Pane legendPane;
	@FXML
	private TextField headerTitle;
	@FXML
	private Stage stage;
	@FXML
	private Scene scene;
	@FXML
	private Button exportBtn;
	@FXML
	private Button closeBtn;
	@FXML
	private Slider musicSlider;
	@FXML
	private Parent root;
	@FXML
	private ImageView playBtn;
	@FXML
	private ImageView stopBtn;
	@FXML
	private ImageView backBtn;
	@FXML
	private ImageView forBtn;

	private CheckEQService checkEQService;
	private PlaybackController pbc;
	private String[] sonifiableNames;
	private List<XYChart.Series<Integer, Double>> prices;
	private Calendar[] dates;
	private Timer myTimer = new Timer();
	private boolean paused = false;
	private Image playImage;
	private Image pauseImage;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		exportBtn.setOnMouseClicked(ev -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Exportiere Audio-stream");
			fileChooser.setInitialFileName("Börsen-Sonifizierung.wav");
			fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Wave-Dateien", "*.wav"));
			File selectedFile = fileChooser.showSaveDialog(exportBtn.getScene().getWindow());
			if (selectedFile != null) {
				try {
					pbc.save(selectedFile);
				} catch (AppError e) {
					e.printStackTrace();
					CommonController.displayError(anchor, e.getMessage(), "Interner Fehler");
				}
			}
		});

		playImage = new Image(getClass().getResource("/play_btn.png").toString());
		pauseImage = new Image(getClass().getResource("/pause_btn.png").toString());
		playBtn.setImage(pauseImage);
		playBtn.setOnMouseClicked(ev -> this.pausePlaySound());
		forBtn.setOnMouseClicked(ev -> pbc.skipForward());
		backBtn.setOnMouseClicked(ev -> pbc.skipBackward());
		stopBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					try {
						stopSound(event);
						playBtn.setImage(playImage);
					} catch (IOException e) {
						// TODO: Error Handling
						e.printStackTrace();
					}
				}
		});
		playBtn.setCursor(Cursor.HAND);
		forBtn.setCursor(Cursor.HAND);
		backBtn.setCursor(Cursor.HAND);
		stopBtn.setCursor(Cursor.HAND);
		exportBtn.setCursor(Cursor.HAND);
		closeBtn.setCursor(Cursor.HAND);

		checkEQService = new CheckEQService();
        checkEQService.setPeriod(Duration.millis(100));
        checkEQService.setOnSucceeded((event) -> {
            List<Msg<MsgToUIType>> messages = checkEQService.getValue();
            for (Msg<MsgToUIType> msg : messages) {
                switch (msg.type) {
                    case ERROR -> {
                        CommonController.displayError(anchor, (String) msg.data, "Interner Fehler");
                        paused = false;
						this.pausePlaySound();
                    }
					default -> {
						System.out.println("MusicScene received a message of type " + msg.type);
						// Put the message back
						// The assumption here is, that we only get other messages when we are switching back to the main scene, so when we put the messages back into the queue again, we assume to not see them again
						EventQueues.toUI.add(msg);
					}
                }
            }
        });
        checkEQService.start();

		Platform.runLater(() -> {
			setupSlider();
			beginTimer();
		});
	}

	public void passData(MusicData musicData) {
		this.pbc = musicData.pbc;
		this.sonifiableNames = musicData.sonifiableNames;
		this.prices = musicData.prices;
		this.dates = musicData.dates;

		assert sonifiableNames.length <= colors.length;
		Platform.runLater(() -> {
			setVisualization();
			pbc.startPlayback();
		});
	}

	private void setVisualization() {
		// Show legend of sonifiable names
		legendPane.getChildren().clear();
		for (int i = 0; i < sonifiableNames.length; i++) {
			addSonifiableName(sonifiableNames[i], colors[i], 60 + (i % 3) * 240, 646 + ((int) (i / 3f)) * 50);
		}

		// Show price data in line chart
		int priceListLen = prices.get(0).getData().size();
		xAxis.setAutoRanging(false);
		xAxis.setLowerBound(0);
		xAxis.setUpperBound(priceListLen);
		xAxis.setTickUnit(priceListLen / 7);
		xAxis.setTickLabelFill(Paint.valueOf("white"));
		xAxis.setTickLabelFont(new Font("System", 10));
		xAxis.setTickLabelFormatter(new StringConverter<Number>() {
			public String toString(Number i) {
				return DateUtil.formatDate(ArrayFunctions.clampedArrAccess(i.intValue(), dates));
			}
			public Number fromString(String string) {
				return 0;
			}
		});

		lineChart.setLegendVisible(false);
		lineChart.setAnimated(false);
		lineChart.setVerticalGridLinesVisible(false);
		lineChart.setHorizontalGridLinesVisible(true);
 		lineChart.setCreateSymbols(false);
		lineChart.getData().addAll(prices);
	}

	private Label addSonifiableName(String name, Paint color, double x, double y) {
		Circle circle = new Circle(14.4);
		circle.setFill(color);
		circle.setLayoutX(x);
		circle.setLayoutY(y + 7.2);

		Label label = new Label(name);
		label.setPrefWidth(150);
		label.setLayoutX(x + 35);
		label.setLayoutY(y);
		label.setTextFill(Paint.valueOf("#fefefe"));

		legendPane.getChildren().add(circle);
		legendPane.getChildren().add(label);
		return label;
	}

	private void setupSlider() {
		// My idea was to set a minor tick per millisecond in a second
		// and a major tick per second in a minute
		// (with 5*60 being the maximum supported amount of seconds)
		musicSlider.setMinorTickCount(1000);
		musicSlider.setMajorTickUnit(5*60);
		musicSlider.setOnMouseClicked(ev -> sliderGoto(ev));
		// TODO: call PBC's goto when the slider is being dragged too
	}

	private void sliderGoto(MouseEvent ev) {
		double perc = ev.getX() / musicSlider.getWidth();
		pbc.goToRelative(perc);
	}

	public void switchToMainScene(ActionEvent event) throws IOException {
		try {
			myTimer.cancel();
            checkEQService.cancel();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainScene.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) anchor.getScene().getWindow();
            Scene scene = new Scene(root);
            String css = this.getClass().getResource("/choice.css").toExternalForm();
            // Set the stylesheet after the scene creation
            scene.getStylesheets().add(css);
            stage.setScene(scene);
            stage.show();
			// Tell the StateManager, that we are back in the main scene again
			EventQueues.toSM.put(new Msg<>(MsgToSMType.ENTERED_MAIN_SCENE));
        } catch (IOException e) {
            CommonController.displayError(anchor, "Fehler beim Laden der nächsten UI-Szene", "Interner Fehler");
        } catch (InterruptedException e) {
			Pane pane = anchor;
			if (!root.getChildrenUnmodifiable().isEmpty() && root.getChildrenUnmodifiable().get(0) instanceof Pane)
				pane = (Pane) root.getChildrenUnmodifiable().get(0);
			CommonController.displayError(pane, "Es konnte keine Verbindung mit dem Backend der Anwendung hergestellt werden", "Interner Fehler");
		}


	}

	public void pausePlaySound(){
		if (paused) {
			pbc.play();
			playBtn.setImage(pauseImage);
			paused = false;
		} else {
			pbc.pause();
			playBtn.setImage(playImage);
			paused = true;
			// pause Song
		}

	}

	public void onClose() {
		pbc.kill();
		myTimer.cancel();
	}

	public void stopSound(MouseEvent event) throws IOException {
		pbc.pause();
		paused = true;
		pbc.goToRelative(0);
	}

	public void beginTimer() {
		myTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				musicSlider.setValue(pbc.getPlayedPercentage() * musicSlider.getMax());
			}
		}, 0, 50);

	}
	public void closeWindow(ActionEvent event) throws IOException {
		onClose();
		switchToMainScene(event);
	}
}