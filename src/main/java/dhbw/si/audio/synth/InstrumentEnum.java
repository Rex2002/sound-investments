package dhbw.si.audio.synth;

/**
 * @author B. Frahm
 * @reviewer M. Richert
 */
public enum InstrumentEnum {
    RETRO_SYNTH,
    STRINGS_SYNTH,
    BANJO_SYNTH,
    BRASS_SYNTH;

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
            case RETRO_SYNTH -> "Retro Synth";
            case STRINGS_SYNTH -> "Strings";
            case BANJO_SYNTH -> "Banjo";
            case BRASS_SYNTH -> "Brass";
        };
    }
}