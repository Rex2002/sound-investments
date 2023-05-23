package dataRepo;

import java.util.Calendar;

public class Stock extends Sonifiable {
	public Stock(String name, String symbol, String exchange) {
		super(name, symbol, exchange);
	}

	public Stock(String name, String symbol, String exchange, Calendar earliest, Calendar latest) {
		super(name, symbol, exchange, earliest, latest);
	}
}
