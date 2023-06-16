package dhbw.si.util;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author V. Richter
 */
public class ArrayFunctions {
	public static <T> int findIndex(T[] xs, Predicate<T> f) {
		for (int i = 0; i < xs.length; i++) {
			if (f.test(xs[i])) return i;
		}
		return -1;
	}

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

	public static <T> T[] add(T[] a, T[] b) {
		T[] res = Arrays.copyOf(a, a.length + b.length);
		System.arraycopy(b, 0, res, a.length, b.length);
		return res;
	}

	public static<T> T clampedArrAccess(int idx, T[] arr) {
		if (idx <= 0) return arr[0];
		if (idx >= arr.length) return arr[arr.length - 1];
		return arr[idx];
	}

	public static<T> void rmDuplicates(List<T> list, int searchRadius, TwoValPredicate<T, T> areEqual) {
		for (int i = 0; i < list.size() - 1; i++) {
			T el = list.get(i);
			for (int j = i + 1, max = i + searchRadius; j < list.size() && j < max; j++) {
				if (areEqual.test(el, list.get(j))) {
					list.remove(j);
					j--;
					max--;
				}
			}
		}
	}
}
