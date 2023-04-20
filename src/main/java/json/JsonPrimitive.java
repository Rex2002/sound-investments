package json;

import javax.lang.model.type.NullType;

public class JsonPrimitive<T> {
	public Class<?> clz;
	public T el;

	public JsonPrimitive(Class<T> clz, T el) {
		this.clz = clz;
		this.el = el;
	}

	public JsonPrimitive(T el) {
		this.clz = el.getClass();
		this.el = el;
	}

	public static JsonPrimitive<NullType> Null() {
		return new JsonPrimitive<NullType>(NullType.class, null);
	}
}
