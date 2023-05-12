package dataRepo;

import java.util.Date;

public class Stock extends Sonifiable {
	public Stock(String name, String symbol, String exchange) {
		super(name, symbol, exchange);
	}

	public Stock(String name, String symbol, String exchange, Date earliest, Date latest) {
		super(name, symbol, exchange, earliest, latest);
	}
}
