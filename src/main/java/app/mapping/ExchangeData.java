package app.mapping;

import java.util.Objects;

import dataRepo.SonifiableID;
import lombok.Data;

@Data
public class ExchangeData<T extends ExchangeParam> {
	private SonifiableID id;
	private T data;

	public ExchangeData(SonifiableID id, T data) {
		this.id = id;
		this.data = data;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ExchangeData<T> ed = (ExchangeData<T>) obj;
		return Objects.equals(id, ed.id) && Objects.equals(data, ed.data);
	}
}
