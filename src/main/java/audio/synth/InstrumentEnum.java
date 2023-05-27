package audio.synth;

public enum InstrumentEnum {
    SYNTH_ONE,
    SYNTH_TWO;

    public static final int size;
    public static final String[] displayVals;
    static {
        InstrumentEnum[] vals = values();
        size = vals.length;
        displayVals = new String[size];
        for (int i = 0; i < size; i++)
            displayVals[i] = vals[i].toString();
    }

    public String toString() {
        return switch (this) {
            case SYNTH_ONE -> "Synth 1";
            case SYNTH_TWO -> "Synth 2";
        };
    }
}