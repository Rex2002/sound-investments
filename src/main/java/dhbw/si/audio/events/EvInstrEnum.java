package dhbw.si.audio.events;

/**
 * @author B. Frahm
 * @author M. Richert
 */
public enum EvInstrEnum {
    BOOM,
    BIKE_BELL,
    SLEIGH_BELL;

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
            case "Bike Bell" -> BIKE_BELL;
            case "Sleigh Bell" -> SLEIGH_BELL;
            default     -> null;
        };
    }

    public String toString() {
        return switch (this) {
            case BOOM -> "Boom";
            case BIKE_BELL -> "Bike Bell";
            case SLEIGH_BELL -> "Sleigh Bell";
        };
    }

    public String toFileName() {
        switch (this){
            case BOOM -> {
                return "Impact_Timp_0.wav";
            }
            case BIKE_BELL -> {
                return "Impact_Bell_0.wav";
            }
            default -> {
                return "Impact_Bell_1.wav";
            }
        }
    }
}