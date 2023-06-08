package util;

@FunctionalInterface
public interface TwoValPredicate<S, T> {
	boolean test(S x, T y);
}