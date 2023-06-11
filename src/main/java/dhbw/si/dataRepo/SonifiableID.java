package dhbw.si.dataRepo;

import lombok.Data;

@Data
public class SonifiableID {
	public String exchange;
	public String symbol;

	public SonifiableID(String symbol, String exchange) {
		this.symbol   = symbol.replace('"', '\'');
		this.exchange = exchange.replace('"', '\'');
	}

	@Override
	public String toString() {
		return this.symbol + "." + this.exchange;
	}

	public String toJSON() {
		return "{ \"symbol\": \"" + symbol + "\", " + "\"exchange\": \"" + exchange + "\" " + "}";
	}
}
