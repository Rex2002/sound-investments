package app.communication;

public enum MsgToUIType implements MsgType {
	FILTERED_SONIFIABLES,
	LOADABLE_MAPPINGS,
	ERROR,
	VALIDATION_DONE,
	VALIDATION_ERROR,
	FINISHED;
}
