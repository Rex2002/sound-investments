package dhbw.si.dataAnalyzer;

import java.util.Arrays;
import java.util.List;

import dhbw.si.app.mapping.LineData;
import dhbw.si.app.mapping.PointData;
import dhbw.si.app.mapping.RangeData;
import dhbw.si.dataRepo.Price;

public class Analyzer {
	private List<Price> prices;
	private boolean[] triangles;
	private boolean[] flags;
	private boolean[] vs;
	private double[] movingAvg;
	private double min = Double.MAX_VALUE;
	private double max = Double.MIN_VALUE;

	public Analyzer(List<Price> prices) {
		this.prices = prices;
	}

	public double[] get(LineData x) {
		return switch (x) {
			case PRICE     -> getPriceValues();
			case MOVINGAVG -> getMovingAvg();
			case RELCHANGE -> getRelChange();
		};
	}

	public boolean[] get(RangeData x) {
		return switch (x) {
			case TRIANGLE -> getTriangleFormations();
			case FLAG     -> getFlagFormations();
			case VFORM    -> getVFormations();
		};
	}

	public boolean[] get(PointData x) {
		return switch (x) {
			case TRENDBREAK  -> getTrendbreaks();
			case EQMOVINGAVG -> getEqMovingAvg();
			case EQSUPPORT   -> getEqSupport();
			case EQRESIST    -> getEqResist();
		};
	}

	public List<Price> getPrices() {
		return prices;
	}

	public void setPrices(List<Price> prices) {
		this.prices = prices;
	}

	public void cutPrices(int start, int end) {
		prices = prices.subList(start, end);
	}

	public double[] getPriceValues() {
		double[] out = new double[prices.size()];
		for (int i = 0; i < out.length; i++)
			out[i] = prices.get(i).getOpen();
		return out;
	}

	public boolean[] getTriangleFormations() {
		if (triangles == null)
			triangles = TriangleFormationAnalyzer.analyze(prices);
		return triangles;
	}

	public boolean[] getFlagFormations() {
		if (flags == null)
			flags = FlagFormationAnalyzer.analyze(prices);
		return flags;
	}

	public boolean[] getVFormations() {
		if (vs == null)
			vs = VFormationAnalyzer.analyze(prices);
		return vs;
	}

	public boolean[] getTrendbreaks() {
		boolean[] out = new boolean[prices.size()];
		Arrays.fill(out, false);
		accTrendbreaks(out, getTriangleFormations());
		accTrendbreaks(out, getFlagFormations());
		accTrendbreaks(out, getVFormations());
		return out;
	}

	private static void accTrendbreaks(boolean[] breaks, boolean[] trends){
        for(int i=0; i < breaks.length - 1; i++){
            breaks[i] |= trends[i] && !trends[i + 1];
        }
		breaks[breaks.length - 1] = false;
    }

	public double[] getMovingAvg() {
		if (movingAvg == null)
			movingAvg = GeneralTrends.calculateMovingAverage(prices);
		return movingAvg;
	}

	public boolean[] getEqMovingAvg() {
		return GeneralTrends.AverageIntersectsStock(getMovingAvg(), prices);
	}

	private void setMinMax() {
		if (min != Double.MAX_VALUE) return;
		for (Price p : prices) {
			if      (p.getLow() < min) min = p.getLow();
			else if (p.getHigh() > max) max = p.getHigh();
		}
	}

	// Global Minimum = Support
	public boolean[] getEqSupport() {
		setMinMax();
		boolean[] out = new boolean[prices.size()];
		for (int i = 0; i < out.length; i++)
			out[i] = prices.get(i).getLow() == min;
		return out;
	}

	// Global Maximum = Resistance
	public boolean[] getEqResist() {
		setMinMax();
		boolean[] out = new boolean[prices.size()];
		for (int i = 0; i < out.length; i++)
			out[i] = prices.get(i).getHigh() == max;
		return out;
	}

	public double[] getRelChange() {
		double[] slope = new double[prices.size()];
		for (int i = 0; i < prices.size(); i++) {
			Price p = prices.get(i);
			slope[i] = p.getOpen() - p.getClose();
		}
		return slope;
	}
}
