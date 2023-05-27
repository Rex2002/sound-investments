package app;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import app.communication.EventQueues;
import app.communication.Msg;
import app.communication.MsgToSMType;
import app.communication.MsgToUIType;
import app.communication.SonifiableFilter;
import app.mapping.Mapping;
import app.ui.App;
import audio.Constants;
import dataRepo.DataRepo;
import dataRepo.Sonifiable;
import javafx.application.Application;

// This class runs in the main thread and coordinates all tasks and the creation of the UI thread
// This is atypical, as JavaFX's UI thread is usually the main thread as well
// (see: https://stackoverflow.com/a/37580083/13764271)
// however, it makes conceptually more sense to me, as the app's logic should be done in the main thread

public class StateManager {
	public static void main(String[] args) {
		testUI(args);
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
				};
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

	public static void testSound(String[] args) {
		// DataRepo.init();

		// List<Sonifiable> data = DataRepo.getAll(FilterFlag.ALL);

		int soundLength = 60; // only for testing purposes, will actually be taken from Mapping
		double numberBeatsRaw = (Constants.TEMPO / 60f) * soundLength;
		// get number of beats to nearest multiple of 16 so that audio always lasts for a full multiple of 4 bars
		int numberBeats = (int) Math.round(numberBeatsRaw / 16) * 16;
		soundLength = (int) Math.ceil(numberBeats / (Constants.TEMPO / 60f));
		// TODO: Call Harmonizer with numberBeats as argument
	}
}
