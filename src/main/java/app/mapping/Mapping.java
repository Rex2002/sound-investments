package app.mapping;

import java.util.Calendar;
import java.util.Optional;
import java.util.HashSet;
import java.util.Set;
import app.Util;

import app.AppError;
import audio.synth.EvInstrEnum;
import audio.synth.InstrumentEnum;
import dataRepo.SonifiableID;

public class Mapping {
	public static int MAX_EV_INSTR_SIZE = 10;
	public static int MIN_SOUND_LENGTH = 30;
	public static int MAX_SOUND_LENGTH = 5 * 60;
	public static int MAX_SONIFIABLES_AMOUNT = 10;

	private final InstrumentMapping[] mappedInstruments = new InstrumentMapping[InstrumentEnum.size];
	private EvInstrMapping[] eventInstruments = new EvInstrMapping[MAX_EV_INSTR_SIZE];
	private int evInstrAmount = 0;
	private Integer soundLength = 30; // stored in seconds
	// Timeperiod
	private Calendar startDate = null;
	private Calendar endDate = null;
	// Reverb parameters
	private Optional<ExchangeData<LineData>> delayReverb = Optional.empty();
	private Optional<ExchangeData<LineData>> feedbackReverb = Optional.empty();
	private Optional<ExchangeData<RangeData>> onOffReverb = Optional.empty();
	// Filter parameters
	private Optional<ExchangeData<LineData>> cutoff = Optional.empty();
	private Optional<ExchangeData<RangeData>> onOffFilter = Optional.empty();
	private Boolean highPass = false;

	public Mapping() {
	}

	// Indicates whether the current mapping can already be used for sonification
	public boolean isValid() {
		return verify() == null;
	}

	public String verify() {
		// TODO: Are there any other ways in which the mapping can be invalid?
		// evInstrAmount > MAX_EV_INSTR_SIZE doesn't have to be checked, because the
		// setters don't allow this to happen anyways
		if (startDate == null)
			return "Start-Datum ist nicht gesetzt.";
		if (endDate == null)
			return "End-Datum ist nicht gesetzt.";
		if (soundLength == null || soundLength < MIN_SOUND_LENGTH || soundLength > MAX_SOUND_LENGTH)
			return "Ungültige Audio-Länge gewählt. Die Audio-Länge muss zwischen 30 Sekunden und 5 Minuten liegen.";

		Set<SonifiableID> mappedSonifiables = new HashSet<>(16);
		for (InstrumentMapping instrMap : mappedInstruments) {
			if (instrMap.isEmpty())
				continue;
			if (instrMap.getPitch() == null)
				return "Der Pitch vom Instrument '" + instrMap.getInstrument().toString()
						+ "' wurde nicht auf einen Börsenwert gemappt.";
			mappedSonifiables.addAll(instrMap.getMappedSonifiables());
		}
		if (mappedSonifiables.isEmpty())
			return "Kein Börsenkurs wurde auf ein Instrument gemappt.";
		if (mappedSonifiables.size() > MAX_SONIFIABLES_AMOUNT)
			return "Zu viele Börsenkurse auf einmal im Mapping. Es dürfen maximal "
					+ Integer.toString(MAX_SONIFIABLES_AMOUNT) + " Börsenkurse auf einmal gemappt werden.";
		return null;
	}

	////////
	// Getters & Setters
	////////

	public void addEvInstr(EvInstrEnum instr, SonifiableID sonifiable, PointData eparam) throws AppError {
		if (evInstrAmount == MAX_EV_INSTR_SIZE)
			throw new AppError("Zu viele Event-Instrumente. Ein Mapping darf höchstens "
					+ Integer.toString(MAX_EV_INSTR_SIZE) + " Event-Instrumente haben");
		eventInstruments[evInstrAmount] = new EvInstrMapping(instr, new ExchangeData<>(sonifiable, eparam));
		evInstrAmount++;
	}

	public void rmEvInstr(SonifiableID sonifiable, PointData eparam) {
		assert evInstrAmount > 0 : "Can't remove Event Instruments, when none have been mapped yet";

	}

	@SuppressWarnings("unchecked")
	private static <T extends ExchangeParam> ExchangeData<T> setParamHelper(SonifiableID sonifiable,
			ExchangeParam eparam, InstrParam iparam) throws AppError {
		try {
			return new ExchangeData<T>(sonifiable, (T) eparam);
		} catch (ClassCastException e) {
			throw new AppError(eparam.toString() + " kann nicht auf " + iparam.toString() + "gemappt werden.");
		}
	}

	private static <T extends ExchangeParam> Optional<ExchangeData<T>> setOptParamHelper(SonifiableID sonifiable,
			ExchangeParam eparam, InstrParam iparam) throws AppError {
		return Optional.of(setParamHelper(sonifiable, eparam, iparam));
	}

	public void setParam(InstrumentEnum instr, SonifiableID sonifiable, InstrParam iparam, ExchangeParam eparam)
			throws AppError {
		InstrumentMapping instrMap = Util.find(mappedInstruments, x -> x.getInstrument() == instr);
		switch (iparam) {
			case PITCH -> instrMap.setPitch(setParamHelper(sonifiable, eparam, iparam));
			case RELVOLUME -> instrMap.setRelVolume(setOptParamHelper(sonifiable, eparam, iparam));
			case ABSVOLUME -> instrMap.setAbsVolume(setOptParamHelper(sonifiable, eparam, iparam));
			case DELAY_ECHO -> instrMap.setDelayEcho(setOptParamHelper(sonifiable, eparam, iparam));
			case FEEDBACK_ECHO -> instrMap.setFeedbackEcho(setOptParamHelper(sonifiable, eparam, iparam));
			case ON_OFF_REVERB -> instrMap.setOnOffReverb(setOptParamHelper(sonifiable, eparam, iparam));
			case CUTOFF -> instrMap.setCutoff(setOptParamHelper(sonifiable, eparam, iparam));
			case ORDER -> instrMap.setOrder(setOptParamHelper(sonifiable, eparam, iparam));
			case ON_OFF_FILTER -> instrMap.setOnOffFilter(setOptParamHelper(sonifiable, eparam, iparam));
			case PAN -> instrMap.setPan(setOptParamHelper(sonifiable, eparam, iparam));
			default ->
				throw new AppError(eparam.toString() + " kann nicht auf " + iparam.toString() + "gemappt werden.");
		}
	}

	public void setParam(SonifiableID sonifiable, InstrParam iparam, ExchangeParam eparam) throws AppError {
		switch (iparam) {
			case DELAY_REVERB -> this.delayReverb = setOptParamHelper(sonifiable, eparam, iparam);
			case FEEDBACK_REVERB -> this.feedbackReverb = setOptParamHelper(sonifiable, eparam, iparam);
			case ON_OFF_REVERB -> this.onOffReverb = setOptParamHelper(sonifiable, eparam, iparam);
			case CUTOFF -> this.cutoff = setOptParamHelper(sonifiable, eparam, iparam);
			case ON_OFF_FILTER -> this.onOffFilter = setOptParamHelper(sonifiable, eparam, iparam);
			default ->
				throw new AppError(eparam.toString() + " kann nicht auf " + iparam.toString() + "gemappt werden.");
		}
	}

	public void setHighPass(InstrumentEnum instr, boolean highpass) {
		InstrumentMapping instrMap = Util.find(mappedInstruments, x -> x.getInstrument() == instr);
		instrMap.setHighPass(highpass);
	}

	public void setHighPass(boolean highpass) {
		this.highPass = highpass;
	}

	public void setSoundLength(int len) {
		this.soundLength = len;
	}

	public void setStartDate(Calendar startDate) {
		this.startDate = startDate;
	}

	public void setEndDate(Calendar endDate) {
		this.endDate = endDate;
	}

	public InstrumentMapping[] getMappedInstruments() {
		return this.mappedInstruments;
	}

	public EvInstrMapping[] getEventInstruments() {
		return this.eventInstruments;
	}

	public Integer getSoundLength() {
		return this.soundLength;
	}

	public Calendar getStartDate() {
		return this.startDate;
	}

	public Calendar getEndDate() {
		return this.endDate;
	}

	public Optional<ExchangeData<LineData>> getDelayReverb() {
		return this.delayReverb;
	}

	public Optional<ExchangeData<LineData>> getFeedbackReverb() {
		return this.feedbackReverb;
	}

	public Optional<ExchangeData<RangeData>> getOnOffReverb() {
		return this.onOffReverb;
	}

	public Optional<ExchangeData<LineData>> getCutoff() {
		return this.cutoff;
	}

	public Optional<ExchangeData<RangeData>> getOnOffFilter() {
		return this.onOffFilter;
	}

	public Boolean getHighPass() {
		return this.highPass;
	}
}
