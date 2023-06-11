package dhbw.si.dataRepo;

import lombok.Data;

@Data
public class Sonifiable {
	public String name;
	public SonifiableID id;

	public Sonifiable(String name, SonifiableID id) {
		this.name = name.replace('"', '\'');
		this.id = id;
	}

	public String getCompositeName() {
		return name + " (" + id.symbol + ")";
	}

	public String toJSON() {
		return "{ " + "\"name\": " + "\"" + name + "\"" + ", " +
				"\"id\": " + id.toJSON() + " }";
	}
}
