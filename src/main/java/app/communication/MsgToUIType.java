package app.communication;

public enum MsgToUIType implements MsgType {
	FILTERED_SONIFIABLES, // data: List<Sonifiable>
	ERROR, // data: String
	MAPPING, // data: Mapping
	FINISHED; // data: MusicData
}
