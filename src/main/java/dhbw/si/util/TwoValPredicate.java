package dhbw.si.util;

/**
 * @author V. Richter
 */
@FunctionalInterface
public interface TwoValPredicate<S, T> {
	boolean test(S x, T y);
}