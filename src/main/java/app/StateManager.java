package app;

import app.communication.*;
import app.mapping.*;
import app.ui.App;
import audio.Sonifier;
import audio.synth.EvInstrData;
import audio.synth.EvInstrEnum;
import audio.synth.InstrumentEnum;
import audio.synth.playback.PlayControlEvent;
import audio.synth.playback.PlayControlEventsEnum;
import audio.synth.playback.PlaybackController;
import dataAnalyzer.*;
import dataRepo.*;
import javafx.application.Application;
import util.DateUtil;
import util.FutureList;
import util.Maths;

import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

// This class runs in the main thread and coordinates all tasks and the creation of the UI thread
// This is atypical, as JavaFX's UI thread is usually the main thread as well
// (see: https://stackoverflow.com/a/37580083/13764271)
// however, it makes conceptually more sense to me, as the app's logic should be done in the main thread

public class StateManager {
	public static final int FILTER_MAX_AMOUNT = 100;

	public static boolean isCurrentlySonifying = false;
	public static SonifiableFilter sonifiableFilter = new SonifiableFilter("", FilterFlag.ALL);
	public static int filterOffset = 0;
	public static Mapping currentMapping;

	public static void main(String[] args) {
		Thread th = new Thread(() -> Application.launch(App.class, args));
		th.start();
		call(DataRepo::init);

		// Check for messages in EventQueue every 100ms
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				try {
					if (!th.isAlive()) {
						// Cleanup & close app
						timer.cancel();
						// @Cleanup It feels a bit hacky to close the Playback-Thread like this
						// I can't seem to figure out how to listen to the UI closing from within the MusicSceneController,
						// where killing the Playback-Thread via PlaybackController would be simple and make sense
						// instead, I send a KILL message to the Playback thread to make sure it gets closed cleanly
						// For some reason though, the app still doesn't get closed then, so I exit the application anyways
						PlayControlEvent p = new PlayControlEvent(PlayControlEventsEnum.KILL);
						try {
							EventQueues.toPlayback.put(p);
							System.exit(0);
						} catch (InterruptedException ie) {
							System.exit(1);
						}
					}

					while (!EventQueues.toSM.isEmpty()) {
						Msg<MsgToSMType> msg = EventQueues.toSM.take();
						switch (msg.type) {
							case FILTERED_SONIFIABLES -> {
								sonifiableFilter = (SonifiableFilter) msg.data;
								DataRepo.updatedData.compareAndSet(true, false);
								sendFilteredSonifiables();
							}
							case START -> {
								if (StateManager.isCurrentlySonifying) return;
								StateManager.isCurrentlySonifying = true;
								currentMapping = (Mapping) msg.data;
								MusicData musicData = sonifyMapping(currentMapping);
								if (musicData != null)
									EventQueues.toUI.add(new Msg<>(MsgToUIType.FINISHED, musicData));
								// sonifyMapping already sends error messages to the UI in case of an error,
								// so no else case is needed
								isCurrentlySonifying = false;
							}
							case ENTERED_MAIN_SCENE -> {
								StateManager.isCurrentlySonifying = false;
								if (currentMapping != null) EventQueues.toUI.add(new Msg<>(MsgToUIType.MAPPING, currentMapping));
								EventQueues.toUI.add(new Msg<>(MsgToUIType.SONIFIABLE_FILTER, sonifiableFilter));
								sendFilteredSonifiables();
							}
						}
					}

					// Check if DataRepo has updated data for us
					if (DataRepo.updatedData.compareAndSet(true, false)) {
						System.out.println("DataRepo has updated Data");
						sendFilteredSonifiables();
					}
				} catch (InterruptedException ie) {
					getInterruptedExceptionHandler().accept(ie);
				}
			}
		}, 10, 100);
	}

	private static void sendFilteredSonifiables() throws InterruptedException {
		Sonifiable[] arr = StateManager.call(() -> DataRepo.findByPrefix(filterOffset, FILTER_MAX_AMOUNT, sonifiableFilter.prefix, sonifiableFilter.categoryFilter), new Sonifiable[0]);
		EventQueues.toUI.add(new Msg<>(MsgToUIType.FILTERED_SONIFIABLES, arr));
	}

	public static double[] getPriceValues(List<Price> prices) {
		double[] out = new double[prices.size()];
		for (int i = 0; i < out.length; i++)
			out[i] = prices.get(i).getOpen();
		return out;
	}

	// Normalizes the values to be in the range of 0 to 1 (inclusive on both ends)
	public static double[] normalizeValues(double[] prices) {
		double[] normalized = new double[prices.length];
		// Find min & max
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		for (double x : prices) {
			if (x < min)
				min = x;
			if (x > max)
				max = x;
		}
		// Calculate the Standard Deviation -> if it's too small, then we want to pack the normalized values
		// closer together as well. The cutoff-point at which we reduce the range of normalized values is
		// set arbitrarily and was decided through testing.
		// Afterward, we offset all values to put the normalized-value range into the middle between 0 and 1
		double std = Maths.std(prices);
		double stdCutoff = 10;
		double normalizedValRange = Maths.clamp(std / stdCutoff, 0.25, 1);
		double offset = (1 - normalizedValRange) / 2;
		System.out.println("normalizedValRange: " + normalizedValRange + ", offset: " + offset);
		// Normalize values
		for (int i = 0; i < normalized.length; i++)
			normalized[i] = offset + (prices[i] - min) / (max - min) * normalizedValRange;
		return normalized;
	}

	public static double[] calcLineData(ExchangeData<LineData> ed, HashMap<SonifiableID, Analyzer> priceMap) throws AppError {
		if (ed == null) return null;
		Analyzer analyzer = priceMap.get(ed.getId());
		double[] ret = new double[analyzer.getPrices().size()];
		int firstNoneNull = getValidPrice(analyzer.getPrices(), true);
		int lastNoneNull = getValidPrice(analyzer.getPrices(), false);
		analyzer.cutPrices(firstNoneNull, lastNoneNull + 1);
		double[] normalized = normalizeValues(analyzer.get(ed.getData()));
		Arrays.fill(ret, -1);
		System.arraycopy(normalized, 0, ret, firstNoneNull, normalized.length);
		return ret;

	}

	public static int getValidPrice(List<Price> prices, boolean first){
		for(int i = first ? 0 : prices.size() - 1; first ?  i < prices.size() : i >= 0; i += first ? 1 : -1){
			if(!(prices.get(i).open == -1 || prices.get(i).close == -1 || prices.get(i).high == -1 || prices.get(i).low == -1)){
				return i;
			}
		}
		return first ? prices.size() : 0;
	}

	public static boolean isDateInFormations(List<FormationResult> formations, Calendar date) {
		for (FormationResult f : formations) {
			if (date.equals(f.getStartDate()) || date.equals(f.getEndDate())
					|| (date.after(f.getStartDate()) && date.before(f.getEndDate())))
				return true;
		}
		return false;
	}

	public static boolean[] calcRangeData(ExchangeData<RangeData> ed, HashMap<SonifiableID, Analyzer> priceMap) {
		if (ed == null) return null;
		Analyzer analyzer = priceMap.get(ed.getId());
		boolean[] ret = new boolean[analyzer.getPrices().size()];
		int firstNoneNull = getValidPrice(analyzer.getPrices(), true);
		int lastNoneNull = getValidPrice(analyzer.getPrices(), false);
		analyzer.cutPrices(firstNoneNull, lastNoneNull + 1);
		boolean[] calculatedPrices = analyzer.get(ed.getData());
		Arrays.fill(ret, false);
		System.arraycopy(calculatedPrices, 0, ret, firstNoneNull, calculatedPrices.length);
		return ret;
	}

	public static boolean[] calcPointData(ExchangeData<PointData> ed, HashMap<SonifiableID, Analyzer> priceMap) throws AppError {
		if (ed == null) return null;
		Analyzer analyzer = priceMap.get(ed.getId());
		return analyzer.get(ed.getData());
	}

	public static void padPrices(Map<SonifiableID, Analyzer> priceMap, Calendar startDate, Calendar endDate, IntervalLength interval, int maxLength) {
		for(SonifiableID id : priceMap.keySet()){
			List<Price> prices = priceMap.get(id).getPrices();
			int lengthDiff = maxLength - prices.size();
			if(lengthDiff == 0){
				continue;
			}

			double dayDifferenceStart = ChronoUnit.DAYS.between( startDate.toInstant(), prices.get(0).getDay().toInstant());
			double dayDifferenceEnd = ChronoUnit.DAYS.between(prices.get(prices.size()-1).getDay().toInstant(), endDate.toInstant());

			// find the fraction of days that should be padded before the start
			int paddingsBefore = (int) ((maxLength - prices.size()) * (dayDifferenceStart / (dayDifferenceEnd + dayDifferenceStart)));
			for(int i = 0; i < paddingsBefore && prices.size() < maxLength; i++){
				prices.add(0, new Price(startDate, prices.get(0).start, prices.get(0).end, -1.0,-1.0,-1.0,-1.0));
			}
			// fill the rest (which is the equivalent to paddingsAfter) until size is maxLength
			while(prices.size() < maxLength){
				prices.add(new Price(startDate, prices.get(0).start, prices.get(0).end, -1.0,-1.0,-1.0,-1.0));
			}
			// not quite sure if this is necessary, but not taking any risks for weird bugs...
			priceMap.get(id).setPrices(prices);
			System.out.println("Updated length: " + prices.size());
		}
	}

	public static IntervalLength determineIntervalLength(Calendar start, Calendar end) {
		if (start.get(Calendar.YEAR) < 2020) return IntervalLength.DAY;
		int yearDiff = end.get(Calendar.YEAR) - start.get(Calendar.YEAR);
		assert yearDiff >= 0;
		if (yearDiff >= 3) return IntervalLength.DAY;
		if (yearDiff >= 1) return IntervalLength.HOUR;
		return IntervalLength.MIN;
	}

	public static MusicData sonifyMapping(Mapping mapping) {
		return call(() -> {
			SonifiableID[] sonifiables = mapping.getMappedSonifiableIDs().toArray(new SonifiableID[0]);
			HashMap<SonifiableID, Analyzer> priceMap = new HashMap<>(sonifiables.length);
			FutureList<List<Price>> getPricesFutures = new FutureList<>(sonifiables.length);
			IntervalLength intervalLength = determineIntervalLength(mapping.getStartDate(), mapping.getEndDate());
			for (SonifiableID sonifiableID : sonifiables) {
				getPricesFutures.add(DataRepo.getPrices(sonifiableID, mapping.getStartDate(), mapping.getEndDate(), intervalLength));
			}

			int maxPriceLen = 0;
			try {
				List<List<Price>> prices = getPricesFutures.getAll(new ArrayList<>(sonifiables.length));
				assert prices.size() == sonifiables.length;
				for (int i = 0; i < prices.size(); i++) {
					if (prices.get(i) == null || prices.get(i).size() == 0)
						throw new AppError("Fehler beim Einholen von Preis-Daten von " + sonifiables[i].getSymbol());
					priceMap.put(sonifiables[i], new Analyzer(prices.get(i)));
					System.out.println("Length: " + prices.get(i).size());
					maxPriceLen = Math.max(maxPriceLen, prices.get(i).size());
				}
        		padPrices(priceMap, mapping.getStartDate(), mapping.getEndDate(), intervalLength, maxPriceLen);
			} catch (ExecutionException e) {
				throw new AppError(e.getMessage());
			} catch (InterruptedException e) {
				throw new AppError("Fehler beim Einholen von Preisdaten.");
			}


			// Create InstrumentDataRaw objects for Harmonizer
			InstrumentMapping[] instrMappings = mapping.getMappedInstruments();
			List<InstrumentDataRaw> instrRawData = new ArrayList<>(instrMappings.length);
			for (InstrumentMapping instrMap : instrMappings) {
				if (instrMap.getPitch() == null)
					continue;

				InstrumentEnum instrument = instrMap.getInstrument();
				double[] pitch = calcLineData(instrMap.getPitch(), priceMap);
				double[] relVolume = calcLineData(instrMap.getRelVolume(), priceMap);
				boolean[] absVolume = calcRangeData(instrMap.getAbsVolume(), priceMap);
				double[] delayEcho = calcLineData(instrMap.getDelayEcho(), priceMap);
				double[] feedbackEcho = calcLineData(instrMap.getFeedbackEcho(), priceMap);
				boolean[] onOffEcho = calcRangeData(instrMap.getOnOffEcho(), priceMap);
				double[] delayReverb = calcLineData(instrMap.getDelayReverb(), priceMap);
				double[] feedbackReverb = calcLineData(instrMap.getFeedbackReverb(), priceMap);
				boolean[] onOffReverb = calcRangeData(instrMap.getOnOffReverb(), priceMap);
				double[] frequency = calcLineData(instrMap.getCutoff(), priceMap);
				boolean highPass = mapping.getHighPass();
				boolean[] onOffFilter = calcRangeData(instrMap.getOnOffFilter(), priceMap);
				double[] pan = calcLineData(instrMap.getPan(), priceMap);

				instrRawData.add(new InstrumentDataRaw(relVolume, absVolume, pitch, instrument, delayEcho, feedbackEcho,
						onOffEcho, delayReverb, feedbackReverb, onOffReverb, frequency, highPass, onOffFilter, pan));
			}
			InstrumentDataRaw[] passedInstrRawDatas = new InstrumentDataRaw[instrRawData.size()];
			passedInstrRawDatas = instrRawData.toArray(passedInstrRawDatas);

			// Create EventInstrumentRawData
			List<EvInstrData> evInstrRawDatas = new ArrayList<>();
			for(EvInstrMapping evInstrMap : mapping.getEventInstruments()){
				if(evInstrMap == null || evInstrMap.getData() == null){
					continue;
				}
				EvInstrEnum instrType = evInstrMap.getInstrument();
				boolean[] triggers = calcPointData(evInstrMap.getData(), priceMap);
				evInstrRawDatas.add(new EvInstrData(instrType, triggers));
			}
			EvInstrData[] evInstrDatas = new EvInstrData[evInstrRawDatas.size()];
			evInstrDatas = evInstrRawDatas.toArray(evInstrDatas);

			// Create GlobalFxDataRaw for Harmonizer
			double[] cutOffFrequency = calcLineData(mapping.getCutoff(), priceMap);
			double[] delayReverb = calcLineData(mapping.getDelayReverb(), priceMap);
			double[] feedbackReverb = calcLineData(mapping.getFeedbackReverb(), priceMap);
			boolean[] onOffReverb = calcRangeData(mapping.getOnOffReverb(), priceMap);
			boolean[] onOffFilter = calcRangeData(mapping.getOnOffFilter(), priceMap);
			boolean isHighPass = mapping.getHighPass();
			GlobalFxDataRaw globalFxDataRaw = new GlobalFxDataRaw(delayReverb, feedbackReverb,cutOffFrequency,onOffReverb, onOffFilter, isHighPass);

			PlaybackController pbc = Sonifier.sonify(passedInstrRawDatas, evInstrDatas, globalFxDataRaw, mapping.getSoundLength());


			String[] sonifiableNames = new String[sonifiables.length];
			for (int i = 0; i < sonifiableNames.length; i++) {
				sonifiableNames[i] = DataRepo.getSonifiableName(sonifiables[i]);
			}
			isCurrentlySonifying = false;
			List<List<Price>> musicDataPrices = new ArrayList<>(priceMap.size());
			for (Analyzer analyzer : priceMap.values()) musicDataPrices.add(analyzer.getPrices());
			return new MusicData(pbc, sonifiableNames, musicDataPrices);
		}, null);

	}

	public static Consumer<InterruptedException> getInterruptedExceptionHandler() {
		return (ie -> {
			// TODO: For now we just crash the application when we fail to establish connection with the UI
			// Is that really what we want to do?
			// What else could we even do?
			ie.printStackTrace();
			System.exit(1);
		});
	}

	public static <T> T call(AppSupplier<T> func, T alternative, Consumer<InterruptedException> errHandler) {
		try {
			try {
				return func.call();
			} catch (AppError e) {
				EventQueues.toUI.put(new Msg<>(MsgToUIType.ERROR, e.getMessage()));
				return alternative;
			}
		} catch (InterruptedException ie) {
			errHandler.accept(ie);
			return alternative;
		}
	}

	public static <T> T call(AppSupplier<T> func, T alternative) {
		return call(func, alternative, getInterruptedExceptionHandler());
	}

	public static void call(AppFunction func, Consumer<InterruptedException> errHandler) {
		try {
			try {
				func.call();
			} catch (AppError e) {
				EventQueues.toUI.put(new Msg<>(MsgToUIType.ERROR, e.getMessage()));
			}
		} catch (InterruptedException ie) {
			errHandler.accept(ie);
		}
	}

	public static void call(AppFunction func) {
		call(func, getInterruptedExceptionHandler());
	}
}