package util;

import java.util.function.Function;
import java.util.function.Predicate;

public class ArrayFunctions {
	public static <T> T find(T[] xs, Predicate<T> f) {
		for (T x : xs) {
			if (f.test(x))
				return x;
		}
		return null;
	}

	public static <T> String toStringArr(T[] xs) {
		return toStringArr(xs, x -> {
			if (x == null)
				return "null";
			else
				return x.toString();
		}, false);
	}

	public static <T> String toStringArr(T[] xs, Function<T, String> f, boolean skipNull) {
		StringBuilder sb = new StringBuilder(64);
		sb.append("[");
		for (int i = 0; i < xs.length; i++) {
			if (skipNull && xs[i] == null)
				continue;
			if (i > 0)
				sb.append(", ");
			sb.append(f.apply(xs[i]));
		}
		sb.append("]");
		return sb.toString();
	}
}
