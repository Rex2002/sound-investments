package state;

public class Msg<T extends MsgType> {
	public T type;
	public Object data = null;

	Msg(T type) {
		this.type = type;
	}

	Msg(T type, Object data) {
		this.type = type;
		this.data = data;
	}
}
