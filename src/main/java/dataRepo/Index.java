package dataRepo;

import java.util.Calendar;

public class Index extends Sonifiable {
	public Index(String name, String symbol, String exchange) {
		super(name, symbol, exchange);
	}

	public Index(String name, String symbol, String exchange, Calendar earliest, Calendar latest) {
		super(name, symbol, exchange, earliest, latest);
	}
}