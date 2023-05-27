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

	private Set<SonifiableID> sonifiables = new HashSet<>(16);
	private final InstrumentMapping[] mappedInstruments = new InstrumentMapping[InstrumentEnum.size];
	private final EvInstrMapping[] eventInstruments = new EvInstrMapping[MAX_EV_INSTR_SIZE];
	private int evInstrAmount = 0;
	private Integer soundLength = null; // stored in seconds
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
		// Initialize mappedInstruments
		InstrumentEnum[] instruments = InstrumentEnum.values();
		for (int i = 0; i < mappedInstruments.length; i++) {
			mappedInstruments[i] = new InstrumentMapping(instruments[i]);
		}
	}

	// Indicates whether the current mapping can already be used for sonification
	public boolean isValid() {
		System.out.println(verify());
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
		if (sonifiables.isEmpty())
			return "Kein Börsenkurs wurde auf ein Instrument gemappt.";
		if (sonifiables.size() > MAX_SONIFIABLES_AMOUNT)
			return "Zu viele Börsenkurse auf einmal im Mapping. Es dürfen maximal "
					+ Integer.toString(MAX_SONIFIABLES_AMOUNT) + " Börsenkurse auf einmal gemappt werden.";

		for (InstrumentMapping instrMap : mappedInstruments) {
			if (instrMap.isEmpty())
				continue;
			if (instrMap.getPitch() == null)
				return "Der Pitch vom Instrument '" + instrMap.getInstrument().toString()
						+ "' wurde nicht auf einen Börsenwert gemappt.";
		}
		return null;
	}

	////////
	// Getters & Setters
	////////

	public void addSonifiable(SonifiableID sonifiable) {
		sonifiables.add(sonifiable);
	}

	public void rmSonifiable(SonifiableID sonifiable) {
		// TODO: Add logic to remove sonifiable from mapping
		sonifiables.remove(sonifiable);
	}

	public boolean hasSonifiable(SonifiableID sonifiable) {
		return sonifiables.contains(sonifiable);
	}

	// Do we even need this?
	// private boolean isSonifiableStillMapped(SonifiableID sonifiable) {
	// for (int i = 0; i < evInstrAmount; i++) {
	// if (eventInstruments[i].getData().getId() == sonifiable)
	// return true;
	// }
	// for (int i = 0; i < mappedInstruments.length; i++) {
	// if (mappedInstruments[i].hasSonifiableMapped(sonifiable))
	// return true;
	// }
	// return false;
	// }

	public void addEvInstr(EvInstrEnum instr, SonifiableID sonifiable, PointData eparam) throws AppError {
		if (evInstrAmount == MAX_EV_INSTR_SIZE)
			throw new AppError("Zu viele Event-Instrumente. Ein Mapping darf höchstens "
					+ Integer.toString(MAX_EV_INSTR_SIZE) + " Event-Instrumente haben");
		eventInstruments[evInstrAmount] = new EvInstrMapping(instr, new ExchangeData<>(sonifiable, eparam));
		evInstrAmount++;
		sonifiables.add(sonifiable);
	}

	public void rmEvInstr(SonifiableID sonifiable, PointData eparam) throws AppError {
		assert evInstrAmount > 0 : "Can't remove Event Instruments, when none have been mapped yet";
		int idx = 0;
		ExchangeData<PointData> e = eventInstruments[idx].getData();
		while (e.getId() != sonifiable || e.getData() != eparam) {
			idx++;
			if (idx == evInstrAmount)
				throw new AppError("Can't remove non-existent Event-Instrument.");
			e = eventInstruments[idx].getData();
		}
		eventInstruments[idx] = eventInstruments[evInstrAmount - 1];
		eventInstruments[evInstrAmount - 1] = null;
		evInstrAmount--;
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

	// TODO: Make sure the switch cases don't forget any important parameters

	// Important to keep in mind:
	// setParam does not make sure to remove old parameter
	public void setParam(InstrumentEnum instr, SonifiableID sonifiable, InstrParam iparam, ExchangeParam eparam)
			throws AppError {
		InstrumentMapping instrMap = Util.find(mappedInstruments, x -> x != null && x.getInstrument() == instr);
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
		sonifiables.add(sonifiable);
	}

	public void rmParam(SonifiableID sonifiable, InstrumentEnum instr, InstrParam iparam) throws AppError {
		InstrumentMapping instrMap = Util.find(mappedInstruments, x -> x.getInstrument() == instr);
		switch (iparam) {
			case PITCH -> instrMap.setPitch(null);
			case RELVOLUME -> instrMap.setRelVolume(Optional.empty());
			case ABSVOLUME -> instrMap.setAbsVolume(Optional.empty());
			case DELAY_ECHO -> instrMap.setDelayEcho(Optional.empty());
			case FEEDBACK_ECHO -> instrMap.setFeedbackEcho(Optional.empty());
			case ON_OFF_REVERB -> instrMap.setOnOffReverb(Optional.empty());
			case CUTOFF -> instrMap.setCutoff(Optional.empty());
			case ORDER -> instrMap.setOrder(Optional.empty());
			case ON_OFF_FILTER -> instrMap.setOnOffFilter(Optional.empty());
			case PAN -> instrMap.setPan(Optional.empty());
			default ->
				throw new AppError(iparam.toString() + " kann nicht gelöscht werden.");
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
		sonifiables.add(sonifiable);
	}

	public void rmParam(SonifiableID sonifiable, InstrParam iparam) throws AppError {
		switch (iparam) {
			case DELAY_REVERB -> this.delayReverb = Optional.empty();
			case FEEDBACK_REVERB -> this.feedbackReverb = Optional.empty();
			case ON_OFF_REVERB -> this.onOffReverb = Optional.empty();
			case CUTOFF -> this.cutoff = Optional.empty();
			case ON_OFF_FILTER -> this.onOffFilter = Optional.empty();
			default ->
				throw new AppError(iparam.toString() + " kann nicht gelöscht werden.");
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

	public Set<SonifiableID> getSonifiables() {
		return this.sonifiables;
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

	@Override
	public String toString() {
		return "{" +
				" sonifiables='" + this.sonifiables + "'" +
				", mappedInstruments='" + Util.toStringArr(this.mappedInstruments) + "'" +
				", eventInstruments='" + Util.toStringArr(this.eventInstruments) + "'" +
				", evInstrAmount='" + this.evInstrAmount + "'" +
				", soundLength='" + this.soundLength + "'" +
				", startDate='" + this.startDate + "'" +
				", endDate='" + this.endDate + "'" +
				", delayReverb='" + this.delayReverb + "'" +
				", feedbackReverb='" + this.feedbackReverb + "'" +
				", onOffReverb='" + this.onOffReverb + "'" +
				", cutoff='" + this.cutoff + "'" +
				", onOffFilter='" + this.onOffFilter + "'" +
				", highPass='" + this.highPass + "'" +
				"}";
	}
}
