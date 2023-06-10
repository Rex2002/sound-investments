package app.mapping;

import audio.events.EvInstrEnum;
import lombok.Data;

@Data
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

	@Override
	public String toString() {
		return "{" +
				" instrument='" + this.instrument + "'" +
				", data='" + this.data + "'" +
				"}";
	}
}