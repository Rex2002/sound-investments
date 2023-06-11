package dhbw.si.app;

@FunctionalInterface
public interface AppSupplier<T> {
	T call() throws AppError;
}