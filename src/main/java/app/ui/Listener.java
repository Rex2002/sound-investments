package app.ui;

@FunctionalInterface
public interface Listener<T> {
	void changed(T oldVal, T newVal);
}
