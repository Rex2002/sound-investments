package util;

// Named Maths instead of Math to avoid name-conflicts with java's built-in Math
public class Maths {
    /**
     * Clamp {@code val} by the minimum {@code min} and {@code max}
     */
	public static double clamp(double val, double min, double max) {
        if (val < min) return min;
        if (val > max) return max;
        return val;
    }

    /**
     * Linearly interpolates from x0 to x1 by factor t
     */
    public static double lerp(double t, double x0, double x1) {
        return x0 + t * (x1 - x0);
        // Due to floating-point errors, the following might be more precise (though slightly slower)
        // return (1 - t) * x0 + t * x1;
    }
}
