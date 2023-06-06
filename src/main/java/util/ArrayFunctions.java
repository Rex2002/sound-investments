package util;

import java.util.Arrays;
import java.util.List;
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

	public static <T> T[] add(T[] a, T[] b) {
		T[] res = Arrays.copyOf(a, a.length + b.length);
		for (int i = 0; i < b.length; i++)
			res[i + a.length] = b[i];
		return res;
	}

	public static <T> void setRange(T[] src, T[] valsToSet, int offset) {
		for (int i = 0; i + offset < src.length; i++) {
			src[i + offset] = valsToSet[i];
		}
	}

	public static<T> T clampedArrAccess(int idx, T[] arr) {
		if (idx <= 0) return arr[0];
		if (idx >= arr.length) return arr[arr.length - 1];
		return arr[idx];
	}

	public static<T> T[] rmSome(T[] arr, Predicate<T> toRem) {
		int rmAmount = 0;
		for (int i = 0; i < arr.length; i++) {
			if (toRem.test(arr[i])) rmAmount++;
			else arr[i - rmAmount] = arr[i];
		}
		if (rmAmount == 0) return arr;
		return Arrays.copyOf(arr, arr.length - rmAmount);
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
