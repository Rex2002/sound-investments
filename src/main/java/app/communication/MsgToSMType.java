package app.communication;

public enum MsgToSMType implements MsgType {
	FILTERED_SONIFIABLES, // data: SonifiableFilter
	SAVE_MAPPING, // data: NamedMapping
	LOAD_MAPPING, // data: String
	START, // daa: Mapping

	// Playback options:
	// TODO: Factor Playback communication to another EventQueue
	PLAYBACK_STOP,
	PLAYBACK_CONTINUE,
	PLAYBACK_BACK,
	PLAYBACK_FORWARD,
	PLAYBACK_GOTO;
}
