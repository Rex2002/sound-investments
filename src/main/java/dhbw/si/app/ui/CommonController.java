package dhbw.si.app.ui;

import javafx.scene.layout.Pane;
import dhbw.si.util.DateUtil;
import javafx.scene.control.Label;
import javafx.scene.Cursor;
import javafx.scene.control.Button;

/**
 * @author V. Richter
 */
public class CommonController {
	public static void displayError(Pane parent, String errorMessage, String errorTitle) {
        double width = 400;
        Pane errorPane = new Pane();
        errorPane.setId("errorPane");
        errorPane.setLayoutX((parent.getWidth() - width)/2);
        errorPane.setLayoutY(240);
        errorPane.setPrefHeight(300);
        errorPane.toFront();
        errorPane.setPrefWidth(width);
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
        close.setCursor(Cursor.HAND);
        errorPane.getChildren().addAll(errorMes, close, errorTit);
        parent.getChildren().add(errorPane);
    }

    public static String secToMinSecString(double secs, int minStrLen) {
        int quot = (int) (secs / 60);
        int rest = (int) Math.round(secs - quot * 60);
        return DateUtil.paddedParse(quot, minStrLen, '0') + ":" + DateUtil.paddedParse(rest, 2, '0');
    }
}
