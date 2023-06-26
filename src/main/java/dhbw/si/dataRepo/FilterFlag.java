package dhbw.si.dataRepo;

/**
 * @author V. Richter
 */
public enum FilterFlag {
	NONE(0),
	STOCK(1 << 0),
	ETF  (1 << 1),
	INDEX(1 << 2),
	ALL((1 << 0) | (1 << 1) | (1 << 2));

	private final int x;

	FilterFlag(int x) {
		this.x = x;
	}

	public int getVal() {
		return x;
	}

	public static int getFilterVal(FilterFlag... flags) {
		int val = 0;
		for (FilterFlag flag : flags) {
			val |= flag.getVal();
		}
		return val;
	}

	public static FilterFlag fromLeewayString(String s) {
		if (s == null || s.equals("")) return NONE;
		s = s.toLowerCase();
		if (s.endsWith("stock")) return STOCK;
		if (s.endsWith("etf"))   return ETF;
		if (s.endsWith("fund"))  return INDEX;
		return NONE;
	}

	public static FilterFlag fromString(String s) {
		return switch (s) {
			case "Aktie" -> STOCK;
			case "ETF"   -> ETF;
			case "Index" -> INDEX;
			case "Alle"  -> ALL;
			default      -> NONE;
		};
	}

	public String toString() {
		return switch (this) {
			case NONE  -> "";
			case STOCK -> "Aktie";
			case ETF   -> "ETF";
			case INDEX -> "Index";
			case ALL   -> "Alle";
		};
	}
}
