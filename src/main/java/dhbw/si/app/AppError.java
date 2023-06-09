package dhbw.si.app;

/**
 * @author V. Richter
 */
public class AppError extends Throwable {
	final String message;

	public AppError(String msg) {
		this.message = msg;
	}

	public String getMessage() {
		return this.message;
	}
}
