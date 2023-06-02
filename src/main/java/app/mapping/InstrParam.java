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
