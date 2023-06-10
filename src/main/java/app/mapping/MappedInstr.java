package app.mapping;

import audio.synth.InstrumentEnum;

public class MappedInstr {
	public final InstrumentEnum instr;
	public final InstrParam param;

	public MappedInstr(InstrumentEnum instr, InstrParam param) {
		this.instr = instr;
		this.param = param;
	}
}
