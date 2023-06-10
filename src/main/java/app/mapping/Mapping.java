package app.mapping;

import app.AppError;
import audio.synth.EvInstrEnum;
import audio.synth.InstrumentEnum;
import dataRepo.Sonifiable;
import dataRepo.SonifiableID;
import util.ArrayFunctions;

import java.util.*;
import java.util.function.Consumer;

public class Mapping {
	public static int MAX_EV_INSTR_SIZE = 10;
	public static int MIN_SOUND_LENGTH = 30;
	public static int MAX_SOUND_LENGTH = 5 * 60;
	public static int MAX_SONIFIABLES_AMOUNT = 10;
	public static int START_END_DATE_MIN_DISTANCE = 4;

	private Set<Sonifiable> sonifiables = new HashSet<>(16);
	private final InstrumentMapping[] mappedInstruments = new InstrumentMapping[InstrumentEnum.size];
	private final EvInstrMapping[] eventInstruments = new EvInstrMapping[MAX_EV_INSTR_SIZE];
	private int evInstrAmount = 0;
	private Consumer<InstrumentEnum> onInstrAdded;
	private Consumer<EvInstrEnum> onEvInstrAdded;
	private Consumer<InstrumentEnum> onInstrRemoved;
	private Consumer<EvInstrEnum> onEvInstrRemoved;
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
		startDate.roll(Calendar.DATE, START_END_DATE_MIN_DISTANCE);
		if (startDate.after(endDate))
			return "Start-Datum muss mindestens " + Integer.toString(START_END_DATE_MIN_DISTANCE) + " Tage vor dem Enddatum liegen";
		if (soundLength == null || soundLength < MIN_SOUND_LENGTH || soundLength > MAX_SOUND_LENGTH)
			return "Ungültige Audio-Länge gewählt. Die Audio-Länge muss zwischen 30 Sekunden und 5 Minuten liegen.";
		if (sonifiables.isEmpty())
			return "Kein Börsenkurs wurde auf ein Instrument gemappt.";
		if (sonifiables.size() > MAX_SONIFIABLES_AMOUNT)
			return "Zu viele Börsenkurse auf einmal im Mapping. Es dürfen maximal "
					+ MAX_SONIFIABLES_AMOUNT + " Börsenkurse auf einmal gemappt werden.";

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

	public InstrParam[] getEmptyLineParams(InstrumentEnum instr, InstrParam oldVal) {
		if (instr == null) return getEmptyLineParams(oldVal);
		InstrumentMapping instrMap = ArrayFunctions.find(mappedInstruments, x -> x != null && x.getInstrument() == instr);
		return instrMap.getEmptyLineParams(oldVal);
	}

	public InstrParam[] getEmptyRangeParams(InstrumentEnum instr, InstrParam oldVal) {
		if (instr == null) return getEmptyRangeParams(oldVal);
		InstrumentMapping instrMap = ArrayFunctions.find(mappedInstruments, x -> x != null && x.getInstrument() == instr);
		return instrMap.getEmptyRangeParams(oldVal);
	}

	public InstrParam[] getEmptyLineParams(InstrParam oldVal) {
		List<InstrParam> params = new ArrayList<>();
		if    (delayReverb == null || oldVal == InstrParam.DELAY_REVERB)     params.add(InstrParam.DELAY_REVERB);
		if (feedbackReverb == null || oldVal == InstrParam.FEEDBACK_REVERB)  params.add(InstrParam.FEEDBACK_REVERB);
		if         (cutoff == null || oldVal == InstrParam.CUTOFF)           params.add(InstrParam.CUTOFF);
		InstrParam[] out = new InstrParam[params.size()];
		return params.toArray(out);
	}

	public InstrParam[] getEmptyRangeParams(InstrParam oldVal) {
		List<InstrParam> params = new ArrayList<>();
		if    (onOffReverb == null || oldVal == InstrParam.ON_OFF_REVERB)    params.add(InstrParam.ON_OFF_REVERB);
		if    (onOffFilter == null || oldVal == InstrParam.ON_OFF_FILTER)    params.add(InstrParam.ON_OFF_FILTER);
		InstrParam[] out = new InstrParam[params.size()];
		return params.toArray(out);
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

		for(EvInstrMapping evInstrMap : eventInstruments){
			if(evInstrMap == null || evInstrMap.isEmpty()) continue;
			out.add(evInstrMap.getData().getId());
		}
		if(getFeedbackReverb() != null) out.add(getFeedbackReverb().getId());
		if(getDelayReverb() != null) out.add(getDelayReverb().getId());
		if(getCutoff() != null) out.add(getCutoff().getId());
		if(getOnOffReverb() != null) out.add(getOnOffReverb().getId());
		if(getOnOffFilter() != null) out.add(getOnOffFilter().getId());

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
		sonifiables.remove(sonifiable);
		for (InstrumentMapping instrMap : mappedInstruments) {
			for (InstrParam iparam : InstrParam.values()) {
				ExchangeData<? extends ExchangeParam> ed = instrMap.get(iparam);
				if (ed != null && ed.getId() == sonifiable.getId())
					instrMap.rm(iparam);
			}
			if (onInstrRemoved != null && instrMap.isEmpty())
				onInstrRemoved.accept(instrMap.getInstrument());
		}
		for (int i = 0; i < evInstrAmount; i++) {
			ExchangeData<PointData> ed = eventInstruments[i].getData();
			if (ed != null && ed.getId() == sonifiable.getId()){
				rmEvInstr(i);
				i--;
			}
		}
		if (delayReverb    != null && delayReverb.getId()    == sonifiable.getId()) delayReverb    = null;
		if (feedbackReverb != null && feedbackReverb.getId() == sonifiable.getId()) feedbackReverb = null;
		if (cutoff         != null && cutoff.getId()         == sonifiable.getId()) cutoff         = null;
		if (onOffReverb    != null && onOffReverb.getId()    == sonifiable.getId()) onOffReverb    = null;
		if (onOffFilter    != null && onOffFilter.getId()    == sonifiable.getId()) onOffFilter    = null;
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
		if (instr == null) return;
		if (evInstrAmount == MAX_EV_INSTR_SIZE)
			throw new AppError("Zu viele Event-Instrumente. Ein Mapping darf höchstens "
					+ MAX_EV_INSTR_SIZE + " Event-Instrumente haben");
		eventInstruments[evInstrAmount] = new EvInstrMapping(instr, new ExchangeData<>(sonifiable.getId(), eparam));
		evInstrAmount++;
		sonifiables.add(sonifiable);
		if (onEvInstrAdded != null) onEvInstrAdded.accept(instr);
	}

	public void rmEvInstr(SonifiableID sonifiable, PointData eparam) throws AppError {
		assert evInstrAmount > 0 : "Can't remove Event Instruments, when none have been mapped yet";
		int idx = 0;
		ExchangeData<PointData> e = eventInstruments[idx].getData();
		while (e.getId() != sonifiable || e.getData() != eparam) {
			idx++;
			if (idx == evInstrAmount)
				throw new AppError("Fehler beim Entfernen des EventInstruments. \n Das EventInstrument existiert nicht");
			e = eventInstruments[idx].getData();
		}
		rmEvInstr(idx);
	}

	private void rmEvInstr(int idx) {
		if (onEvInstrRemoved != null) onEvInstrRemoved.accept(eventInstruments[idx].getInstrument());
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
			throw new AppError(eparam.toString() + " kann nicht auf " + iparam.toString() + " gemappt werden.");
		}
	}

	// Important to keep in mind:
	// setParam does not check if the change is even allowed
	public void setParam(InstrumentEnum instr, Sonifiable sonifiable, InstrParam iparam, ExchangeParam eparam)
			throws AppError {
		InstrumentMapping instrMap = ArrayFunctions.find(mappedInstruments, x -> x != null && x.getInstrument() == instr);
		if (onInstrAdded != null && instrMap.isEmpty()) onInstrAdded.accept(instrMap.getInstrument());
		SonifiableID id = sonifiable.getId();
		switch (iparam) {
			case PITCH           -> instrMap.setPitch(setParamHelper(id, eparam, iparam));
			case RELVOLUME       -> instrMap.setRelVolume(setParamHelper(id, eparam, iparam));
			case ABSVOLUME       -> instrMap.setAbsVolume(setParamHelper(id, eparam, iparam));
			case DELAY_ECHO      -> instrMap.setDelayEcho(setParamHelper(id, eparam, iparam));
			case FEEDBACK_ECHO   -> instrMap.setFeedbackEcho(setParamHelper(id, eparam, iparam));
			case ON_OFF_ECHO     -> instrMap.setOnOffEcho(setParamHelper(id, eparam, iparam));
			case DELAY_REVERB    -> instrMap.setDelayReverb(setParamHelper(id, eparam, iparam));
			case FEEDBACK_REVERB -> instrMap.setFeedbackReverb(setParamHelper(id, eparam, iparam));
			case ON_OFF_REVERB   -> instrMap.setOnOffReverb(setParamHelper(id, eparam, iparam));
			case CUTOFF          -> instrMap.setCutoff(setParamHelper(id, eparam, iparam));
			case ON_OFF_FILTER   -> instrMap.setOnOffFilter(setParamHelper(id, eparam, iparam));
			case PAN             -> instrMap.setPan(setParamHelper(id, eparam, iparam));
			case HIGHPASS        -> throw new AppError(eparam.toString() + " kann nicht auf " + iparam + " gemappt werden.");
		}
		sonifiables.add(sonifiable);
	}

	public boolean isMapped(InstrumentEnum instr, InstrParam iparam) throws AppError {
		InstrumentMapping instrMap = ArrayFunctions.find(mappedInstruments, x -> x.getInstrument() == instr);
		return switch (iparam) {
			case PITCH           -> instrMap.getPitch() != null;
			case RELVOLUME       -> instrMap.getRelVolume() != null;
			case ABSVOLUME       -> instrMap.getAbsVolume() != null;
			case DELAY_ECHO      -> instrMap.getDelayEcho() != null;
			case FEEDBACK_ECHO   -> instrMap.getFeedbackEcho() != null;
			case ON_OFF_ECHO     -> instrMap.getOnOffEcho() != null;
			case DELAY_REVERB    -> instrMap.getDelayReverb() != null;
			case FEEDBACK_REVERB -> instrMap.getFeedbackReverb() != null;
			case ON_OFF_REVERB   -> instrMap.getOnOffReverb() != null;
			case CUTOFF          -> instrMap.getCutoff() != null;
			case ON_OFF_FILTER   -> instrMap.getOnOffFilter() != null;
			case PAN             -> instrMap.getPan() != null;
			case HIGHPASS        -> throw new AppError(iparam + " kann nicht gemappt sein");
		};
	}

	public void rmParam(InstrumentEnum instr, InstrParam iparam) throws AppError {
		if (instr == null) {
			rmParam(iparam);
			return;
		}
		InstrumentMapping instrMap = ArrayFunctions.find(mappedInstruments, x -> x.getInstrument() == instr);
		switch (iparam) {
			case PITCH           -> instrMap.setPitch(null);
			case RELVOLUME       -> instrMap.setRelVolume(null);
			case ABSVOLUME       -> instrMap.setAbsVolume(null);
			case DELAY_ECHO      -> instrMap.setDelayEcho(null);
			case FEEDBACK_ECHO   -> instrMap.setFeedbackEcho(null);
			case ON_OFF_ECHO     -> instrMap.setOnOffEcho(null);
			case DELAY_REVERB    -> instrMap.setDelayReverb(null);
			case FEEDBACK_REVERB -> instrMap.setFeedbackReverb(null);
			case ON_OFF_REVERB   -> instrMap.setOnOffReverb(null);
			case CUTOFF          -> instrMap.setCutoff(null);
			case ON_OFF_FILTER   -> instrMap.setOnOffFilter(null);
			case PAN             -> instrMap.setPan(null);
			case HIGHPASS        -> throw new AppError(iparam + " kann nicht gelöscht werden.");
		}
		if (onInstrRemoved != null && instrMap.isEmpty()) onInstrRemoved.accept(instrMap.getInstrument());
	}

	public void setParam(Sonifiable sonifiable, InstrParam iparam, ExchangeParam eparam) throws AppError {
		SonifiableID id = sonifiable.getId();
		switch (iparam) {
			case DELAY_REVERB    -> this.delayReverb = setParamHelper(id, eparam, iparam);
			case FEEDBACK_REVERB -> this.feedbackReverb = setParamHelper(id, eparam, iparam);
			case ON_OFF_REVERB   -> this.onOffReverb = setParamHelper(id, eparam, iparam);
			case CUTOFF          -> this.cutoff = setParamHelper(id, eparam, iparam);
			case ON_OFF_FILTER   -> this.onOffFilter = setParamHelper(id, eparam, iparam);
			default              -> throw new AppError(eparam.toString() + " kann nicht auf " + iparam + "gemappt werden.");
		}
		sonifiables.add(sonifiable);
	}

	public boolean isMapped(InstrParam iparam) throws AppError {
		return switch (iparam) {
			case DELAY_REVERB    -> this.delayReverb != null;
			case FEEDBACK_REVERB -> this.feedbackReverb != null;
			case ON_OFF_REVERB   -> this.onOffReverb != null;
			case CUTOFF          -> this.cutoff != null;
			case ON_OFF_FILTER   -> this.onOffFilter != null;
			default              -> throw new AppError(iparam + " kann nicht gemappt sein.");
		};
	}

	public void rmParam(InstrParam iparam) throws AppError {
		switch (iparam) {
			case DELAY_REVERB    -> this.delayReverb = null;
			case FEEDBACK_REVERB -> this.feedbackReverb = null;
			case ON_OFF_REVERB   -> this.onOffReverb = null;
			case CUTOFF          -> this.cutoff = null;
			case ON_OFF_FILTER   -> this.onOffFilter = null;
			default              -> throw new AppError(iparam + " kann nicht gelöscht werden.");
		}
	}

	public MappedInstr get(ExchangeData<? extends ExchangeParam> ed) {
		for (InstrumentMapping instrMap : mappedInstruments) {
			InstrParam param = instrMap.get(ed);
			if (param != null)
				return new MappedInstr(instrMap.getInstrument(), param);
		}
		return null;
	}

	public String[] getMappedInstrNames() {
		String[] out = new String[mappedInstruments.length + evInstrAmount];
		int len = 0;
		for (int i = 0; i < mappedInstruments.length; i++) {
			if (!mappedInstruments[i].isEmpty())
				out[len++] = mappedInstruments[i].getInstrument().toString();
		}
		for (int i = 0; i < evInstrAmount; i++) {
			out[len++] = eventInstruments[i].getInstrument().toString();
		}
		return out;
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

	public void setOnInstrAdded(Consumer<InstrumentEnum> callback) {
		this.onInstrAdded = callback;
	}

	public void setOnEvInstrAdded(Consumer<EvInstrEnum> callback) {
		this.onEvInstrAdded = callback;
	}

	public void setOnInstrRemoved(Consumer<InstrumentEnum> callback) {
		this.onInstrRemoved = callback;
	}

	public void setOnEvInstrRemoved(Consumer<EvInstrEnum> callback) {
		this.onEvInstrRemoved = callback;
	}


	public void setHighPass(InstrumentEnum instr, boolean highpass) {
		InstrumentMapping instrMap = ArrayFunctions.find(mappedInstruments, x -> x.getInstrument() == instr);
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
		EvInstrMapping[] out = new EvInstrMapping[evInstrAmount];
		System.arraycopy(eventInstruments, 0, out, 0, evInstrAmount);
		return out;
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
				", mappedInstruments='" + ArrayFunctions.toStringArr(this.mappedInstruments) + "'" +
				", eventInstruments='" + ArrayFunctions.toStringArr(this.eventInstruments) + "'" +
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