package util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FutureList<V> implements Future<V> {
	private Future<V>[] arr;

	public FutureList(Future<V>[] arr) {
		this.arr = arr;
	}

	/**
	 * Attempts to cancel execution of all tasks in this list.
	 *
	 * @param mayInterruptIfRunning {@code true} if the thread executing its task,
	 *                              shoud be interrupted.
	 * @return {@code false} if any of the futures in this list couldn't be
	 *         cancelled, typically because it had already completed; {@code true}
	 *         otherwise
	 */
	public boolean cancel(boolean mayInterruptIfRunning) {
		boolean res = false;
		for (int i = 0; i < arr.length; i++) {
			res = this.arr[i].cancel(mayInterruptIfRunning) && res;
		}
		return res;
	}

	/**
	 * Returns {@code true} if all tasks in this list were cancelled before they
	 * were completed normally.
	 */
	public boolean isCancelled() {
		for (Future<V> f : arr) {
			if (!f.isCancelled())
				return false;
		}
		return true;
	}

	/**
	 * Returns {@code true} if all tasks in this list are done; {@code false}
	 * otherwise.
	 */
	public boolean isDone() {
		for (Future<V> f : arr) {
			if (!f.isDone())
				return false;
		}
		return true;
	}

	/**
	 * Waits for all tasks to be done and retrieves the result from the last task.
	 * To get a list of all results from all tasks, use {@code getAll} instead.
	 */
	public V get() throws InterruptedException, ExecutionException {
		V res = null;
		for (Future<V> f : arr) {
			res = f.get();
		}
		return res;
	}

	/**
	 * Waits for all tasks to be done and retrieves all of their results as an
	 * array.
	 */
	@SuppressWarnings("unchecked")
	public V[] getAll() throws InterruptedException, ExecutionException {
		V[] res = (V[]) new Object[arr.length];
		for (int i = 0; i < arr.length; i++) {
			res[i] = arr[i].get();
		}
		return res;
	}

	/**
	 * Waits for at most the given time for all tasks to be done and retrieves the
	 * result from the last task. To get a list of all results from all tasks, use
	 * {@code getAll} instead.
	 *
	 * @param timeout the maximum time to wait
	 * @param unit    the time unit of the timeout argument
	 */
	public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		V res = null;
		timeout = unit.toNanos(timeout);
		long start = System.nanoTime();
		for (Future<V> f : arr) {
			res = f.get(timeout + start - System.nanoTime(), TimeUnit.NANOSECONDS);
		}
		return res;
	}

	/**
	 * Waits for at most the given time for all tasks to be done and retrieves the
	 * result from the last task. To get a list of all results from all tasks, use
	 * {@code getAll} instead.
	 *
	 * @param timeout the maximum time to wait
	 * @param unit    the time unit of the timeout argument
	 */
	@SuppressWarnings("unchecked")
	public V[] getAll(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		V[] res = (V[]) new Object[arr.length];
		timeout = unit.toNanos(timeout);
		long start = System.nanoTime();
		for (int i = 0; i < arr.length; i++) {
			res[i] = arr[i].get(timeout + start - System.nanoTime(), TimeUnit.NANOSECONDS);
		}
		return res;
	}
}
