package dataRepo;

import java.util.Date;

public class ETF extends Sonifiable {
	public ETF(String name, String symbol, String exchange) {
		super(name, symbol, exchange);
	}

	public ETF(String name, String symbol, String exchange, Date earliest, Date latest) {
		super(name, symbol, exchange, earliest, latest);
	}
}