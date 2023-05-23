package app.mapping;

import java.util.Optional;

import audio.synth.InstrumentEnum;

public class InstrumentMapping {
	InstrumentEnum instrument;
	// Volume
	Optional<ExchangeData<LineData>> relVolume;
	Optional<ExchangeData<RangeData>> absVolume;
	// Pitch
	ExchangeData<LineData> pitch;
	// Echo
	Optional<ExchangeData<LineData>> delayEcho;
	Optional<ExchangeData<LineData>> feedbackEcho;
	Optional<ExchangeData<RangeData>> onOffEcho;
	// Reverb parameters
	Optional<ExchangeData<LineData>> delayReverb;
	Optional<ExchangeData<LineData>> feedbackReverb;
	Optional<ExchangeData<RangeData>> onOffReverb;
	// Filter parameters
	Optional<ExchangeData<LineData>> cutoff;
	Optional<ExchangeData<LineData>> order;
	Optional<ExchangeData<RangeData>> onOffFilter;
	boolean highPass;
	// Panning
	Optional<ExchangeData<LineData>> pan;
}
