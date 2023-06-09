package dhbw.si.app.mapping;

import dhbw.si.audio.events.EvInstrEnum;
import lombok.Data;

/**
 * @author V. Richter
 */
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
}