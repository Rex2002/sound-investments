package dataRepo;

import java.util.Objects;

// @Cleanup let Lombok create this boilerplate
public class Sonifiable {
	public String name;
	public SonifiableID id;

	public Sonifiable(String name, SonifiableID id) {
		this.name = name;
		this.id = id;
	}

	public Stock asStock() {
		return new Stock(name, id);
	}

	public ETF asETF() {
		return new ETF(name, id);
	}

	public Index asIndex() {
		return new Index(name, id);
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
