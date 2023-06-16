package dhbw.si.app.mapping;

import dhbw.si.audio.synth.InstrumentEnum;

/**
 * @author V. Richter
 */
public class MappedInstr {
	public final InstrumentEnum instr;
	public final InstrParam param;

	public MappedInstr(InstrumentEnum instr, InstrParam param) {
		this.instr = instr;
		this.param = param;
	}
}
