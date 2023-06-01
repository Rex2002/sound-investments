package app.ui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import audio.synth.playback.PlaybackController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.Node;

public class MusicSceneController implements Initializable {
	@FXML
	private Button PlayBtn;
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
	private Label test;
	@FXML
	private LineChart lineChart;
	private PlaybackController pbc;
	private double addDuration;
	private double duration;
	private LocalDateTime startDate;
	private LocalDateTime endDate;
	private long daysBetween;
	@FXML
	private ImageView pBtn;
	private boolean paused;
	File playFile = new File("/pause_btn.png");// Wahrscheinlich Path Problem zeigt nichts
	File pauseFile = new File("/pause_btn.png");
	Image playImage = new Image(playFile.toURI().toString());
	Image pauseImage = new Image(pauseFile.toURI().toString());

	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MM yyyy");

	// TODO: Listen to when the user moves the musicSlider
	// TODO: Kill Playback-Thread when Window is closed (or scene is switched back again)
	// TODO: Get data for visualization from StateManager
	// TODO: Get updated audio length from StateManager (since the audio-length may have been updated in the SM)
	// TODO: Make sure the musicSlider is in sync with the Playback

	@FXML
	// Wahrscheinlich irgendwie zwei dimensionales Array oder so
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Platform.runLater(() -> {
			startSlider();
			pbc.startPlayback();
			beginTimer();
			test.setText(String.valueOf(daysBetween));
			addbtn();
		});
		// addbtn();
		// übergabe der Kurse wie viele usw mit Statemanager oder per Scene
	}

	void passData(PlaybackController pbc, double newDuration, LocalDate start, LocalDate end) {
		this.pbc = pbc;
		duration = newDuration;
		startDate = start.atStartOfDay();
		endDate = end.atStartOfDay();
		daysBetween = Duration.between(startDate, endDate).toDays();
		addDuration = (daysBetween) / (duration);
		addData();
	}

	private void addbtn() {
		try {
			pBtn.setImage(playImage);
			pBtn.setCache(true);
		} catch (Exception e) {

		}
		pBtn.prefHeight(80);
		pBtn.prefWidth(285);
		pBtn.setLayoutY(684);
		pBtn.setLayoutX(1247);
		pBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				if (pBtn.getImage() == playImage) {
					pBtn.setImage(pauseImage);

				} else {
					pBtn.setImage(playImage);
					myTimer.cancel();

					// pause Song
				}
			}
		});
	}

	private void startSlider() {
		// Übergebene Daten, von MainScene
		musicSlider.setMinorTickCount(0);
		musicSlider.setMajorTickUnit(daysBetween);
		musicSlider.setBlockIncrement(daysBetween / 10);
	}

	// Könten den ALLMIGHTY den Kursnamenn eben und dann wirft er ein 2D Array
	// raus(Möglichkeit)
	public void addData() {
		// int x = 0;
		// int y = 0;
		// XYChart.Series series = new XYChart.Series();
		// while (Array groß nocnicht leer){
		//
		// while(Array nochnicht leer){
		// series.getData().add(new XYChart.Data(Array[1], Array[1][x]));
		// x+1
		// }
		// y+1
		// x = 0;
		// }
	}

	public void switchToMainScene(ActionEvent event) throws IOException {
		root = FXMLLoader.load(getClass().getResource("/MainScene.fxml"));
		stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		scene = new Scene(root);
		String css = this.getClass().getResource("/choice.css").toExternalForm();
		// Set the stylesheet after the scene creation
		scene.getStylesheets().add(css);
		stage.setScene(scene);
		stage.show();
	}

	public void pausePlaySound(ActionEvent e) {
		if (paused) {
			pbc.play();
			PlayBtn.setText("Pause");
			paused = false;
		} else {
			pbc.pause();
			PlayBtn.setText("Play");
			paused = true;
			// pause Song
		}

	}

	public void stopSound(ActionEvent event) throws IOException {
		pbc.kill();
		myTimer.cancel();
		myTimer.purge();
		switchToMainScene(event);
	}

	Timer myTimer = new Timer();

	public void beginTimer() {
		myTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (paused == false) {
					if (musicSlider.getValue() != duration) {
						musicSlider.setValue(musicSlider.getValue() + addDuration);
					} else {
						myTimer.cancel();
					}
				}
			}
		}, 0, 1000);

	}

}
