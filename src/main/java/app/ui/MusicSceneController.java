package app.ui;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javafx.css.converter.StringConverter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.Node;

public class MusicSceneController implements Initializable {

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

	@FXML
	// Wahrscheinlich irgendwie zwei deminsionales Array oder so
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		addData();
		musicSlider.setOnMouseReleased(event -> {
            test.setText(String.valueOf(musicSlider.getValue()));
        });
		//übergabe der Kurse wie viele usw mit Statemanager oder per Scene
	}
	private void startSlider(LocalDate startDate, LocalDate endDate){
		//Übergebene Daten, von MainScene 
		musicSlider.setMinorTickCount(0);
		long daysBetween = Duration.between(startDate, endDate).toDays();
		musicSlider.setMajorTickUnit(daysBetween);
		musicSlider.setBlockIncrement(daysBetween/10);
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

	public void pausePlaySound() {
		// Image wechsel und an den Stage manager infos weitergeben
		//pause timer
	}

	public void stopSound() {
	}
	
	public void beginTimer() {

		Timer timer = new Timer();

		TimerTask task = new TimerTask() {

			public void run() {
				// songProgressBar.setProgress(current/end);

				/*
				 * if(current/end == 1) {
				 *
				 * cancelTimer();
				 * }
				 */
			}

		};

		// timer.scheduleAtFixedRate(task, 0, 1000);

	}

	public void cancelTimer() {
		// timer.cancel();
	}
}
