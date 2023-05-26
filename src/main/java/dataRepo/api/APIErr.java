package dataRepo.api;

public class APIErr extends Exception {
	public int status;
	public String body;

	public APIErr(int status, String body) {
		this.status = status;
		this.body = body;
	}
}
