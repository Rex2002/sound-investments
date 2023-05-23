package audio.synth;

public enum InstrumentEnum {
    SYNTH_ONE,
    SYNTH_TWO;

    public static final int size;
    static {
        size = values().length;
    }
}