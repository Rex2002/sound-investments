package dataRepo;

import java.util.Date;

public class Index extends Sonifiable {
	public Index(String name, String symbol, String exchange) {
		super(name, symbol, exchange);
	}

	public Index(String name, String symbol, String exchange, Date earliest, Date latest) {
		super(name, symbol, exchange, earliest, latest);
	}
}