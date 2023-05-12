package state;

import UI.App;
import javafx.application.Application;

// This class runs in the main thread and coordinates all tasks and the creation of the UI thread
// This is atypical, as JavaFX's UI thread is usually the main thread as well
// (see: https://stackoverflow.com/a/37580083/13764271)
// however, it makes conceptually more sense, as the app's logic should be done in the main thread

public class StateManager {
	public static void main(String[] args) {
		Thread th = new Thread(() -> Application.launch(App.class, args));
		th.start();

		try {
			Thread.sleep(1000);
			Integer i = 0;
			while (true) {
				System.out.println(i.toString());
				EventQueues.toUI.add(i.toString());
				i++;
				Thread.sleep(50);
			}
		} catch (InterruptedException e) {
			System.out.println("ERROR");
			e.printStackTrace();
		}
	}
}
