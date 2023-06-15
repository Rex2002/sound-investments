package dhbw.si.app.mapping;

public enum PointData implements ExchangeParam {
	TRENDBREAK, // breaking a trend formation
	EQMOVINGAVG, // price = moving average
	EQSUPPORT, // price = support line
	EQRESIST; // price = resist line

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
			case TRENDBREAK -> "Trendbrüche";
			case EQMOVINGAVG -> "Preis = Schnitt";
			case EQSUPPORT -> "Preis = Stützt";
			case EQRESIST -> "Preis = Widerst.";
		};
	}
}
