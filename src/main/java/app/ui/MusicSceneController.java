package app.ui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import audio.synth.playback.PlaybackController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.Node;
import app.communication.EventQueues;
import app.communication.Msg;
import app.communication.MsgToSMType;
import app.communication.MusicData;

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
	private Slider musicSlider;
	@FXML
	private Parent root;
	@FXML
	private ImageView playBtn;
	private PlaybackController pbc;
	private String[] sonifiableNames;
	private double[][] prices;
	private Timer myTimer = new Timer();
	private boolean paused = false;
	private Image playImage;
	private Image pauseImage;

	// TODO: Add playback button image for play, stop, forward, backward, reset
	// TODO: Make line-chart colors the same as in the legend

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		playImage = new Image(getClass().getResource("/pause_btn.png").toString());
		pauseImage = new Image(getClass().getResource("/pause_btn.png").toString());

		Platform.runLater(() -> {
			setupSlider();
			beginTimer();
			addbtn(pauseImage, 1050, 669).setOnMouseClicked(ev -> pausePlaySound());
		});
	}

	public void passData(MusicData musicData) {
		this.pbc = musicData.pbc;
		this.sonifiableNames = musicData.sonifiableNames;
		this.prices = musicData.prices;

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
			addSonifiableName(sonifiableNames[i], colors[i], 50 + (i % 4) * 240, 646 + ((int) (i / 4)) * 50);
		}

		// Show price data in line chart
		xAxis.setAutoRanging(false);
		xAxis.setLowerBound(0);
		xAxis.setUpperBound(prices[0].length);
		xAxis.setOpacity(0);
		lineChart.setLegendVisible(false);
		lineChart.setAnimated(false);
		lineChart.setVerticalGridLinesVisible(false);
		lineChart.setHorizontalGridLinesVisible(true);
 		lineChart.setCreateSymbols(false);
		for (int i = 0; i < prices.length; i++) {
			XYChart.Series<Integer, Double> series = new XYChart.Series<>();
			for (int j = 0; j < prices[i].length; j++) {
				series.getData().add(new XYChart.Data<>(j, prices[i][j]));
			}
			lineChart.getData().add(series);
		}
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

	private ImageView addbtn(Image img, double x, double y) {
		playBtn = new ImageView(img);
		playBtn.setCache(true);
		playBtn.setFitHeight(80);
		playBtn.setFitWidth(80);
		playBtn.setLayoutX(x);
		playBtn.setLayoutY(y);
		anchor.getChildren().add(playBtn);
		return playBtn;
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
		// Tell StateManager, that we are back in the main scene again
		try {
		EventQueues.toSM.put(new Msg<>(MsgToSMType.BACK_IN_MAIN_SCENE));
		} catch (InterruptedException ie) {
			// TODO: Error Handling
		}

		root = FXMLLoader.load(getClass().getResource("/MainScene.fxml"));
		stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		scene = new Scene(root);
		String css = this.getClass().getResource("/choice.css").toExternalForm();
		// Set the stylesheet after the scene creation
		scene.getStylesheets().add(css);
		stage.setScene(scene);
		stage.show();
	}

	public void pausePlaySound() {
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

	public void stopSound(ActionEvent event) throws IOException {
		onClose();
		switchToMainScene(event);
	}

	public void beginTimer() {
		myTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				musicSlider.setValue(pbc.getPlayedPercentage() * musicSlider.getMax());
			}
		}, 0, 50);

	}

}
