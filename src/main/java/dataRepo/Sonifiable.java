package dataRepo;

import java.util.Calendar;
import java.util.Objects;

public abstract class Sonifiable {
	public String name;
	public String symbol;
	public String exchange;
	public Calendar earliest;
	public Calendar latest;

	public Sonifiable(String name, String symbol, String exchange) {
		this.name = name;
		this.symbol = symbol;
		this.exchange = exchange;
		this.earliest = null;
		this.latest = null;
	}

	public Sonifiable(String name, String symbol, String exchange, Calendar earliest, Calendar latest) {
		this.name = name;
		this.symbol = symbol;
		this.exchange = exchange;
		this.earliest = earliest;
		this.latest = latest;
	}

	public String getSymbolExchange() {
		return symbol + "." + exchange;
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

	public Calendar getEarliest() {
		return this.earliest;
	}

	public void setEarliest(Calendar earliest) {
		this.earliest = earliest;
	}

	public Calendar getLatest() {
		return this.latest;
	}

	public void setLatest(Calendar latest) {
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
				", earliest='" + Util.formatDate(getEarliest()) + "'" +
				", latest='" + Util.formatDate(getLatest()) + "'" +
				"}";
	}
}
