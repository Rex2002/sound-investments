package app.communication;

public enum MsgToUIType implements MsgType {
	FILTERED_SONIFIABLES, // data: List<Sonifiable>
	LOADABLE_MAPPINGS, // data: List<String>
	ERROR, // data: String
	VALIDATION_DONE, // data: null
	VALIDATION_ERROR, // data: String
	FINISHED // data: MusicData
}