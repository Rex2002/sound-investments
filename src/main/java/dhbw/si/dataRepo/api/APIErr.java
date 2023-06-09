package dhbw.si.dataRepo.api;

/**
 * @author V. Richter
 */
public class APIErr extends Exception {
	public final int status;
	public final String body;

	public APIErr(int status, String body) {
		super("Error in API-Request: status=" + status + ", body=" + body);
		this.status = status;
		this.body = body;
	}
}
