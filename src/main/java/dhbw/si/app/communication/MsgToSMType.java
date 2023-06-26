package dhbw.si.app.communication;

/**
 * @author V. Richter
 */
public enum MsgToSMType implements MsgType {
	FILTERED_SONIFIABLES, // data: SonifiableFilter
	START, // data: Mapping
	ENTERED_MAIN_SCENE // data: null
}