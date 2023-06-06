package app.mapping;

import audio.synth.InstrumentEnum;

public class MappedInstr {
	public InstrumentEnum instr;
	public InstrParam param;

	public MappedInstr(InstrumentEnum instr, InstrParam param) {
		this.instr = instr;
		this.param = param;
	}
}
