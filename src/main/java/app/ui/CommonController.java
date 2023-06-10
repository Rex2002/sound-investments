package app.ui;

import javafx.scene.layout.Pane;
import util.DateUtil;
import javafx.scene.control.Label;
import javafx.scene.control.Button;

public class CommonController {
	public static void displayError(Pane parent, String errorMessage, String errorTitle) {
        Pane errorPane = new Pane();
        errorPane.setId("errorPane");
        errorPane.setLayoutX(593);
        errorPane.setLayoutY(262.5);
        errorPane.setPrefHeight(300);
        errorPane.toFront();
        errorPane.setPrefWidth(400);
        Label errorMes = new Label(errorMessage);
        errorMes.setId("errorMessage");
        errorMes.setLayoutY(40);
        errorMes.setWrapText(true);
        
        errorMes.setLayoutX(20);
        Label errorTit = new Label(errorTitle);
        errorTit.setId("errorTitle");
        errorTit.setLayoutY(20);
        errorTit.setLayoutX(30);
        
        errorTit.setWrapText(true);
        Button close = new Button("Schließen");
        close.setOnMouseClicked(event ->{
                parent.getChildren().remove(errorPane);
        });
        close.setLayoutX(257.5);
        close.setLayoutY(20);
        close.setId("closeBtn");
        errorPane.getChildren().addAll(errorMes, close, errorTit);
        parent.getChildren().add(errorPane);
    }

    public static String secToMinSecString(double secs, int minStrLen) {
        int quot = (int) (secs / 60);
        int rest = (int) Math.round(secs - quot * 60);
        return DateUtil.paddedParse(quot, minStrLen, '0') + ":" + DateUtil.paddedParse(rest, 2, '0');
    }
}
