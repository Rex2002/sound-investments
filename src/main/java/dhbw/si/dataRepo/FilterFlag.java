package dhbw.si.dataRepo;

public enum FilterFlag {
	STOCK(1),
	ETF(1 << 1),
	INDEX(1 << 2),
	ALL(1 | (1 << 1) | (1 << 2));

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
}
