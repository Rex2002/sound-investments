package app.mapping;

public enum RangeData implements ExchangeParam {
	FLAG,
	TRIANGLE,
	VFORM;

	public static final int size;
	public static final String[] displayVals;
	static {
		RangeData[] vals = values();
		size = vals.length;
		displayVals = new String[size];
		for (int i = 0; i < size; i++)
			displayVals[i] = vals[i].toString();
	}

	public String toString() {
		return switch (this) {
			case FLAG -> "Flaggenformation";
			case TRIANGLE -> "Dreiecksformation";
			case VFORM -> "V-Formation";
		};
	}
}
