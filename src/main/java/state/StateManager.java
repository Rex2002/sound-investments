package state;

import java.util.List;

import UI.App;
import dataRepo.DataRepo;
import dataRepo.Price;
import dataRepo.Sonifiable;
import dataRepo.DataRepo.FilterFlag;
import dataRepo.DataRepo.IntervalLength;
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
			List<Sonifiable> l = DataRepo.getAll(FilterFlag.ALL);
			l = l.subList(0, Math.min(l.size(), 10));
			if (!l.isEmpty()) {
				for (Sonifiable x : l)
					System.out.println(x);
				Sonifiable x = l.get(0);
				List<Price> prices = DataRepo.getPrices(x, x.getEarliest(), x.getLatest(), IntervalLength.DAY);
				for (Price p : prices.subList(0, Math.min(prices.size(), 10)))
					System.out.println(p);
				EventQueues.toUI.put(l);
			}
		} catch (Exception e) {
			System.out.println("ERROR:");
			e.printStackTrace();
		}
	}
}
