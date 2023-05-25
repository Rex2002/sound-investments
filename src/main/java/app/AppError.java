package app;

public class AppError extends Throwable {
	String message;

	public AppError(String msg) {
		this.message = msg;
	}

	public String getMessage() {
		return this.message;
	}
}
