package dataRepo;

import java.time.Instant;
import java.util.Calendar;
import java.util.Objects;

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

	public Calendar getDay() {
		return this.day;
	}

	public void setDay(Calendar day) {
		this.day = day;
	}

	public Instant getStart() {
		return this.start;
	}

	public void setStart(Instant start) {
		this.start = start;
	}

	public Instant getEnd() {
		return this.end;
	}

	public void setEnd(Instant end) {
		this.end = end;
	}

	public Double getOpen() {
		return this.open;
	}

	public void setOpen(Double open) {
		this.open = open;
	}

	public Double getClose() {
		return this.close;
	}

	public void setClose(Double close) {
		this.close = close;
	}

	public Double getLow() {
		return this.low;
	}

	public void setLow(Double low) {
		this.low = low;
	}

	public Double getHigh() {
		return this.high;
	}

	public void setHigh(Double high) {
		this.high = high;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof Price)) {
			return false;
		}
		Price price = (Price) o;
		return Objects.equals(day, price.day) && Objects.equals(start, price.start) && Objects.equals(end, price.end)
				&& Objects.equals(open, price.open) && Objects.equals(close, price.close)
				&& Objects.equals(low, price.low) && Objects.equals(high, price.high);
	}

	@Override
	public int hashCode() {
		return Objects.hash(day, start, end, open, close, low, high);
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
