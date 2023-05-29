package app.ui;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
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
	private double addDuration;
	private double duration;
	private LocalDateTime startDate;
	private LocalDateTime endDate;
	private long daysBetween;
	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MM yyyy");
	@FXML
	// Wahrscheinlich irgendwie zwei deminsionales Array oder so
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Platform.runLater(() -> {
			startSlider();
			beginTimer();
			test.setText(String.valueOf(daysBetween));
		});
		//übergabe der Kurse wie viele usw mit Statemanager oder per Scene
	}
	void passData(double newDuration, LocalDate start, LocalDate end){
		duration = newDuration;
		startDate = start.atStartOfDay();
		endDate  = end.atStartOfDay();
		daysBetween = Duration.between( startDate, endDate).toDays();
		addDuration = (daysBetween)/(duration);
		addData();
	}
	private void startSlider(){
		//Übergebene Daten, von MainScene 
		musicSlider.setMinorTickCount(0);
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
	Timer myTimer = new Timer();
	public void beginTimer() {

		
          myTimer.schedule(new TimerTask(){

            @Override
            public void run() {
				if(musicSlider.getValue() != duration){
				musicSlider.setValue(musicSlider.getValue()+ addDuration);
				}
				else{
					myTimer.cancel();
				}
            }
          }, 0,1000);
        

	}

	public void cancelTimer() {
		myTimer.cancel();
	}
}
