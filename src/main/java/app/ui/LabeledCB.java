package app.ui;

import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;

public class LabeledCB<T> extends Region implements LabeledCBLayout {
	protected Font labelFont = new Font("System bold", 20);
	protected Label label;
	protected CB<T> cb;

	public LabeledCB(String text, String[] cbKeys, T[] cbValues) {
		label = new Label(text);
		cb    = new CB<>(cbKeys, cbValues);
		getChildren().addAll(label, cb);
	}

	public Label getLabel() {
		return label;
	}

	public CB<T> getCB() {
		return cb;
	}

	public void setLabelFontSize(double size) {
		labelFont = new Font(labelFont.getName(), size);
		label.setFont(labelFont);
	}

	public void setFontSizes(double labelFontSize, double cbFontSize) {
		setLabelFontSize(labelFontSize);
		cb.setFontSize(cbFontSize);
	}

	public void layout(double x, double y, double width, double height, double labelFontSize, double cbFontSize, double cbPad, double cbMargin, double labelMargin) {
		relocate(x, y);
		setPrefSize(width, height);

		double requiredHeight = labelFontSize + cbMargin + labelMargin +  cbFontSize + 2*cbPad;
		double labelY         = (height - requiredHeight)/2;
		double cbHeight       = cbFontSize + 2*cbPad;
		double cbY            = labelY + labelFontSize + cbMargin + labelMargin;

		setLabelFontSize(labelFontSize);
		label.relocate((width - label.getWidth())/2, labelY);
		cb.layout(0, cbY, width, cbHeight, cbPad);
	}
}
