package app.mapping;

public enum PointData implements ExchangeParam {
	TRENDBREAK, // Beim Durchbrechen einer Trend-Formation
	EQMOVINGAVG, // Wenn absoluter Preis = gleitender Durchschnitt gilt
	EQSUPPORT, // Wenn absoluter Preis = Unterstützungs-Linie gilt
	EQRESIST; // Wenn absoluter Preis = Widerstands-Linie gilt

	public static final int size;
	public static final String[] displayVals;
	static {
		PointData[] vals = values();
		size = vals.length;
		displayVals = new String[size];
		for (int i = 0; i < size; i++)
			displayVals[i] = vals[i].toString();
	}

	public String toString() {
		return switch (this) {
			case TRENDBREAK -> "Trendlinien-Brüche";
			case EQMOVINGAVG -> "Gleich gleitendem Preis";
			case EQSUPPORT -> "Gleich Support-Linie";
			case EQRESIST -> "Gleich Resist-Linie";
		};
	}
}
