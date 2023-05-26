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
}
