package app.communication;

import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import javafx.scene.chart.XYChart;

import dataRepo.Price;
import audio.synth.playback.PlaybackController;

public class MusicData {
	public PlaybackController pbc;
	// sonifiableNames and prices are parallel arrays
	// meaning that prices[i] gives the list of prices for sonifiableNames[i]
	// It is also guranteed that every inner array in prices has the same length
	public String[] sonifiableNames;
	public List<XYChart.Series<Integer, Double>> prices;
	public Calendar[] dates;

	public MusicData(PlaybackController pbc, String[] sonifiableNames, Collection<List<Price>> prices) {
		assert sonifiableNames.length == prices.size();
		this.pbc = pbc;
		this.sonifiableNames = sonifiableNames;

		this.dates = null;
		this.prices = new ArrayList<>(prices.size());
		for (List<Price> priceList : prices) {
			boolean firstIteration = this.dates == null;
			if (firstIteration)
				this.dates = new Calendar[priceList.size()];

			XYChart.Series<Integer, Double> series = new XYChart.Series<>();
			for (int i = 0; i < priceList.size(); i++) {
				double p = priceList.get(i).getOpen();
				if (p != 0)
					series.getData().add(new XYChart.Data<>(i, p));
				if (firstIteration)
					this.dates[i] = priceList.get(i).getDay();
			}
			this.prices.add(series);
		}
	}
}
