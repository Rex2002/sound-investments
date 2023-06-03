package app.communication;

import java.util.Calendar;

import audio.synth.playback.PlaybackController;

public class MusicData {
	public PlaybackController pbc;
	// sonifiableNames and prices are parallel arrays
	// meaning that prices[i] gives the list of prices for sonifiableNames[i]
	// It is also guranteed that every inner array in prices has the same length
	public String[] sonifiableNames;
	public double[][] prices;
	// TODO: Do we need start & end date in the MusicScene?
	// Calendar start;
	// Calendar end;

	public MusicData(PlaybackController pbc, String[] sonifiableNames, double[][] prices) {
		assert sonifiableNames.length == prices.length;
		this.pbc = pbc;
		this.sonifiableNames = sonifiableNames;
		this.prices = prices;
	}
}
