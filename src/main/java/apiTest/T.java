package apiTest;

public class T {
	public Double start;
	public Double end;

	public T(Double start, Double end) {
		this.start = start;
		this.end = end;
	}

	public String toString() {
		return start.toString() + " -> " + end.toString();
	}
}
