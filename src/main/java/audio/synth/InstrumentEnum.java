package audio.synth;

public enum InstrumentEnum {
    SYNTH_ONE,
    SYNTH_TWO;

    public static final int size;
    static {
        size = values().length;
    }

    public String toString() {
        return switch (this) {
            case SYNTH_ONE -> "Synth 1";
            case SYNTH_TWO -> "Synth 2";
        };
    }
}