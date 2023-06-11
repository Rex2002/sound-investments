package dhbw.si.dataRepo.api;

public class StrTuple {
	private final String[] h = { "\0", "\0" };

	public StrTuple(String first, String second) {
		h[0] = first;
		h[1] = second;
	}

	public String getFirst() {
		return h[0];
	}

	public String getSecond() {
		return h[1];
	}

	public void setFirst(String first) {
		h[0] = first;
	}

	public void setSecond(String second) {
		h[1] = second;
	}

	public String[] getArr() {
		return h;
	}
}
