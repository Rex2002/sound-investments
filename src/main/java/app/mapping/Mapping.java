package app.mapping;

import java.util.*;
import app.Util;

import app.AppError;
import audio.synth.EvInstrEnum;
import audio.synth.InstrumentEnum;
import dataRepo.Sonifiable;
import dataRepo.SonifiableID;

public class Mapping {
	public static int MAX_EV_INSTR_SIZE = 10;
	public static int MIN_SOUND_LENGTH = 30;
	public static int MAX_SOUND_LENGTH = 5 * 60;
	public static int MAX_SONIFIABLES_AMOUNT = 10;

	private Set<Sonifiable> sonifiables = new HashSet<>(16);
	private final InstrumentMapping[] mappedInstruments = new InstrumentMapping[InstrumentEnum.size];
	private final EvInstrMapping[] eventInstruments = new EvInstrMapping[MAX_EV_INSTR_SIZE];
	private int evInstrAmount = 0;
	private Integer soundLength = null; // stored in seconds
	// Timeperiod
	private Calendar startDate = null;
	private Calendar endDate = null;
	// Reverb parameters
	private ExchangeData<LineData> delayReverb = null;
	private ExchangeData<LineData> feedbackReverb = null;
	private ExchangeData<RangeData> onOffReverb = null;
	// Filter parameters
	private ExchangeData<LineData> cutoff = null;
	private ExchangeData<RangeData> onOffFilter = null;
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
		// System.out.println(verify());
		return verify() == null;
	}

	public String verify() {
		// TODO: Are there any other ways in which the mapping can be invalid?
		// evInstrAmount > MAX_EV_INSTR_SIZE doesn't have to be checked, because the
		// setters don't allow this to happen anyways
		// TODO: Update Warning messages for invalid Mapping
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

		boolean isAnyInstrMapped = false;
		for (InstrumentMapping instrMap : mappedInstruments) {
			if (instrMap.isEmpty())
				continue;
			isAnyInstrMapped = true;
			if (instrMap.getPitch() == null)
				return "Der Pitch vom Instrument '" + instrMap.getInstrument().toString()
						+ "' wurde nicht auf einen Börsenwert gemappt.";
		}
		if (!isAnyInstrMapped)
			return "Es muss min. 1 Instrument gemappt werden.";
		return null;
	}
	public InstrParam[] getEmptyInstrumentParams(InstrumentEnum instr, InstrParam oldVal) {
		InstrumentMapping instrMap = Util.find(mappedInstruments, x -> x != null && x.getInstrument() == instr);
		return instrMap.getEmptyParams(oldVal);
	}

	public Sonifiable[] getMappedSonifiables() {
		Set<SonifiableID> ids = getMappedSonifiableIDs();
		Sonifiable[] out = new Sonifiable[ids.size()];
		int i = 0;
		for (SonifiableID id : ids) {
			for (Sonifiable s : sonifiables) {
				if (s.getId() == id) {
					out[i] = s;
					break;
				}
			}
			i++;
		}
		return out;
	}

	public Set<SonifiableID> getMappedSonifiableIDs() {
		Set<SonifiableID> out = new HashSet<>();
		for (InstrumentMapping instrMap : mappedInstruments) {
			if (instrMap.isEmpty()) continue;
			out.addAll(instrMap.getMappedSonifiables());
		}
		return out;
	}

	public void addSonifiable(Sonifiable sonifiable) {
		sonifiables.add(sonifiable);
	}

	public void rmSonifiable(SonifiableID id) {
		for (Sonifiable s : sonifiables) {
			if (s.getId() == id) {
				rmSonifiable(s);
				return;
			}
		}
	}

	public void rmSonifiable(Sonifiable sonifiable) {
		// TODO: Add logic to remove sonifiable from mapping
		sonifiables.remove(sonifiable);
	}

	public boolean hasSonifiable(SonifiableID id) {
		for (Sonifiable s : sonifiables) {
			if (s.getId() == id) return true;
		}
		return false;
	}

	public boolean hasSonifiable(Sonifiable sonifiable) {
		return sonifiables.contains(sonifiable);
	}

	public void addEvInstr(EvInstrEnum instr, Sonifiable sonifiable, PointData eparam) throws AppError {
		if (evInstrAmount == MAX_EV_INSTR_SIZE)
			throw new AppError("Zu viele Event-Instrumente. Ein Mapping darf höchstens "
					+ Integer.toString(MAX_EV_INSTR_SIZE) + " Event-Instrumente haben");
		eventInstruments[evInstrAmount] = new EvInstrMapping(instr, new ExchangeData<>(sonifiable.getId(), eparam));
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

	// Important to keep in mind:
	// setParam does not check if the change is even allowed
	public void setParam(InstrumentEnum instr, Sonifiable sonifiable, InstrParam iparam, ExchangeParam eparam)
			throws AppError {
		InstrumentMapping instrMap = Util.find(mappedInstruments, x -> x != null && x.getInstrument() == instr);
		SonifiableID id = sonifiable.getId();
		switch (iparam) {
			case PITCH -> instrMap.setPitch(setParamHelper(id, eparam, iparam));
			case RELVOLUME -> instrMap.setRelVolume(setParamHelper(id, eparam, iparam));
			case ABSVOLUME -> instrMap.setAbsVolume(setParamHelper(id, eparam, iparam));
			case DELAY_ECHO -> instrMap.setDelayEcho(setParamHelper(id, eparam, iparam));
			case FEEDBACK_ECHO -> instrMap.setFeedbackEcho(setParamHelper(id, eparam, iparam));
			case ON_OFF_REVERB -> instrMap.setOnOffReverb(setParamHelper(id, eparam, iparam));
			case CUTOFF -> instrMap.setCutoff(setParamHelper(id, eparam, iparam));
			case ORDER -> instrMap.setOrder(setParamHelper(id, eparam, iparam));
			case ON_OFF_FILTER -> instrMap.setOnOffFilter(setParamHelper(id, eparam, iparam));
			case PAN -> instrMap.setPan(setParamHelper(id, eparam, iparam));
			default ->
				throw new AppError(eparam.toString() + " kann nicht auf " + iparam.toString() + "gemappt werden.");
		}
		sonifiables.add(sonifiable);
	}

	public boolean isMapped(InstrumentEnum instr, InstrParam iparam) throws AppError {
		InstrumentMapping instrMap = Util.find(mappedInstruments, x -> x.getInstrument() == instr);
		return switch (iparam) {
			case PITCH -> instrMap.getPitch() != null;
			case RELVOLUME -> instrMap.getRelVolume() != null;
			case ABSVOLUME -> instrMap.getAbsVolume() != null;
			case DELAY_ECHO -> instrMap.getDelayEcho() != null;
			case FEEDBACK_ECHO -> instrMap.getFeedbackEcho() != null;
			case ON_OFF_REVERB -> instrMap.getOnOffReverb() != null;
			case CUTOFF -> instrMap.getCutoff() != null;
			case ORDER -> instrMap.getOrder() != null;
			case ON_OFF_FILTER -> instrMap.getOnOffFilter() != null;
			case PAN -> instrMap.getPan() != null;
			default ->
				throw new AppError(iparam.toString() + " kann nicht gelöscht werden.");
		};
	}

	public void rmParam(InstrumentEnum instr, SonifiableID sonifiable, InstrParam iparam) throws AppError {
		InstrumentMapping instrMap = Util.find(mappedInstruments, x -> x.getInstrument() == instr);
		switch (iparam) {
			case PITCH -> instrMap.setPitch(null);
			case RELVOLUME -> instrMap.setRelVolume(null);
			case ABSVOLUME -> instrMap.setAbsVolume(null);
			case DELAY_ECHO -> instrMap.setDelayEcho(null);
			case FEEDBACK_ECHO -> instrMap.setFeedbackEcho(null);
			case ON_OFF_REVERB -> instrMap.setOnOffReverb(null);
			case CUTOFF -> instrMap.setCutoff(null);
			case ORDER -> instrMap.setOrder(null);
			case ON_OFF_FILTER -> instrMap.setOnOffFilter(null);
			case PAN -> instrMap.setPan(null);
			default ->
				throw new AppError(iparam.toString() + " kann nicht gelöscht werden.");
		}
	}

	public void setParam(Sonifiable sonifiable, InstrParam iparam, ExchangeParam eparam) throws AppError {
		SonifiableID id = sonifiable.getId();
		switch (iparam) {
			case DELAY_REVERB -> this.delayReverb = setParamHelper(id, eparam, iparam);
			case FEEDBACK_REVERB -> this.feedbackReverb = setParamHelper(id, eparam, iparam);
			case ON_OFF_REVERB -> this.onOffReverb = setParamHelper(id, eparam, iparam);
			case CUTOFF -> this.cutoff = setParamHelper(id, eparam, iparam);
			case ON_OFF_FILTER -> this.onOffFilter = setParamHelper(id, eparam, iparam);
			default ->
				throw new AppError(eparam.toString() + " kann nicht auf " + iparam.toString() + "gemappt werden.");
		}
		sonifiables.add(sonifiable);
	}

	public boolean isMapped(InstrParam iparam) throws AppError {
		return switch (iparam) {
			case DELAY_REVERB -> this.delayReverb != null;
			case FEEDBACK_REVERB -> this.feedbackReverb != null;
			case ON_OFF_REVERB -> this.onOffReverb != null;
			case CUTOFF -> this.cutoff != null;
			case ON_OFF_FILTER -> this.onOffFilter != null;
			default ->
				throw new AppError(iparam.toString() + " kann nicht gemappt sein.");
		};
	}

	public void rmParam(SonifiableID sonifiable, InstrParam iparam) throws AppError {
		switch (iparam) {
			case DELAY_REVERB -> this.delayReverb = null;
			case FEEDBACK_REVERB -> this.feedbackReverb = null;
			case ON_OFF_REVERB -> this.onOffReverb = null;
			case CUTOFF -> this.cutoff = null;
			case ON_OFF_FILTER -> this.onOffFilter = null;
			default ->
				throw new AppError(iparam.toString() + " kann nicht gelöscht werden.");
		}
	}

	// Returns list with first element being the earliest & second element being the last allowed date
	public Calendar[] getDateRange() {
		Calendar[] out = {null, null};
		for (Sonifiable s : sonifiables) {
			if (out[0] == null || out[0].after(s.getEarliest()))
				out[0] = s.getEarliest();
			if (out[1] == null || out[1].before(s.getLatest()))
				out[1] = s.getLatest();
		}
		return out;
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

	public Set<Sonifiable> getSonifiables() {
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

	public ExchangeData<LineData> getDelayReverb() {
		return this.delayReverb;
	}

	public ExchangeData<LineData> getFeedbackReverb() {
		return this.feedbackReverb;
	}

	public ExchangeData<RangeData> getOnOffReverb() {
		return this.onOffReverb;
	}

	public ExchangeData<LineData> getCutoff() {
		return this.cutoff;
	}

	public ExchangeData<RangeData> getOnOffFilter() {
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
