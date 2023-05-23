package app.communication;

public enum MsgToSMType implements MsgType {
	FILTERED_SONIFIABLES,
	SAVE_MAPPING,
	LOAD_MAPPING,
	START,
	// Playback options
	// Should only go from UI to SM
	PLAYBACK_STOP,
	PLAYBACK_CONTINUE,
	PLAYBACK_BACK,
	PLAYBACK_FORWARD,
	PLAYBACK_GOTO;
}
