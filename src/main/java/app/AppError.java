package app;

public class AppError extends Throwable {
	String message;

	AppError(String msg) {
		this.message = msg;
	}

	public String getMessage() {
		return this.message;
	}
}
