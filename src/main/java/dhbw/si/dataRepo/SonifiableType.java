package dhbw.si.dataRepo;

public enum SonifiableType {
	NONE,
	STOCK,
	ETF,
	INDEX;

	public static SonifiableType fromString(String s) {
		if (s == null || s.equals("")) return NONE;
		s = s.toLowerCase();
		if (s.endsWith("stock")) return STOCK;
		if (s.endsWith("etf")) return ETF;
		if (s.endsWith("fund")) return INDEX;
		return NONE;
	}

	public String toString() {
		return switch (this) {
			case NONE -> "";
			case STOCK -> "Aktie";
			case ETF -> "ETF";
			case INDEX -> "Index";
		};
	}
}
