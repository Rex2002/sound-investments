package dataRepo;

import java.util.Objects;

import util.General;

// @Cleanup let Lombok create this boilerplate
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

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public SonifiableID getId() {
		return this.id;
	}

	public void setId(SonifiableID id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof Sonifiable)) {
			return false;
		}
		Sonifiable sonifiable = (Sonifiable) o;
		return Objects.equals(name, sonifiable.name) && Objects.equals(id, sonifiable.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, id);
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
