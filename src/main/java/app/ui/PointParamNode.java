package app.ui;

import app.AppError;
import app.communication.*;
import app.mapping.*;
import audio.synth.EvInstrEnum;
import dataRepo.Sonifiable;

public class PointParamNode extends LabeledCB<EvInstrEnum> {
	protected Sonifiable sonifiable;
	protected PointData eparam;
	protected Mapping mapping;

	public PointParamNode(Mapping mapping, Sonifiable sonifiable, PointData eparam) {
		super(eparam.toString(), EvInstrEnum.displayVals, EvInstrEnum.values());
		this.sonifiable = sonifiable;
		this.eparam     = eparam;
		this.mapping    = mapping;

		cb.setChangeListener((oldVal, newVal) -> {
			try {
				if (oldVal != null)
					mapping.rmEvInstr(sonifiable.getId(), eparam);
				if (newVal != null)
					mapping.addEvInstr(newVal, sonifiable, (PointData) eparam);
			} catch (AppError e) {
				EventQueues.toUI.add(new Msg<>(MsgToUIType.ERROR, e.getMessage()));
			}
		});
	}

	public void setMapping(Mapping mapping) {
		this.mapping = mapping;
	}

	public void showMapping() {
		for (EvInstrMapping evInstMap : mapping.getEventInstruments()) {
			if (evInstMap.getData().getData().equals(eparam)
					&& evInstMap.getData().getId().equals(sonifiable.getId())) {
				cb.select(evInstMap.getInstrument());
				break;
			}
		}
	}
}
