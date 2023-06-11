package dhbw.si.dataRepo;

import lombok.Data;

@Data
public class Sonifiable {
	public String name;
	public SonifiableID id;
	public FilterFlag type;

	public Sonifiable(String name, SonifiableID id, FilterFlag type) {
		this.name = name.replace('"', '\'');
		this.id   = id;
		this.type = type;
	}

	public String getCompositeName() {
		return name + " (" + id.symbol + ")";
	}

	public String toJSON() {
		return "{ " + "\"name\": " + "\"" + name + "\"" + ", " +
				"\"id\": " + id.toJSON() + ", \"type\": \"" + type + "\" }";
	}
}
