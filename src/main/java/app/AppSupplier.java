package app;

@FunctionalInterface
public interface AppSupplier<T> {
	public T call() throws AppError;
}
