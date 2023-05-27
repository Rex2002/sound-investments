package app.mapping;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.HashSet;
import java.util.Optional;

import audio.synth.InstrumentEnum;
import dataRepo.SonifiableID;

// TODO: Remove Optionals

public class InstrumentMapping {
	// Optional fields represent that the user can leave those empty
	// Any other fields have to be set for a valid mapping
	// They can still be null, if the mapping isn't finished
	public final InstrumentEnum instrument;
	// Pitch
	private ExchangeData<LineData> pitch = null;
	// Volume
	private Optional<ExchangeData<LineData>> relVolume = Optional.empty();
	private Optional<ExchangeData<RangeData>> absVolume = Optional.empty();
	// Echo
	private Optional<ExchangeData<LineData>> delayEcho = Optional.empty();
	private Optional<ExchangeData<LineData>> feedbackEcho = Optional.empty();
	private Optional<ExchangeData<RangeData>> onOffEcho = Optional.empty();
	// Reverb parameters
	private Optional<ExchangeData<LineData>> delayReverb = Optional.empty();
	private Optional<ExchangeData<LineData>> feedbackReverb = Optional.empty();
	private Optional<ExchangeData<RangeData>> onOffReverb = Optional.empty();
	// Filter parameters
	private Optional<ExchangeData<LineData>> cutoff = Optional.empty();
	private Optional<ExchangeData<LineData>> order = Optional.empty();
	private Optional<ExchangeData<RangeData>> onOffFilter = Optional.empty();
	private boolean highPass = false;
	// Panning
	private Optional<ExchangeData<LineData>> pan = Optional.empty();

	InstrumentMapping(InstrumentEnum instrument) {
		this.instrument = instrument;
	}

	public boolean isEmpty() {
		for (Field f : getClass().getFields()) {
			if (Optional.class.equals(f.getType())) {
				try {
					if (((Optional<?>) f.get(this)).isPresent())
						return false;
				} catch (Exception e) {
					// DO nothing
				}
			} else if (ExchangeData.class.equals(f.getType())) {
				try {
					if (f.get(this) != null)
						return false;
				} catch (Exception e) {
					// Do nothing
				}
			} else {
				// Do nothing
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public boolean hasSonifiableMapped(SonifiableID sonifiable) {
		for (Field f : getClass().getFields()) {
			if (Optional.class.equals(f.getType())) {
				try {
					Optional<ExchangeData<?>> optField = (Optional<ExchangeData<?>>) f.get(this);
					if (optField.isPresent() && optField.get().getId() == sonifiable)
						return true;
				} catch (Exception e) {
					// DO nothing
				}
			} else if (ExchangeData.class.equals(f.getType())) {
				try {
					if (((ExchangeData<?>) f.get(this)).getId() == sonifiable)
						return true;
				} catch (Exception e) {
					// Do nothing
				}
			} else {
				// Do nothing
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public Set<SonifiableID> getMappedSonifiables() {
		Set<SonifiableID> set = new HashSet<>(10);
		for (Field f : getClass().getFields()) {
			if (Optional.class.equals(f.getType())) {
				try {
					Optional<ExchangeData<?>> optField = (Optional<ExchangeData<?>>) f.get(this);
					if (optField.isPresent())
						set.add(optField.get().getId());
				} catch (Exception e) {
					// DO nothing
				}
			} else if (ExchangeData.class.equals(f.getType())) {
				try {
					set.add(((ExchangeData<?>) f.get(this)).getId());
				} catch (Exception e) {
					// Do nothing
				}
			} else {
				// Do nothing
			}
		}
		return set;
	}

	/////////
	// Getters & Setters:
	/////////

	public InstrumentEnum getInstrument() {
		return this.instrument;
	}

	public Optional<ExchangeData<LineData>> getRelVolume() {
		return this.relVolume;
	}

	public void setRelVolume(Optional<ExchangeData<LineData>> relVolume) {
		this.relVolume = relVolume;
	}

	public Optional<ExchangeData<RangeData>> getAbsVolume() {
		return this.absVolume;
	}

	public void setAbsVolume(Optional<ExchangeData<RangeData>> absVolume) {
		this.absVolume = absVolume;
	}

	public ExchangeData<LineData> getPitch() {
		return this.pitch;
	}

	public void setPitch(ExchangeData<LineData> pitch) {
		this.pitch = pitch;
	}

	public Optional<ExchangeData<LineData>> getDelayEcho() {
		return this.delayEcho;
	}

	public void setDelayEcho(Optional<ExchangeData<LineData>> delayEcho) {
		this.delayEcho = delayEcho;
	}

	public Optional<ExchangeData<LineData>> getFeedbackEcho() {
		return this.feedbackEcho;
	}

	public void setFeedbackEcho(Optional<ExchangeData<LineData>> feedbackEcho) {
		this.feedbackEcho = feedbackEcho;
	}

	public Optional<ExchangeData<RangeData>> getOnOffEcho() {
		return this.onOffEcho;
	}

	public void setOnOffEcho(Optional<ExchangeData<RangeData>> onOffEcho) {
		this.onOffEcho = onOffEcho;
	}

	public Optional<ExchangeData<LineData>> getDelayReverb() {
		return this.delayReverb;
	}

	public void setDelayReverb(Optional<ExchangeData<LineData>> delayReverb) {
		this.delayReverb = delayReverb;
	}

	public Optional<ExchangeData<LineData>> getFeedbackReverb() {
		return this.feedbackReverb;
	}

	public void setFeedbackReverb(Optional<ExchangeData<LineData>> feedbackReverb) {
		this.feedbackReverb = feedbackReverb;
	}

	public Optional<ExchangeData<RangeData>> getOnOffReverb() {
		return this.onOffReverb;
	}

	public void setOnOffReverb(Optional<ExchangeData<RangeData>> onOffReverb) {
		this.onOffReverb = onOffReverb;
	}

	public Optional<ExchangeData<LineData>> getCutoff() {
		return this.cutoff;
	}

	public void setCutoff(Optional<ExchangeData<LineData>> cutoff) {
		this.cutoff = cutoff;
	}

	public Optional<ExchangeData<LineData>> getOrder() {
		return this.order;
	}

	public void setOrder(Optional<ExchangeData<LineData>> order) {
		this.order = order;
	}

	public Optional<ExchangeData<RangeData>> getOnOffFilter() {
		return this.onOffFilter;
	}

	public void setOnOffFilter(Optional<ExchangeData<RangeData>> onOffFilter) {
		this.onOffFilter = onOffFilter;
	}

	public boolean getHighPass() {
		return this.highPass;
	}

	public void setHighPass(boolean highPass) {
		this.highPass = highPass;
	}

	public Optional<ExchangeData<LineData>> getPan() {
		return this.pan;
	}

	public void setPan(Optional<ExchangeData<LineData>> pan) {
		this.pan = pan;
	}

	@Override
	public String toString() {
		return "{" +
				" instrument='" + this.instrument + "'" +
				", relVolume='" + this.relVolume + "'" +
				", absVolume='" + this.absVolume + "'" +
				", pitch='" + this.pitch + "'" +
				", delayEcho='" + this.delayEcho + "'" +
				", feedbackEcho='" + this.feedbackEcho + "'" +
				", onOffEcho='" + this.onOffEcho + "'" +
				", delayReverb='" + this.delayReverb + "'" +
				", feedbackReverb='" + this.feedbackReverb + "'" +
				", onOffReverb='" + this.onOffReverb + "'" +
				", cutoff='" + this.cutoff + "'" +
				", order='" + this.order + "'" +
				", onOffFilter='" + this.onOffFilter + "'" +
				", highPass='" + this.highPass + "'" +
				", pan='" + this.pan + "'" +
				"}";
	}
}
