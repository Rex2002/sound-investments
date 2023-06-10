package util;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A FutureList is a list of Futures, that should be awaited together
 * <p>
 * The FutureList implements the Future interface itself and allows awaiting all contained futures together.
 * There are two methods for retrieving the results of the Futures.
 * <p>
 * {@code get} returns the result of the last future of the list.
 * It still blocks until *all* futures in the list are done.
 * <p>
 * {@code getAll} on the other hand returns a list of all results.
 *
 * @ImplNote
 * This class is very lightweight.
 * It uses a simple array of Futures, that can grow arbitrarily large by copying the existing array into a bigger one.
 *
 */
public class FutureList<V> implements Future<V> {
	private Future<V>[] arr;
	private int len = 0;

	/**
	 * Create a new FutureList with the provided capacity
	 *
	 * @param capacity The initial length of the backing array
	 */
	@SuppressWarnings("unchecked")
	public FutureList(int capacity) {
		this.arr = (Future<V>[]) new Future[capacity];
		this.len = 0;
	}

	/**
	 * Create a new FutureList. The provided array will be used as the backing array.
	 *
	 * @param arr The array of Futures to await together
	 */
	public FutureList(Future<V>[] arr) {
		this.arr = arr;
		this.len = arr.length;
	}

	/**
	 * Add another Future to this FutureList.
	 *
	 * @ImplNote
	 * If the backing array is big enough, this is an O(1) operation, otherwise the array will be doubled in size and copied over - making it O(n).
	 *
	 * @param f The next future to add
	 */
	public void add(Future<V> f) {
		if (this.arr.length <= this.len) {
			this.arr = Arrays.copyOf(this.arr, this.arr.length * 2);
		}
		this.arr[this.len] = f;
		this.len++;
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
		for (int i = 0; i < len; i++) {
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
		V[] res = (V[]) new Object[len];
		for (int i = 0; i < len; i++) {
			res[i] = arr[i].get();
		}
		return res;
	}

	/**
	 * Waits for all tasks to be done and adds all of their results to the provided list.
	 */
	public List<V> getAll(List<V> list) throws InterruptedException, ExecutionException {
		for (int i = 0; i < len; i++) {
			list.add(arr[i].get());
		}
		return list;
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
}
