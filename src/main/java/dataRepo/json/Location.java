package dataRepo.json;

public class Location {
	public String file;
	public int row;
	public int col;

	public Location(String file, int row, int col) {
		this.file = file;
		this.row = row;
		this.col = col;
	}

	public String toString() {
		return file + ":" + row + ":" + col;
	}
}
