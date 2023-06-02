package dataRepo.api;

public class APIErr extends Exception {
	public int status;
	public String body;

	public APIErr(int status, String body) {
		super("Error in API-Request: status=" + status + ", body=" + body);
		this.status = status;
		this.body = body;
	}
}
