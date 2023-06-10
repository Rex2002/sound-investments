package app.communication;

public enum MsgToUIType implements MsgType {
	FILTERED_SONIFIABLES, // data: Sonifiable[]
	SONIFIABLE_FILTER, // data: SonifiableFilter
	ERROR, // data: String
	MAPPING, // data: Mapping
	FINISHED // data: MusicData
}