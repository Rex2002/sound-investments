package apiTest;

import java.util.Objects;

public class Stock {
	public final String name;
	public final String code;
	public final String exchange;
	public final String type;

	public Stock(String name, String code, String exchange, String type) {
		this.name = name;
		this.code = code;
		this.exchange = exchange;
		this.type = type;
	}
}
