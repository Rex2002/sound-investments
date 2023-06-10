package dataRepo;

import java.time.Instant;

public enum IntervalLength {
	MIN,
	HOUR,
	DAY;

	public Instant addToInstant(Instant x) {
		long millis = 1000;
		millis *= switch (this) {
			case MIN -> 60;
			case HOUR -> 60 * 60;
			case DAY -> 12 * 60 * 60;
		};
		long res = x.toEpochMilli() + millis;
		return Instant.ofEpochMilli(res);
	}

	@SuppressWarnings("SwitchStatementWithTooFewBranches")
	public String toString(API api) {
		return switch (this) {
			case MIN -> switch (api) {
				case LEEWAY -> "5m";
				default -> "5min";
			};
			case HOUR -> switch (api) {
				case MARKETSTACK -> "1hour";
				default -> "1h";
			};
			case DAY -> switch (api) {
				case LEEWAY -> null; // Leeway doesn't support 24hour intraday -> use /eod endpoint instead
				case MARKETSTACK -> "24hour";
				case TWELVEDATA -> "1day";
			};
		};
	}
}
