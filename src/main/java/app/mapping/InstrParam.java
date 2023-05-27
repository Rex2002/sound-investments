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
}
