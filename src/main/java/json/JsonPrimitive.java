package json;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.type.NullType;

public class JsonPrimitive<T> {
	private Class<?> clz;
	private T el;

	public JsonPrimitive(Class<T> clz, T el) {
		this.clz = clz;
		this.el = el;
	}

	public JsonPrimitive(T el) {
		this.clz = el.getClass();
		this.el = el;
	}

	public T get() {
		return this.el;
	}

	public <S> S as(Type S) {
		return (S) this.el;
	}

	public boolean asBool() {
		return (boolean) this.el;
	}

	public String asStr() {
		return (String) this.el;
	}

	public int asInt() {
		return ((Number) this.el).intValue();
	}

	public double asDouble() {
		return ((Number) this.el).doubleValue();
	}

	public float asFloat() {
		return ((Number) this.el).floatValue();
	}

	public long asLong() {
		return ((Number) this.el).longValue();
	}

	public List<JsonPrimitive<?>> asList() {
		return (List<JsonPrimitive<?>>) this.el;
	}

	// This operation copies the entire List. Make sure that cost is worth it.
	public <S> List<S> asList(Type S) {
		List<JsonPrimitive<?>> l = asList();
		return l.stream().map(x -> (S) x.as(S)).toList();
	}

	public HashMap<String, JsonPrimitive<?>> asMap() {
		return (HashMap<String, JsonPrimitive<?>>) this.el;
	}

	// This operation copies the entire HashMap. Make sure that cost is worth it.
	public <S> HashMap<String, S> asMap(Type S) {
		HashMap<String, JsonPrimitive<?>> m = asMap();
		HashMap<String, S> res = new HashMap<>(m.size());

		for (Map.Entry<String, JsonPrimitive<?>> e : m.entrySet()) {
			String key = e.getKey();
			S value = e.getValue().as(S);
			res.put(key, value);
		}

		return res;
	}

	public static JsonPrimitive<NullType> Null() {
		return new JsonPrimitive<NullType>(NullType.class, null);
	}
}
