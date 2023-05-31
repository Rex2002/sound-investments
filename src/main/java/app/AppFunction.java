package app;

@FunctionalInterface
public interface AppFunction {
	public void call() throws AppError;
}
