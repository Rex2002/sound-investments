package app.ui;

import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;

public class LabeledDoubleCB<T, S> extends Region implements LabeledCBLayout {
	protected Font labelFont = new Font("System bold", 20);
	protected Label label;
	protected CB<T> cb1;
	protected CB<S> cb2;

	public LabeledDoubleCB(String text, String[] keys1, T[] vals1, String[] keys2, S[] vals2) {
		label = new Label(text);
		cb1   = new CB<>(keys1, vals1);
		cb2   = new CB<>(keys2, vals2);
		getChildren().addAll(label, cb1, cb2);
	}

	public Label getLabel() {
		return label;
	}

	public CB<T> getCB1() {
		return cb1;
	}

	public CB<S> getCB2() {
		return cb2;
	}

	public void setLabelFontSize(double size) {
		labelFont = new Font(labelFont.getName(), size);
		label.setFont(labelFont);
	}

	public void setFontSizes(double labelFontSize, double cbFontSize) {
		setLabelFontSize(labelFontSize);
		cb1.setFontSize(cbFontSize);
		cb2.setFontSize(cbFontSize);
	}

	public void layout(double x, double y, double width, double height, double labelFontSize, double cbFontSize, double cbPad, double cbMargin, double labelMargin) {
		relocate(x, y);
		setPrefSize(width, height);

		double requiredHeight = labelFontSize + labelMargin + 2*(cbMargin + cbFontSize + 2*cbPad);
		double labelY         = (height - requiredHeight)/2;
		double cbHeight       = cbFontSize + 2*cbPad;
		double cb1Y           = labelY + labelFontSize + labelMargin + cbMargin;
		double cb2Y           = cb1Y + cbHeight + labelMargin;

		setLabelFontSize(labelFontSize);
		label.relocate((width - label.getWidth())/2, labelY);
		cb1.layout(0, cb1Y, width, cbHeight, cbPad);
		cb2.layout(0, cb2Y, width, cbHeight, cbPad);
	}
}
