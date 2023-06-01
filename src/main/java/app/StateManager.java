package app;

import app.communication.*;
import app.mapping.*;
import app.ui.App;
import audio.Sonifier;
import audio.synth.EvInstrData;
import audio.synth.InstrumentEnum;
import audio.synth.playback.PlaybackController;
import dataAnalyzer.*;
import dataRepo.*;
import dataRepo.DataRepo.IntervalLength;
import javafx.application.Application;

import java.util.*;

// This class runs in the main thread and coordinates all tasks and the creation of the UI thread
// This is atypical, as JavaFX's UI thread is usually the main thread as well
// (see: https://stackoverflow.com/a/37580083/13764271)
// however, it makes conceptually more sense to me, as the app's logic should be done in the main thread

public class StateManager {
	public static void main(String[] args) {
		testSound(args);
	}

	public static void testUI(String[] args) {
		try {
			Thread th = new Thread(() -> Application.launch(App.class, args));
			th.start();
			call(() -> DataRepo.init());

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
								case START -> {
									Mapping mapping = (Mapping) msg.data;
									System.out.println(mapping);
									// TODO: Start sonification
								}
								default -> StateManager.call(() -> {
									throw new AppError(
											"Msg-Type " + msg.type.toString()
													+ " is not yet handled by the StateManager.");
								});
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
							System.exit(1);
						}
					}

					if (!th.isAlive()) {
						timer.cancel();
					}
				}
			}, 10, 100);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static <T> T call(AppSupplier<T> func, T alternative) throws InterruptedException {
		try {
			return func.call();
		} catch (AppError e) {
			EventQueues.toUI.put(new Msg<>(MsgToUIType.ERROR, e.getMessage()));
			return alternative;
		}
	}

	public static void call(AppFunction func) throws InterruptedException {
		try {
			func.call();
		} catch (AppError e) {
			EventQueues.toUI.put(new Msg<>(MsgToUIType.ERROR, e.getMessage()));
		}
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

	public static double[] calcLineData(ExchangeData<LineData> ed, HashMap<SonifiableID, List<Price>> priceMap)
			throws AppError {
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

	public static boolean[] calcRangeData(ExchangeData<RangeData> ed, HashMap<SonifiableID, List<Price>> priceMap) {
		List<Price> prices = priceMap.get(ed.getId());
		return switch (ed.getData()) {
			case FLAG -> formationResultToBool(new FlagFormationAnalyzer().analyzeFormations(prices), prices);
			case TRIANGLE -> formationResultToBool(new TriangleFormationAnalyzer().analyzeFormations(prices), prices);
			case VFORM -> formationResultToBool(new VFormationAnalyzer().analyzeFormations(prices), prices);
		};
	}

	public static boolean[] calcPointData(ExchangeData<PointData> ed, HashMap<SonifiableID, List<Price>> priceMap)
			throws AppError {
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
			mapping.setParam(InstrumentEnum.SYNTH_ONE, s, InstrParam.PITCH, LineData.PRICE);
			mapping.setParam(InstrumentEnum.SYNTH_ONE, s, InstrParam.RELVOLUME, LineData.MOVINGAVG);
			mapping.setParam(InstrumentEnum.SYNTH_ONE, s, InstrParam.ABSVOLUME, RangeData.TRIANGLE);
			mapping.setHighPass(true);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return mapping;
	}

	public static void testSound(String[] args) {
		try {
			// Get Mapping & Price Data
			call(() -> DataRepo.init());
			Mapping mapping = getTestMapping();
			HashMap<SonifiableID, List<Price>> sonifiablePriceMap = new HashMap<>();
			SonifiableID[] sonifiableSet = mapping.getSonifiables().toArray(new SonifiableID[0]);
			for (SonifiableID sonifiableID : sonifiableSet) {
				sonifiablePriceMap.put(
						sonifiableID,
						DataRepo.getPrices(sonifiableID, mapping.getStartDate(), mapping.getEndDate(),
								IntervalLength.HOUR));
			}

			// Create InstrumentDataRaw objects for Harmonizer
			InstrumentMapping[] instrMappings = mapping.getMappedInstruments();
			List<InstrumentDataRaw> instrRawDatas = new ArrayList<>(instrMappings.length);
			for (InstrumentMapping instrMap : instrMappings) {
				if (instrMap.getPitch() == null)
					continue;

				InstrumentEnum instrument = instrMap.getInstrument();
				double[] pitch = calcLineData(instrMap.getPitch(), sonifiablePriceMap);
				double[] relVolume = instrMap.getRelVolume().isPresent()
						? calcLineData(instrMap.getRelVolume().get(), sonifiablePriceMap)
						: null;
				boolean[] absVolume = instrMap.getAbsVolume().isPresent()
						? calcRangeData(instrMap.getAbsVolume().get(), sonifiablePriceMap)
						: null;
				double[] delayEcho = instrMap.getDelayEcho().isPresent()
						? calcLineData(instrMap.getDelayEcho().get(), sonifiablePriceMap)
						: null;
				double[] feedbackEcho = instrMap.getFeedbackEcho().isPresent()
						? calcLineData(instrMap.getFeedbackEcho().get(), sonifiablePriceMap)
						: null;
				boolean[] onOffEcho = instrMap.getOnOffEcho().isPresent()
						? calcRangeData(instrMap.getOnOffEcho().get(), sonifiablePriceMap)
						: null;
				double[] delayReverb = instrMap.getDelayReverb().isPresent()
						? calcLineData(instrMap.getDelayReverb().get(), sonifiablePriceMap)
						: null;
				double[] feedbackReverb = instrMap.getFeedbackReverb().isPresent()
						? calcLineData(instrMap.getFeedbackReverb().get(), sonifiablePriceMap)
						: null;
				boolean[] onOffReverb = instrMap.getOnOffReverb().isPresent()
						? calcRangeData(instrMap.getOnOffReverb().get(), sonifiablePriceMap)
						: null;
				double[] frequency = instrMap.getCutoff().isPresent()
						? calcLineData(instrMap.getCutoff().get(), sonifiablePriceMap)
						: null;
				boolean highPass = instrMap.getHighPass();
				boolean[] onOffFilter = instrMap.getOnOffFilter().isPresent()
						? calcRangeData(instrMap.getOnOffFilter().get(), sonifiablePriceMap)
						: null;
				double[] pan = instrMap.getPan().isPresent()
						? calcLineData(instrMap.getPan().get(), sonifiablePriceMap)
						: null;

				instrRawDatas.add(new InstrumentDataRaw(relVolume, absVolume, pitch, instrument, delayEcho,
						feedbackEcho,
						onOffEcho, delayReverb, feedbackReverb, onOffReverb, frequency, highPass, onOffFilter, pan));
			}
			EvInstrData[] evInstrDatas = new EvInstrData[]{};
			InstrumentDataRaw[] passedInstrRawDatas = new InstrumentDataRaw[instrRawDatas.size()];
			passedInstrRawDatas = instrRawDatas.toArray(passedInstrRawDatas);
			PlaybackController pbc = Sonifier.sonify(passedInstrRawDatas, evInstrDatas, mapping.getSoundLength());

			//PlaybackController pbc = new PlaybackController(sdl, audioLines[0]);
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
		} catch (Throwable e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}
}