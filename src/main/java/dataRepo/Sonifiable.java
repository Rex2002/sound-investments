package dataRepo;

import java.util.Calendar;
import java.util.Objects;

public abstract class Sonifiable {
	public String name;
	public SonifiableID id;
	public Calendar earliest;
	public Calendar latest;

	public Sonifiable(String name, SonifiableID id) {
		this.name = name;
		this.id = id;
		this.earliest = null;
		this.latest = null;
	}

	public Sonifiable(String name, SonifiableID id, Calendar earliest, Calendar latest) {
		this.name = name;
		this.id = id;
		this.earliest = earliest;
		this.latest = latest;
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

	public Calendar getEarliest() {
		return this.earliest;
	}

	public void setEarliest(Calendar earliest) {
		this.earliest = earliest;
	}

	public Calendar getLatest() {
		return this.latest;
	}

	public void setLatest(Calendar latest) {
		this.latest = latest;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof Sonifiable)) {
			return false;
		}
		Sonifiable sonifiable = (Sonifiable) o;
		return Objects.equals(name, sonifiable.name) && Objects.equals(id, sonifiable.id)
				&& Objects.equals(earliest, sonifiable.earliest)
				&& Objects.equals(latest, sonifiable.latest);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, id, earliest, latest);
	}

	public String toJSON() {
		return "{ " + "\"name\": " + "\"" + name.toString() + "\"" + ", " +
				"\"id\": " + id.toJSON() + ", " +
				"\"earliest\": " + (earliest == null ? "null" : "\"" + DateUtil.formatDate(earliest) + "\"")
				+ ", " +
				"\"latest\": " + (latest == null ? "null" : "\"" + DateUtil.formatDate(latest) + "\"") + " }";
	}

	@Override
	public String toString() {
		return "{" +
				" name='" + getName() + "'" +
				", id='" + getId() + "'" +
				", earliest='" + (earliest == null ? "null"
						: DateUtil.formatDate(earliest))
				+ "'" +
				", latest='" + (latest == null ? "null"
						: DateUtil.formatDate(latest))
				+ "'" +
				"}";
	}
}
