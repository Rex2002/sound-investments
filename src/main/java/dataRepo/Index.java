package dataRepo;

import java.util.Calendar;

public class Index extends Sonifiable {
	public Index(String name, SonifiableID id) {
		super(name, id);
	}

	public Index(String name, SonifiableID id, Calendar earliest, Calendar latest) {
		super(name, id, earliest, latest);
	}
}