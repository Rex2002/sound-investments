package dhbw.si.dataRepo;

import java.time.Instant;

/**
 * @author V. Richter
 */
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

	public String toString() {
		return switch (this) {
			case MIN -> "5m";
			case HOUR -> "1h";
			case DAY -> null; // In this case leeway expects you to use a different endpoint
		};
	}
}
