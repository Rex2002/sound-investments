package util;

import javafx.scene.text.Font;

public class General {
	public static String fontString(Font font) {
		return Integer.toString((int) font.getSize()) + "px '" + font.getName() + "'";
	}
}
