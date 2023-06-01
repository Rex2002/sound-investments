package app;

import app.communication.*;
import app.mapping.*;
import app.ui.App;
import audio.Constants;
import audio.Sonifier;
import audio.synth.EvInstrData;
import audio.synth.InstrumentEnum;
import audio.synth.playback.PlaybackController;
import dataAnalyzer.*;
import dataRepo.*;
import dataRepo.DataRepo.IntervalLength;
import javafx.application.Application;

import java.util.*;
import java.util.function.Consumer;

// This class runs in the main thread and coordinates all tasks and the creation of the UI thread
// This is atypical, as JavaFX's UI thread is usually the main thread as well
// (see: https://stackoverflow.com/a/37580083/13764271)
// however, it makes conceptually more sense to me, as the app's logic should be done in the main thread

public class StateManager {
	public static void main(String[] args) {
		// testUI(args);
		testSound(args);
	}

	public static void testUI(String[] args) {
		Thread th = new Thread(() -> Application.launch(App.class, args));
		th.start();
		call(DataRepo::init);

		// Check for messages in EventQueue every 100ms
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				while (!EventQueues.toSM.isEmpty()) {
					try {
						Msg<MsgToSMType> msg = EventQueues.toSM.take();
						switch (msg.type) {
							case FILTERED_SONIFIABLES -> {
								SonifiableFilter filter = (SonifiableFilter) msg.data;
								List<Sonifiable> list = StateManager
										.call(() -> DataRepo.findByPrefix(filter.prefix, filter.categoryFilter),
												List.of());
								EventQueues.toUI.add(new Msg<>(MsgToUIType.FILTERED_SONIFIABLES, list));
							}
							case SAVE_MAPPING -> EventQueues.toUI.add(new Msg<>(MsgToUIType.ERROR, "SAVE_MAPPING is not yet implemented"));
							case LOAD_MAPPING -> EventQueues.toUI.add(new Msg<>(MsgToUIType.ERROR, "LOAD_MAPPING is not yet implemented"));
							case START -> {
								Mapping mapping = (Mapping) msg.data;
								PlaybackController pbc = sonifyMapping(mapping);
								EventQueues.toUI.add(new Msg<>(MsgToUIType.FINISHED, pbc));
							}
						}
					} catch (InterruptedException ie) {
						getInterruptedExceptionHandler().accept(ie);
					}
				}

				if (!th.isAlive()) {
					// Stop timer to allow app to close
					timer.cancel();
				}
			}
		}, 10, 100);
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
			case MOVINGAVG -> normalizeValues(new MovingAverage().calculateMovingAverage(prices));
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

	public static boolean[] formationResultToBool(List<FormationResult> formations,
												  List<Price> prices) {
		boolean[] out = new boolean[prices.size()];
		for (int i = 0; i < out.length; i++) {
			Calendar currentDate = prices.get(i).getDay();
			out[i] = isDateInFormations(formations, currentDate);
		}
		return out;
	}

	public static boolean[] calcRangeData(ExchangeData<RangeData> ed, HashMap<SonifiableID, List<Price>> priceMap) throws AppError {
		if (ed == null) return null;
		List<Price> prices = priceMap.get(ed.getId());
		return switch (ed.getData()) {
			case FLAG -> formationResultToBool(new FlagFormationAnalyzer().analyzeFormations(prices), prices);
			case TRIANGLE -> formationResultToBool(new TriangleFormationAnalyzer().analyzeFormations(prices), prices);
			case VFORM -> formationResultToBool(new VFormationAnalyzer().analyzeFormations(prices), prices);
		};
	}

	public static boolean[] calcPointData(ExchangeData<PointData> ed, HashMap<SonifiableID, List<Price>> priceMap) throws AppError {
		if (ed == null) return null;
		List<Price> prices = priceMap.get(ed.getId());
		return switch (ed.getData()) {
			case EQMOVINGAVG -> new MovingAverage().AverageIntersectsStock(new MovingAverage().calculateMovingAverage(prices), prices);
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

			SonifiableID s = new SonifiableID("XETRA", "SAP");
			mapping.setParam(InstrumentEnum.RETRO_SYNTH, s, InstrParam.PITCH, LineData.PRICE);
			mapping.setParam(InstrumentEnum.RETRO_SYNTH, s, InstrParam.RELVOLUME, LineData.MOVINGAVG);
			mapping.setHighPass(true);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return mapping;
	}

	public static PlaybackController sonifyMapping(Mapping mapping) {
		// TODO: Validate that we have enough price data for mapping
		HashMap<SonifiableID, List<Price>> priceMap = new HashMap<>();
		SonifiableID[] sonifiableSet = mapping.getSonifiables().toArray(new SonifiableID[0]);
		for (SonifiableID sonifiableID : sonifiableSet) {
			priceMap.put(
				sonifiableID,
				DataRepo.getPrices(sonifiableID, mapping.getStartDate(), mapping.getEndDate(), IntervalLength.HOUR)
			);
		}

		return call(() -> {
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

			EvInstrData[] evInstrDatas = new EvInstrData[] {};
			InstrumentDataRaw[] passedInstrRawDatas = new InstrumentDataRaw[instrRawDatas.size()];
			passedInstrRawDatas = instrRawDatas.toArray(passedInstrRawDatas);

			double numberBeatsRaw = (Constants.TEMPO / 60f) * mapping.getSoundLength();
			// get number of beats to nearest multiple of 16 so that audio always lasts for
			// a full multiple of 4 bars
			int numberBeats = (int) Math.round(numberBeatsRaw / 16) * 16;

			return Sonifier.sonify(passedInstrRawDatas, evInstrDatas, numberBeats);
		}, null);
	}

	public static void testSound(String[] args) {
		// Get Mapping & Price Data
		call(DataRepo::init);
		Mapping mapping = getTestMapping();
		PlaybackController pbc = sonifyMapping(mapping);
		pbc.startPlayback();
		boolean running = true;
		while (running) {
			System.out.println("Please enter your next control action: ");
			Scanner in = new Scanner(System.in);
			String controlAction = in.next();

			switch (controlAction) {
				// resume
				case "r" -> pbc.play();
				// pause
				case "p" -> pbc.pause();
				// jump forward 1s
				case "jf" -> pbc.skipForward();
				// jump backward 1s
				case "jb" -> pbc.skipBackward();
				case "s" -> {
					pbc.kill();
					running = false;
				}
				case "rs" -> pbc.reset();
			}
		}
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