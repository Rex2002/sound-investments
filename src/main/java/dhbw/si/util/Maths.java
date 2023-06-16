package dhbw.si.util;

/**
 * @author V. Richter
 */
public class Maths {
	public static double clamp(double x, double min, double max) {
		return x < min ? min : Math.min(x, max);
	}

	public static double mean(double[] xs) {
		double mean = 0;
		for (double x : xs)
			mean += x;
		return mean / xs.length;
	}

	public static double var(double[] xs) {
		double mean = mean(xs);
		double var = 0;
		for (double x : xs)
			var += (x - mean) * (x - mean);
		return var / xs.length;
	}

	public static double std(double[] xs) {
		return Math.sqrt(var(xs));
	}
}
