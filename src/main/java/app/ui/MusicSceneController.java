package app.ui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
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

import app.communication.MusicData;

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
	private LineChart lineChart;
	private PlaybackController pbc;
	private String[] sonifiableNames;
	private double[][] prices;
	private Timer myTimer = new Timer();
	@FXML
	private ImageView pBtn;
	private boolean paused;
	File playFile = new File("/pause_btn.png");// Wahrscheinlich Path Problem zeigt nichts
	File pauseFile = new File("/pause_btn.png");
	Image playImage = new Image(playFile.toURI().toString());
	Image pauseImage = new Image(pauseFile.toURI().toString());

	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MM yyyy");

	// TODO: Show the sonifiableNames
	// TODO: Show the graph of prices
	// TODO: Show the playback button images

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Platform.runLater(() -> {
			pbc.startPlayback();
			setupSlider();
			beginTimer();
			addbtn();
		});
		// addbtn();
		// übergabe der Kurse wie viele usw mit Statemanager oder per Scene
	}

	void passData(MusicData musicData) {
		this.pbc = musicData.pbc;
		this.sonifiableNames = musicData.sonifiableNames;
		this.prices = musicData.prices;
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
