package app;

import java.util.function.Predicate;

public class Util {
	public static <T> T find(Iterable<T> xs, Predicate<T> f) {
		for (T x : xs) {
			if (f.test(x))
				return x;
		}
		return null;
	}

	public static <T> T find(T[] xs, Predicate<T> f) {
		for (T x : xs) {
			if (f.test(x))
				return x;
		}
		return null;
	}

	public static <T> String toStringArr(T[] xs) {
		StringBuilder sb = new StringBuilder(64);
		sb.append("[");
		for (int i = 0; i < xs.length; i++) {
			if (i > 0) sb.append(", ");
			if (xs[i] == null) sb.append("null");
			else sb.append(xs[i].toString());
		}
		sb.append("]");
		return sb.toString();
	}
}
