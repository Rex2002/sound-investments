package app.ui;

import java.util.function.Consumer;
import app.AppError;
import app.communication.*;
import app.mapping.*;
import audio.synth.InstrumentEnum;
import dataRepo.Sonifiable;

public class LineRangeParamNode extends LabeledDoubleCB<InstrumentEnum, InstrParam> {
	protected Sonifiable sonifiable;
	protected ExchangeParam eparam;
	protected Mapping mapping;
	protected boolean isLineParam;
	protected boolean isUpdating;
	protected Consumer<MappedInstr> onChanged;

	public LineRangeParamNode(Mapping mapping, Sonifiable sonifiable, ExchangeParam eparam, boolean isLineParam, Consumer<MappedInstr> onChanged) {
		super(eparam.toString(), InstrumentEnum.displayVals, InstrumentEnum.values(), isLineParam ? InstrParam.LineParamDisplays : InstrParam.RangeParamDisplays, isLineParam ? InstrParam.LineDataParams : InstrParam.RangeDataParams);
		this.sonifiable  = sonifiable;
		this.eparam      = eparam;
		this.mapping     = mapping;
		this.isLineParam = isLineParam;
		this.onChanged   = onChanged;
		this.isUpdating  = false;

		cb2.disable(true);
		cb1.setChangeListener((oldVal, newVal) -> {
			try {
				// If the new instrument already has the selected parameter mapped or if
				// newValue is null, then we need to remove the parameter in the UI
				// otherwise we also need to set the parameter in the mapping
				// in any case, we need to remove the old mapping with the old instrument
				if (isUpdating) return;
				isUpdating = true;
				InstrParam paramVal = cb2.getSelected();

				cb2.disable(cb1.getSelectedIdx() <= 0);
				refreshParamOpts(newVal, true);
				if (newVal != null && paramVal != null) {
					mapping.rmParam(oldVal, paramVal);
					if (!mapping.isMapped(newVal, paramVal))
						mapping.setParam(newVal, sonifiable, paramVal, eparam);
				}
				if (onChanged != null) onChanged.accept(new MappedInstr(newVal, paramVal));
				isUpdating = false;
			} catch (AppError e) {
				EventQueues.toUI.add(new Msg<>(MsgToUIType.ERROR, e.getMessage()));
			}
		});
		cb2.getChoiceBox().setOnMouseClicked(ev -> {
			if (isUpdating) return;
			isUpdating = true;
			refreshParamOpts(cb1.getSelected(), false);
			isUpdating = false;
			cb2.getChoiceBox().show();
		});
		cb2.setChangeListener((oldVal, newVal) -> {
			try {
				if (isUpdating) return;
				isUpdating = true;
				InstrumentEnum instVal = cb1.getSelected();

				 if (oldVal != null) {
					if (instVal != null)
						mapping.rmParam(instVal, oldVal);
					else
						mapping.rmParam(oldVal);
				}
				if (newVal != null) {
					if (instVal != null)
						mapping.setParam(instVal, sonifiable, newVal, eparam);
					else
						mapping.setParam(sonifiable, newVal, eparam);
				}

				if (onChanged != null) onChanged.accept(new MappedInstr(instVal, newVal));
				isUpdating = false;
			} catch (AppError e) {
				EventQueues.toUI.add(new Msg<>(MsgToUIType.ERROR, e.getMessage()));
			}
		});
	}

	protected InstrParam[] getOpts(InstrumentEnum inst, InstrParam param) {
		return isLineParam ? mapping.getEmptyLineParams(inst, param) : mapping.getEmptyRangeParams(inst, param);
	}

	protected void refreshParamOpts(InstrumentEnum instVal, boolean checkForMapping) {
		try {
			cb2.clear();
			InstrParam paramVal = cb2.getSelected();
			InstrParam[] newOpts;
            if (paramVal != null && (!checkForMapping || instVal == null || !mapping.isMapped(instVal, paramVal)))
				newOpts = getOpts(instVal, paramVal);
			else
				newOpts = getOpts(instVal, null);

			cb2.add("", null);
            int idxToSelect = -1;
            for (int i = 0; i < newOpts.length; i++) {
                cb2.add(newOpts[i].toString(), newOpts[i]);
                if (newOpts[i].equals(paramVal))
                    idxToSelect = i;
            }

            if (idxToSelect >= 0)
                cb2.select(idxToSelect + 1);
		} catch (AppError e) {
			EventQueues.toUI.add(new Msg<>(MsgToUIType.ERROR, e.getMessage()));
		}
	}

	public void setMapping(Mapping mapping) {
		this.mapping = mapping;
	}

	public void showMapping() {
		MappedInstr mi = mapping.get(new ExchangeData<>(sonifiable.getId(), eparam));
		if (mi != null) {
			cb1.select(mi.instr);
			cb2.select(mi.param);
			cb2.disable(false);
		}
	}
}
