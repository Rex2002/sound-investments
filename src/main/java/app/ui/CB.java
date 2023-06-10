package app.ui;

import java.util.Arrays;
import java.util.function.Predicate;

import javafx.scene.Cursor;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import util.ArrayFunctions;
import util.General;

public class CB<T> extends Region {
	protected static final int DEFAULT_CAP = 16;

	protected Font font = new Font("System", 20);
	protected ChoiceBox<String> cb;
	protected String[] keys;
	protected T[] vals;
	protected int size;

	@SuppressWarnings("unchecked")
	public CB(String[] cbKeys, T[] cbValues) {
		assert cbKeys.length == cbValues.length;
		int cap = Math.max(cbKeys.length, DEFAULT_CAP);
		keys    = new String[cap];
		vals    = (T[]) new Object[cap];
		cb      = new ChoiceBox<>();
		cb.setCursor(Cursor.HAND);
		addAll(cbKeys, cbValues);
	}

	public boolean isAnySelected() {
		return getSelectedIdx() >= 0;
	}

	public void disable(boolean b) {
		cb.setDisable(b);
	}

	public ChoiceBox<String> getChoiceBox() {
		return cb;
	}

	public void setChangeListener(Listener<T> listener) {
		cb.getSelectionModel().selectedIndexProperty().addListener((obs, o, n) -> {
			int oi = o.intValue();
			int ni = n.intValue();
			T ox = oi < 0 ? null : vals[oi];
			T nx = ni < 0 ? null : vals[ni];
			listener.changed(ox, nx);
		});
	}

	public T getSelected() {
		int idx = cb.getSelectionModel().getSelectedIndex();
		if (idx < 0) return null;
		else return vals[idx];
	}

	public int getSelectedIdx() {
		return cb.getSelectionModel().getSelectedIndex();
	}

	public void select(int idx) {
		cb.getSelectionModel().select(idx);
	}

	public void select(T obj) {
		if (obj == null) select(-1);
		else select(ArrayFunctions.findIndex(vals, x -> obj.equals(x)));
	}

	public void select(String obj) {
		if (obj == null) select(-1);
		else select(ArrayFunctions.findIndex(keys, x -> obj.equals(x)));
	}

	public void clear() {
		cb.getSelectionModel().select(-1);
		cb.getItems().clear();
		Arrays.fill(keys, 0, size, null);
		Arrays.fill(vals, 0, size, null);
	}

	public void rmSome(Predicate<T> shouldRemove) {
		int rmAmount = 0;
		for (int i = 0; i < size; i++) {
			if (shouldRemove.test(vals[i])) rmAmount++;
			else {
				vals[i - rmAmount] = vals[i];
				keys[i - rmAmount] = keys[i];
			};
		}
		int newSize = size - rmAmount;
		Arrays.fill(vals, newSize, size, null);
		Arrays.fill(keys, newSize, size, null);
		size = newSize;
	}

	public void add(String key, T val) {
		grow(size + 1);
		keys[size] = key;
		vals[size] = val;
		size++;
		cb.getItems().add(key);
	}

	public void addAll(String[] newKeys, T[] newVals) {
		assert newKeys.length == newVals.length;
		grow(size + newKeys.length);
		System.arraycopy(newKeys, 0, keys, size, newKeys.length);
		System.arraycopy(newVals, 0, vals, size, newVals.length);
		size += newKeys.length;
		cb.getItems().addAll(newKeys);
	}

	@SuppressWarnings("unchecked")
	protected void grow(int minCap) {
		if (minCap < keys.length) return;
		String[] newKeys = new String[minCap];
		T[] newVals = (T[]) new Object[minCap];
		System.arraycopy(keys, 0, newKeys, 0, size);
		System.arraycopy(vals, 0, newVals, 0, size);
	}

	public void setFontSize(double size) {
		font = new Font(font.getFamily(), size);
		cb.setStyle("-fx-font: " + General.fontString(font));
	}

	public void layout(double x, double y, double width, double height, double padding) {
		cb.relocate(x, y);
		cb.setPrefSize(width, height);
		setFontSize(height - 2*padding);
	}
}
