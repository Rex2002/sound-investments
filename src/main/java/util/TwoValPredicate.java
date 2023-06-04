package util;

@FunctionalInterface
public interface TwoValPredicate<S, T> {
	public boolean test(S x, T y);
}
