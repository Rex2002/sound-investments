package app.communication;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class EventQueues {
	public static final BlockingQueue<Msg<MsgToUIType>> toUI = new ArrayBlockingQueue<>(10);

	public static final BlockingQueue<Msg<MsgToSMType>> toSM = new ArrayBlockingQueue<>(10);
}
