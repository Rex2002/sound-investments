package dataRepo;

import java.util.Date;
import java.util.Objects;

public abstract class Sonifiable {
	public String name;
	public String symbol;
	public String exchange;
	public Date earliest;
	public Date latest;

	public Sonifiable(String name, String symbol, String exchange) {
		this.name = name;
		this.symbol = symbol;
		this.exchange = exchange;
		this.earliest = null;
		this.latest = null;
	}

	public Sonifiable(String name, String symbol, String exchange, Date earliest, Date latest) {
		this.name = name;
		this.symbol = symbol;
		this.exchange = exchange;
		this.earliest = earliest;
		this.latest = latest;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSymbol() {
		return this.symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getExchange() {
		return this.exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	public Date getEarliest() {
		return this.earliest;
	}

	public void setEarliest(Date earliest) {
		this.earliest = earliest;
	}

	public Date getLatest() {
		return this.latest;
	}

	public void setLatest(Date latest) {
		this.latest = latest;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof Sonifiable)) {
			return false;
		}
		Sonifiable sonifiable = (Sonifiable) o;
		return Objects.equals(name, sonifiable.name) && Objects.equals(symbol, sonifiable.symbol)
				&& Objects.equals(exchange, sonifiable.exchange) && Objects.equals(earliest, sonifiable.earliest)
				&& Objects.equals(latest, sonifiable.latest);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, symbol, exchange, earliest, latest);
	}

	@Override
	public String toString() {
		return "{" +
				" name='" + getName() + "'" +
				", symbol='" + getSymbol() + "'" +
				", exchange='" + getExchange() + "'" +
				", earliest='" + getEarliest() + "'" +
				", latest='" + getLatest() + "'" +
				"}";
	}
}
