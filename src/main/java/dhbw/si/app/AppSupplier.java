package dhbw.si.app;

/**
 * @author V. Richter
 */
@FunctionalInterface
public interface AppSupplier<T> {
	T call() throws AppError;
}