package json;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import javax.lang.model.type.NullType;

@SuppressWarnings("unchecked")
public class JsonPrimitive<T> {
	private T el;

	public JsonPrimitive(T el) {
		this.el = el;
	}

	public Class<? extends Object> getElClass() {
		return this.el.getClass();
	}

	public T get() {
		return this.el;
	}

	public <S> S as(Type S) {
		return (S) this.el;
	}

	public NullType asNull() {
		return (NullType) this.el;
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

	public <S> List<S> applyList(Function<JsonPrimitive<?>, S> func) {
		List<JsonPrimitive<?>> list = asList();
		List<S> res = new ArrayList<>(list.size());
		for (JsonPrimitive<?> x : list) {
			res.add(func.apply(x));
		}
		return res;
	}

	public static JsonPrimitive<NullType> Null() {
		return new JsonPrimitive<NullType>(null);
	}

	@Override
	public String toString() {
		if (el == null) {
			return "null";
		} else if (el instanceof Number || el instanceof Boolean) {
			return el.toString();
		} else if (el instanceof String) {
			return "\"" + asStr() + "\"";
		} else if (el instanceof List) {
			List<JsonPrimitive<?>> l = asList();
			StringBuilder sb = new StringBuilder("[");
			for (int i = 0; i < l.size(); i++) {
				if (i > 0)
					sb.append(", ");
				sb.append(l.get(i).toString());
			}
			sb.append("]");
			return sb.toString();
		} else if (el instanceof HashMap) {
			HashMap<String, JsonPrimitive<?>> m = asMap();
			StringBuilder sb = new StringBuilder("{");
			boolean isFirst = true;
			for (Entry<String, JsonPrimitive<?>> e : m.entrySet()) {
				if (!isFirst)
					sb.append(", ");
				else
					isFirst = false;
				sb.append("\"" + e.getKey() + "\": " + e.getValue().toString());
			}
			sb.append("}");
			return sb.toString();
		} else {
			assert false : "unreachable";
			return "UNREACHABLE";
		}
	}
}
