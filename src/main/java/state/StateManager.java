package state;

import UI.App;
import dataRepo.DataRepo;
import dataRepo.DataRepo.FilterFlag;
import javafx.application.Application;

// This class runs in the main thread and coordinates all tasks and the creation of the UI thread
// This is atypical, as JavaFX's UI thread is usually the main thread as well
// (see: https://stackoverflow.com/a/37580083/13764271)
// however, it makes conceptually more sense, as the app's logic should be done in the main thread

public class StateManager {
	public static void main(String[] args) {
		Thread th = new Thread(() -> Application.launch(App.class, args));
		th.start();
		DataRepo.init();

		try {
			EventQueues.toUI.put(DataRepo.getSlice(0, 10, FilterFlag.STOCK));
		} catch (Exception e) {
			System.out.println("ERROR:");
			e.printStackTrace();
		}
	}
}
