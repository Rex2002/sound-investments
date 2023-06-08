package app.ui;

import javafx.scene.layout.Pane;
import javafx.scene.control.Label;
import javafx.scene.control.Button;

public class CommonController {
	public static void displayError(Pane parent, String errorMessage, String errorTitle) {
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
                parent.getChildren().remove(errorPane);
        });
        close.setLayoutX(330);
        close.setLayoutY(20);
        close.setId("closeBtn");
        errorPane.getChildren().addAll(errorMes, close, errorTit);
        parent.getChildren().add(errorPane);

    }
}
