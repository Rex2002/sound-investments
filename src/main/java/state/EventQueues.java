package state;

import dataRepo.Sonifiable;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class EventQueues {
	public static final BlockingQueue<List<Sonifiable>> toUI = new ArrayBlockingQueue<>(10);
	public static final BlockingQueue<Integer> toPlayback = new ArrayBlockingQueue<>(10);
}