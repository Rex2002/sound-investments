package dataRepo;

import java.util.Calendar;

public class ETF extends Sonifiable {
	public ETF(String name, SonifiableID id) {
		super(name, id);
	}

	public ETF(String name, SonifiableID id, Calendar earliest, Calendar latest) {
		super(name, id, earliest, latest);
	}
}