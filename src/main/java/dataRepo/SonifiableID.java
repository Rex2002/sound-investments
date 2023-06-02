package dataRepo;

import java.util.Objects;

public class SonifiableID {
	public String exchange;
	public String symbol;

	public SonifiableID(String symbol, String exchange) {
		this.symbol = symbol;
		this.exchange = exchange;
	}

	public String getExchange() {
		return this.exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	public String getSymbol() {
		return this.symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof SonifiableID)) {
			return false;
		}
		SonifiableID sonifiableID = (SonifiableID) o;
		return Objects.equals(exchange, sonifiableID.exchange) && Objects.equals(symbol, sonifiableID.symbol);
	}

	@Override
	public String toString() {
		return this.symbol + "." + this.exchange;
	}
}
