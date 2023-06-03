package audio.synth;

public enum EvInstrEnum {
    GONG,
    GING,
    GANG;

    public static final int size;
    public static final String[] displayVals;
    static {
        EvInstrEnum[] vals = values();
        size = vals.length;
        displayVals = new String[size];
        for (int i = 0; i < size; i++)
            displayVals[i] = vals[i].toString();
    }

    public static EvInstrEnum fromString(String s) {
        return switch (s) {
            case "Gong" -> GONG;
            case "Ging" -> GING;
            case "Gang" -> GANG;
            default     -> null;
        };
    }

    public String toString() {
        return switch (this) {
            case GONG -> "Gong";
            case GING -> "Ging";
            case GANG -> "Gang";
        };
    }

    public String toFileName() {
        switch (this){
            case GONG -> {
                return "CPA_Free_Impact_30.wav";
            }
            case GANG -> {
                // TODO
                return "";
            }
            case GING -> {
                //TODO
                return "";
            }
            default -> {
                // TODO
                return "defaultSample";
            }
        }
    }
}