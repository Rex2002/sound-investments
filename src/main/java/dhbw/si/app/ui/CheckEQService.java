package dhbw.si.app.ui;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import java.util.ArrayList;
import java.util.List;
import dhbw.si.app.communication.*;

/**
 * @author V. Richter
 */
public class CheckEQService extends ScheduledService<List<Msg<MsgToUIType>>> {
	private final List<Msg<MsgToUIType>> l = new ArrayList<>(8);

	protected Task<List<Msg<MsgToUIType>>> createTask() {
		return new Task<>() {
			@Override
			protected List<Msg<MsgToUIType>> call() {
				l.clear();
				EventQueues.toUI.drainTo(l, 8);
				return l;
			}
		};
	}
}
