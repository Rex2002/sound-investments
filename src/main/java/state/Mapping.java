package state;

import java.util.Calendar;
import java.util.Optional;

public class Mapping {
	InstrumentMapping[] mappedInstruments;
	EvInstrMapping[] eventInstruments;
	Integer soundLength; // stored as seconds
	// Timeperiod
	Calendar startDate;
	Calendar endDate;
	// Reverb parameters
	Optional<ExchangeData<LineData>> delayReverb;
	Optional<ExchangeData<LineData>> feedbackReverb;
	Optional<ExchangeData<RangeData>> onOffReverb;
	// Filter parameters
	Optional<ExchangeData<LineData>> cutoff;
	Optional<ExchangeData<RangeData>> onOffFilter;
	Boolean highPass;
}
