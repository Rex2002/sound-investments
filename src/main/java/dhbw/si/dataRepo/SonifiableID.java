package dhbw.si.dataRepo;

import lombok.Data;

import java.util.Objects;

@Data
public class SonifiableID {
	public String exchange;
	public String symbol;

	public SonifiableID(String symbol, String exchange) {
		this.symbol   = symbol.replace('"', '\'');
		this.exchange = exchange.replace('"', '\'');
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof SonifiableID sonifiableID)) {
			return false;
		}
		return Objects.equals(exchange, sonifiableID.exchange) && Objects.equals(symbol, sonifiableID.symbol);
	}

	@Override
	public String toString() {
		return this.symbol + "." + this.exchange;
	}

	public String toJSON() {
		return "{ \"symbol\": \"" + symbol + "\", " + "\"exchange\": \"" + exchange + "\" " + "}";
	}
}
