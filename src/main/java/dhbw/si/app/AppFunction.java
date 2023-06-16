package dhbw.si.app;

/**
 * @author V. Richter
 */
@FunctionalInterface
public interface AppFunction {
	void call() throws AppError;
}