package state;

import util.AppError;

@FunctionalInterface
public interface AppSupplier<T> {
	public T call() throws AppError;
}
