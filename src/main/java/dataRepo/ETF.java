package dataRepo;

import java.util.Calendar;

public class ETF extends Sonifiable {
	public ETF(String name, String symbol, String exchange) {
		super(name, symbol, exchange);
	}

	public ETF(String name, String symbol, String exchange, Calendar earliest, Calendar latest) {
		super(name, symbol, exchange, earliest, latest);
	}
}