package dhbw.si.dataRepo.json;

/**
 * @author V. Richter
 */
public class Location {
	public final String file;
	public final int row;
	public final int col;

	public Location(String file, int row, int col) {
		this.file = file;
		this.row = row;
		this.col = col;
	}

	public String toString() {
		return file + ":" + row + ":" + col;
	}
}
