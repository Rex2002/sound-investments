package state;

import util.AppError;

@FunctionalInterface
public interface AppFunction {
	public void call() throws AppError;
}
