package app.communication;

public class Msg<T extends MsgType> {
	public T type;
	public Object data = null;

	public Msg(T type) {
		this.type = type;
	}

	public Msg(T type, Object data) {
		this.type = type;
		this.data = data;
	}
}
