package dhbw.si.dataRepo;

import lombok.Data;

import java.util.Objects;

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

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof Sonifiable sonifiable)) {
			return false;
		}
		return Objects.equals(name, sonifiable.name) && Objects.equals(id, sonifiable.id);
	}

	public String toJSON() {
		return "{ " + "\"name\": " + "\"" + name + "\"" + ", " +
				"\"id\": " + id.toJSON() + " }";
	}

	@Override
	public String toString() {
		return "{" +
				" name='" + getName() + "'" +
				", id='" + getId() + "'" +
				"}";
	}
}
