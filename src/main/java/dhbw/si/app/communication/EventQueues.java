package dhbw.si.app.communication;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import dhbw.si.audio.playback.PlayControlEvent;

/**
 * @author V. Richter
 * @reviewer B. Frahm
 */
public class EventQueues {
	public static final BlockingQueue<Msg<MsgToUIType>> toUI = new ArrayBlockingQueue<>(10);

	public static final BlockingQueue<Msg<MsgToSMType>> toSM = new ArrayBlockingQueue<>(10);

	public static final BlockingQueue<PlayControlEvent> toPlayback = new ArrayBlockingQueue<>(10);
}
