package state;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class EventQueues {
	public static final BlockingQueue<String> toUI = new ArrayBlockingQueue<>(10);
}
