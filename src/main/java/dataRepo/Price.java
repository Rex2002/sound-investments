package dataRepo;

import lombok.Data;

import java.time.Instant;
import java.util.Calendar;
import java.util.Objects;

@Data
public class Price {
	public Calendar day;
	public Instant start;
	public Instant end;
	public Double open;
	public Double close;
	public Double low;
	public Double high;

	public Price(Calendar day, Instant start, Instant end, Double open, Double close, Double low, Double high) {
		this.day = day;
		this.start = start;
		this.end = end;
		this.open = open;
		this.close = close;
		this.low = low;
		this.high = high;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof Price price)) {
			return false;
		}
		return Objects.equals(day, price.day) && Objects.equals(start, price.start) && Objects.equals(end, price.end)
				&& Objects.equals(open, price.open) && Objects.equals(close, price.close)
				&& Objects.equals(low, price.low) && Objects.equals(high, price.high);
	}

	@Override
	public String toString() {
		return "{" +
				" day='" + getDay() + "'" +
				", start='" + getStart() + "'" +
				", end='" + getEnd() + "'" +
				", open='" + getOpen() + "'" +
				", close='" + getClose() + "'" +
				", low='" + getLow() + "'" +
				", high='" + getHigh() + "'" +
				"}";
	}
}
