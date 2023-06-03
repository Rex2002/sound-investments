package app.mapping;

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
	ORDER,
	ON_OFF_FILTER,
	HIGHPASS,
	PAN;

	public static InstrParam[] LineDataParams = { PITCH, RELVOLUME, DELAY_ECHO, FEEDBACK_ECHO, DELAY_REVERB,
			FEEDBACK_REVERB,
			CUTOFF, ORDER, PAN };
	public static InstrParam[] RangeDataParams = { ABSVOLUME, ON_OFF_FILTER, ON_OFF_REVERB, ON_OFF_ECHO };
	public static InstrParam[] BoolParams = { HIGHPASS };

	public static InstrParam fromString(String s) {
		if (s == null) return null;
		return switch (s) {
			case "Pitch" -> PITCH;
			case "Relvolume" -> RELVOLUME;
			case "Absvolume" -> ABSVOLUME;
			case "Delay_Echo" -> DELAY_ECHO;
			case "Feedback_Echo" -> FEEDBACK_ECHO;
			case "On_Off_Echo" -> ON_OFF_ECHO;
			case "Delay_Reverb" -> DELAY_REVERB;
			case "Feedback_Reverb" -> FEEDBACK_REVERB;
			case "On_Off_Reverb" -> ON_OFF_REVERB;
			case "Cutoff" -> CUTOFF;
			case "Order" -> ORDER;
			case "On_Off_Filter" -> ON_OFF_FILTER;
			case "Highpass" -> HIGHPASS;
			case "Pan" -> PAN;
			default -> null;
		};
	}

	public String toString() {
		return switch (this) {
			case PITCH -> "Pitch";
			case RELVOLUME -> "Relvolume";
			case ABSVOLUME -> "Absvolume";
			case DELAY_ECHO -> "Delay_Echo";
			case FEEDBACK_ECHO -> "Feedback_Echo";
			case ON_OFF_ECHO -> "On_Off_Echo";
			case DELAY_REVERB -> "Delay_Reverb";
			case FEEDBACK_REVERB -> "Feedback_Reverb";
			case ON_OFF_REVERB -> "On_Off_Reverb";
			case CUTOFF -> "Cutoff";
			case ORDER -> "Order";
			case ON_OFF_FILTER -> "On_Off_Filter";
			case HIGHPASS -> "Highpass";
			case PAN -> "Pan";
		};
	}
}
