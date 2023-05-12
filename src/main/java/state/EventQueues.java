package state;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import dataRepo.Sonifiable;

public class EventQueues {
	public static final BlockingQueue<List<Sonifiable>> toUI = new ArrayBlockingQueue<>(10);
}
