package app.mapping;

public enum LineData implements ExchangeParam {
	PRICE, // Absoluter Preis des Kurses
	MOVINGAVG, // Gleitender Durchschnitt wie in 2.3 beschrieben
	RELCHANGE; // Steigung zwischen zwei Preispunkten

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
			case PRICE -> "Preis";
			case MOVINGAVG -> "Gleitender Durchschnitt";
			case RELCHANGE -> "Relative Änderung";
		};
	}
}
