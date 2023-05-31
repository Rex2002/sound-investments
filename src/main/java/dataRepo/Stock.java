package dataRepo;

import java.util.Calendar;

public class Stock extends Sonifiable {
	public Stock(String name, SonifiableID id) {
		super(name, id);
	}

	public Stock(String name, SonifiableID id, Calendar earliest, Calendar latest) {
		super(name, id, earliest, latest);
	}
}
