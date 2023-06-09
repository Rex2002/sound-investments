package util;

public class Maths {
	public static double clamp(double x, double min, double max) {
		if (x < min) return min;
		if (x > max) return max;
		return x;
	}

	// See: https://www.desmos.com/calculator/3nakwtzuis
	public static double sigmoid(double x, double fact, double offset) {
		return 1 / (1 + Math.exp(-fact*x - offset));
	}

	public static double max(double[] xs) {
		double max = Double.MIN_VALUE;
		for (double x : xs)
			if (x > max) max = x;
		return max;
	}

	public static double min(double[] xs) {
		double min = Double.MAX_VALUE;
		for (double x : xs)
			if (x < min) min = x;
		return min;
	}

	public static double mean(double[] xs) {
		double mean = 0;
		for (double x : xs)
			mean += x;
		return mean / xs.length;
		// This would be more precise, bc we keep values closer to 0
		// which means we are dealing with more precise double values
		// however, because of the many divisions, it's also much slower
		// for (double x : xs)
		//     mean += x/xs.length;
		// return mean;
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
