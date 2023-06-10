package app.mapping;

import audio.events.EvInstrEnum;

public class EvInstrMapping {
	private EvInstrEnum instrument;
	private ExchangeData<PointData> data;

	public EvInstrMapping(EvInstrEnum instrument, ExchangeData<PointData> data) {
		this.instrument = instrument;
		this.data = data;
	}

	public boolean isEmpty() {
		return data == null;
	}

	/////////
	// Getters & Setters:
	/////////

	public EvInstrEnum getInstrument() {
		return this.instrument;
	}

	public void setInstrument(EvInstrEnum instrument) {
		this.instrument = instrument;
	}

	public ExchangeData<PointData> getData() {
		return this.data;
	}

	public void setData(ExchangeData<PointData> data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "{" +
				" instrument='" + this.instrument + "'" +
				", data='" + this.data + "'" +
				"}";
	}
}