package app.communication;

public enum MsgToSMType implements MsgType {
	FILTERED_SONIFIABLES, // data: SonifiableFilter
	SAVE_MAPPING, // data: NamedMapping
	LOAD_MAPPING, // data: String
	START, // data: Mapping
	BACK_IN_MAIN_SCENE // data: null
}