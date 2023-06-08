package app;

@FunctionalInterface
public interface AppFunction {
	void call() throws AppError;
}