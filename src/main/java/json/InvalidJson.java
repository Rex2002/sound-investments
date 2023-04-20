package json;

public class InvalidJson extends Error {
	public InvalidJson(String msg, Location loc) {
		super("Error at " + loc.toString() + ":" + "\n" + msg);
	}
}
