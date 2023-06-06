package audio.synth;

public enum EvInstrEnum {
    BOOM,
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
            case "Boom" -> BOOM;
            case "Ging" -> GING;
            case "Gang" -> GANG;
            default     -> null;
        };
    }

    public String toString() {
        return switch (this) {
            case BOOM -> "Boom";
            case GING -> "Ging";
            case GANG -> "Gang";
        };
    }

    public String toFileName() {
        switch (this){
            case BOOM -> {
                return "Impact_Bass_0.wav";
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