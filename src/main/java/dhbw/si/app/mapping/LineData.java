package dhbw.si.app.mapping;

/**
 * @author V. Richter
 * @reviewer J. Kautz
 */
public enum LineData implements ExchangeParam {
	PRICE, // absolute Price
	MOVINGAVG, // moving average
	RELCHANGE; // gradient between two data points

	public static final int size;
	public static final String[] displayVals;
	static {
		LineData[] vals = values();
		size = vals.length;
		displayVals = new String[size];
		for (int i = 0; i < size; i++)
			displayVals[i] = vals[i].toString();
	}

	public String toString() {
		return switch (this) {
			case PRICE     -> "Preis";
			case MOVINGAVG -> "Gleitender Durchschnitt";
			case RELCHANGE -> "Relative Änderung";
		};
	}
}
