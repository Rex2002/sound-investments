package app.mapping;

import dataRepo.SonifiableID;

public class ExchangeData<T extends ExchangeParam> {
	private SonifiableID id;
	private T data;

	public ExchangeData(SonifiableID id, T data) {
		this.id = id;
		this.data = data;
	}

	public SonifiableID getId() {
		return this.id;
	}

	public void setId(SonifiableID id) {
		this.id = id;
	}

	public T getData() {
		return this.data;
	}

	public void setData(T data) {
		this.data = data;
	}

}
