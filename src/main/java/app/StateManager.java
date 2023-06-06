package app;

import audio.Sonifier;
import audio.synth.EvInstrData;
import audio.synth.EvInstrEnum;
import audio.synth.InstrumentEnum;
import audio.synth.playback.PlayControlEvent;
import audio.synth.playback.PlayControlEventsEnum;
import audio.synth.playback.PlaybackController;
import javafx.application.Application;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import app.communication.*;
import app.mapping.*;
import app.ui.App;
import dataAnalyzer.*;
import dataRepo.*;
import util.*;

// This class runs in the main thread and coordinates all tasks and the creation of the UI thread
// This is atypical, as JavaFX's UI thread is usually the main thread as well
// (see: https://stackoverflow.com/a/37580083/13764271)
// however, it makes conceptually more sense to me, as the app's logic should be done in the main thread

public class StateManager {
	public static boolean isCurrentlySonifying = false;
	public static SonifiableFilter sonifiableFilter = new SonifiableFilter("", FilterFlag.ALL);
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
								isCurrentlySonifying = false;
							}
							case ENTERED_MAIN_SCENE -> {
								StateManager.isCurrentlySonifying = false;
								if (currentMapping != null) EventQueues.toUI.add(new Msg<>(MsgToUIType.MAPPING, currentMapping));
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
		List<Sonifiable> list = StateManager.call(() -> DataRepo.findByPrefix(sonifiableFilter.prefix, sonifiableFilter.categoryFilter), List.of());
		EventQueues.toUI.add(new Msg<>(MsgToUIType.FILTERED_SONIFIABLES, list));
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
			else if (x > max)
				max = x;
		}
		// Normalize values
		for (int i = 0; i < normalized.length; i++)
			normalized[i] = (prices[i] - min) / (max - min);
		return normalized;
	}

	public static double[] calcLineData(ExchangeData<LineData> ed, HashMap<SonifiableID, List<Price>> priceMap) throws AppError {
		if (ed == null) return null;
		List<Price> prices = priceMap.get(ed.getId());
		return switch (ed.getData()) {
			case PRICE -> normalizeValues(getPriceValues(prices));
			case MOVINGAVG -> normalizeValues(GeneralTrends.calculateMovingAverage(prices));
			case RELCHANGE -> throw new AppError("RELCHANGE is not yet implemented");
		};
	}

	public static boolean isDateInFormations(List<FormationResult> formations, Calendar date) {
		for (FormationResult f : formations) {
			if (date.equals(f.getStartDate()) || date.equals(f.getEndDate())
					|| (date.after(f.getStartDate()) && date.before(f.getEndDate())))
				return true;
		}
		return false;
	}

	public static boolean[] calcRangeData(ExchangeData<RangeData> ed, HashMap<SonifiableID, List<Price>> priceMap) throws AppError {
		if (ed == null) return null;
		List<Price> prices = priceMap.get(ed.getId());
		return switch (ed.getData()) {
			case FLAG -> FlagFormationAnalyzer.analyze(prices);
			case TRIANGLE -> new TriangleFormationAnalyzer().analyze(prices);
			case VFORM -> new VFormationAnalyzer().analyze(prices);
		};
	}

	public static boolean[] calcPointData(ExchangeData<PointData> ed, HashMap<SonifiableID, List<Price>> priceMap) throws AppError {
		if (ed == null) return null;
		List<Price> prices = priceMap.get(ed.getId());
		return switch (ed.getData()) {
			case EQMOVINGAVG -> GeneralTrends.AverageIntersectsStock(GeneralTrends.calculateMovingAverage(prices), prices);
			case TRENDBREAK -> throw new AppError("TRENDBREAK is not yet implemented");
			case EQSUPPORT -> throw new AppError("EQSUPPORT is not yet implemented");
			case EQRESIST -> throw new AppError("EQRESIST is not yet implemented");
		};
	}

	// Change the mapping to test different functionalities
	public static Mapping getTestMapping() {
		Mapping mapping = new Mapping();
		try {
			mapping.setStartDate(DateUtil.calFromDateStr("2022-06-16"));
			mapping.setEndDate(DateUtil.calFromDateStr("2023-05-16"));
			mapping.setSoundLength(60);

			Sonifiable s = new Stock("SAP", new SonifiableID("SAP", "XETRA"));
			mapping.setParam(InstrumentEnum.RETRO_SYNTH, s, InstrParam.PITCH, LineData.PRICE);
			mapping.setParam(InstrumentEnum.RETRO_SYNTH, s, InstrParam.RELVOLUME, LineData.MOVINGAVG);
			mapping.setHighPass(true);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return mapping;
	}

	public static void padPrices(Map<SonifiableID, List<Price>> priceMap, Calendar startDate, Calendar endDate, IntervalLength interval, int maxLength){
		for(SonifiableID id : priceMap.keySet()){
			List<Price> prices = priceMap.get(id);
			int lengthDiff = maxLength - prices.size();
			if(lengthDiff == 0){
				continue;
			}

			double dayDifferenceStart = ChronoUnit.DAYS.between( startDate.toInstant(), prices.get(0).getDay().toInstant());
			double dayDifferenceEnd = ChronoUnit.DAYS.between(prices.get(prices.size()-1).getDay().toInstant(), endDate.toInstant());

			// find the fraction of days that should be padded before the start
			int paddingsBefore = (int) ((maxLength - prices.size()) * (dayDifferenceStart / (dayDifferenceEnd + dayDifferenceStart)));
			for(int i = 0; i < paddingsBefore && prices.size() < maxLength; i++){
				prices.add(0, new Price(startDate, prices.get(0).start, prices.get(0).end, 0.0,0.0,0.0,0.0));
			}
			// fill the rest (which is the equivalent to paddingsAfter) until size is maxLength
			while(prices.size() < maxLength){
				prices.add(new Price(startDate, prices.get(0).start, prices.get(0).end, 0.0,0.0,0.0,0.0));
			}
			// not quite sure if this is necessary, but not taking any risks for weird bugs...
			priceMap.put(id, prices);
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
			HashMap<SonifiableID, List<Price>> priceMap = new HashMap<>(sonifiables.length);
			FutureList<List<Price>> getPricesFutures = new FutureList<>(sonifiables.length);
			IntervalLength intervalLength = determineIntervalLength(mapping.getStartDate(), mapping.getEndDate());
			for (SonifiableID sonifiableID : sonifiables) {
				getPricesFutures.add(DataRepo.getPrices(sonifiableID, mapping.getStartDate(), mapping.getEndDate(), intervalLength));
			}
			try {
				List<List<Price>> prices = getPricesFutures.getAll(new ArrayList<>(sonifiables.length));
				assert prices.size() == sonifiables.length;
        int maxPriceLen = 0;
				for (int i = 0; i < prices.size(); i++) {
					if (prices.get(i) == null || prices.get(i).size() == 0)
						throw new AppError("Fehler beim Einholen von Preis-Daten von " + sonifiables[i].getSymbol());
          maxPriceLen = Math.max(maxPriceLen, prices.get(i).size());
					priceMap.put(sonifiables[i], prices.get(i));
				}
        padPrices(priceMap, mapping.getStartDate(), mapping.getEndDate(), intervalLength, maxPriceLen);
			} catch (ExecutionException e) {
				throw new AppError(e.getMessage());
			} catch (InterruptedException e) {
				throw new AppError("Fehler beim Einholen von Preisdaten.");
			}

			// Create InstrumentDataRaw objects for Harmonizer
			InstrumentMapping[] instrMappings = mapping.getMappedInstruments();
			List<InstrumentDataRaw> instrRawDatas = new ArrayList<>(instrMappings.length);
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
				boolean highPass = instrMap.getHighPass();
				boolean[] onOffFilter = calcRangeData(instrMap.getOnOffFilter(), priceMap);
				double[] pan = calcLineData(instrMap.getPan(), priceMap);

				instrRawDatas.add(new InstrumentDataRaw(relVolume, absVolume, pitch, instrument, delayEcho,
						feedbackEcho,
						onOffEcho, delayReverb, feedbackReverb, onOffReverb, frequency, highPass, onOffFilter, pan));
			}
			InstrumentDataRaw[] passedInstrRawDatas = new InstrumentDataRaw[instrRawDatas.size()];
			passedInstrRawDatas = instrRawDatas.toArray(passedInstrRawDatas);

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

			PlaybackController pbc = Sonifier.sonify(passedInstrRawDatas, evInstrDatas, mapping.getSoundLength());

			String[] sonifiableNames = new String[sonifiables.length];
			for (int i = 0; i < sonifiableNames.length; i++) {
				sonifiableNames[i] = DataRepo.getSonifiableName(sonifiables[i]);
			}
			isCurrentlySonifying = false;
			return new MusicData(pbc, sonifiableNames, priceMap.values());
		}, null, ie -> {
			isCurrentlySonifying = false;
			getInterruptedExceptionHandler().accept(ie);
		});
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