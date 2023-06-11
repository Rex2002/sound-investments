package dhbw.si.app.mapping;

public enum InstrParam {
	PITCH,
	RELVOLUME,
	ABSVOLUME,
	DELAY_ECHO,
	FEEDBACK_ECHO,
	ON_OFF_ECHO,
	DELAY_REVERB,
	FEEDBACK_REVERB,
	ON_OFF_REVERB,
	CUTOFF,
	ON_OFF_FILTER,
	HIGHPASS,
	PAN;

	public static InstrParam fromString(String s) {
		if (s == null) return null;
		return switch (s) {
			case "Pitch" -> PITCH;
			case "Lautstärke" -> RELVOLUME;
			case "An/Aus" -> ABSVOLUME;
			case "Echo: Delay" -> DELAY_ECHO;
			case "Echo: Level" -> FEEDBACK_ECHO;
			case "Echo: An/Aus" -> ON_OFF_ECHO;
			case "Reverb: Delay" -> DELAY_REVERB;
			case "Reverb: Level" -> FEEDBACK_REVERB;
			case "Reverb: An/Aus" -> ON_OFF_REVERB;
			case "Filter: Frequenz" -> CUTOFF;
			case "Filter: An/Aus" -> ON_OFF_FILTER;
			case "Highpass" -> HIGHPASS;
			case "Pan" -> PAN;
			default -> null;
		};
	}

	public String toString() {
		return switch (this) {
			case PITCH -> "Pitch";
			case RELVOLUME -> "Lautstärke";
			case ABSVOLUME -> "An/Aus";
			case DELAY_ECHO -> "Echo: Delay";
			case FEEDBACK_ECHO -> "Echo: Level";
			case ON_OFF_ECHO -> "Echo: An/Aus";
			case DELAY_REVERB -> "Reverb: Delay";
			case FEEDBACK_REVERB -> "Reverb: Level";
			case ON_OFF_REVERB -> "Reverb: An/Aus";
			case CUTOFF -> "Filter: Frequenz";
			case ON_OFF_FILTER -> "Filter: An/Aus";
			case HIGHPASS -> "Highpass";
			case PAN -> "Pan";
		};
	}
}