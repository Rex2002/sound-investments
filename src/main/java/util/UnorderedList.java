package util;

import java.util.*;
import java.util.function.Predicate;

// Basically ArrayList, but much faster when having to remove a lot of elements.
// The speedup is possible, because this List implementation doesn't keep elements in the same order.
// For Benchmarking see: https://github.com/ArtInLines/speedy-java
public class UnorderedList<E> implements List<E>, RandomAccess {
	private static final int DEFAULT_CAPACITY = 16;

	private int cap = DEFAULT_CAPACITY;
	private int len = 0;
	private Object[] arr;
	private int toRemoveAmount = 0;
	private int[] toRemove = new int[DEFAULT_CAPACITY];

	public UnorderedList(int capacity) {
		if (capacity < 0)
			throw new IllegalArgumentException("Illegal Capacity: " +
					capacity);
		this.arr = new Object[capacity];
		this.cap = capacity;
	}

	public UnorderedList() {
		this.arr = new Object[DEFAULT_CAPACITY];
	}

	public UnorderedList(Collection<? extends E> c) {
		this.cap = c.size();
		this.len = cap;
		this.arr = c.toArray();
	}

	public int capacity() {
		return cap;
	}

	public int size() {
		return len;
	}

	public boolean isEmpty() {
		return len == 0;
	}

	public Object[] toArray() {
		return Arrays.copyOf(arr, len);
	}

	@SuppressWarnings("unchecked")
	public <T> T[] getArray() {
		return (T[]) arr;
	}

	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] a) {
		if (a.length < len)
			// Make a new array of a's runtime type, but my contents:
			return (T[]) Arrays.copyOf(arr, len, a.getClass());
		System.arraycopy(arr, 0, a, 0, len);
		if (a.length > len)
			a[len] = null;
		return a;
	}

	public int indexOfRange(Object o, int low, int high) {
		if (o == null) {
			for (int i = low; i < high; i++) {
				if (arr[i] == null)
					return i;
			}
		} else {
			for (int i = low; i < high; i++) {
				if (o.equals(arr[i]))
					return i;
			}
		}
		return -1;
	}

	public int indexOf(Object o) {
		return indexOfRange(o, 0, len);
	}

	public int lastIndexOfRange(Object o, int low, int high) {
		if (o == null) {
			for (int i = high - 1; i >= low; i--) {
				if (arr[i] == null)
					return i;
			}
		} else {
			for (int i = high - 1; i >= low; i--) {
				if (o.equals(arr[i]))
					return i;
			}
		}
		return -1;
	}

	public int lastIndexOf(Object o) {
		return lastIndexOfRange(o, 0, len);
	}

	@SuppressWarnings("unchecked")
	public E get(int index) {
		Objects.checkIndex(index, len);
		return (E) arr[index];
	}

	@SuppressWarnings("unchecked")
	public E set(int index, E element) {
		Objects.checkIndex(index, len);
		E old = (E) arr[index];
		arr[index] = element;
		return old;
	}

	public boolean ensureCapacity(int minCapacity) {
		if (cap >= minCapacity)
			return false;
		grow(minCapacity);
		return true;
	}

	public void grow(int minCapacity) {
		int newCap = Math.max(cap + (cap >> 1), minCapacity);
		arr = Arrays.copyOf(arr, newCap);
	}

	public void grow() {
		cap = Math.max(DEFAULT_CAPACITY, cap + (cap >> 1));
		arr = Arrays.copyOf(arr, cap);
	}

	public boolean shrink(int maxCapacity) {
		if (len >= maxCapacity)
			return false;
		arr = Arrays.copyOf(arr, maxCapacity);
		cap = maxCapacity;
		return true;
	}

	public boolean shrink() {
		int maxCapacity = Math.max(len, cap >> 1);
		arr = Arrays.copyOf(arr, maxCapacity);
		cap = maxCapacity;
		return true;
	}

	public void add(int index, E element) {
		if (len == cap)
			grow();
		if (index == len) {
			arr[len] = element;
		} else {
			arr[len] = arr[index];
			arr[index] = element;
		}
		len++;
	}

	public boolean add(E element) {
		add(len, element);
		return true;
	}

	public void quickRemove(int index) {
		len--;
		arr[index] = arr[len];
		arr[len] = null;
	}

	public void removeLater(int index) {
		// Assumes that `index` isn't already in the `toRemove` array
		if (toRemove.length == toRemoveAmount) {
			toRemove = Arrays.copyOf(toRemove, toRemove.length + (toRemove.length >> 1));
		}
		toRemove[toRemoveAmount] = index;
		toRemoveAmount++;
	}

	public void applyRemoves() {
		for (int i = 0; i < toRemoveAmount; i++) {
			// Find smallest index in sublist
			int min = i;
			for (int j = i + 1; j < toRemoveAmount; j++) {
				if (toRemove[j] < toRemove[min])
					min = j;
			}
			// Remove element at that index
			int idxToRemove = toRemove[min] - i; // offset by i, because we already removed i elements before
			if (idxToRemove >= 0 && idxToRemove < len)
				quickRemove(idxToRemove);
		}
		// Shrink toRemove array if it's more than double toRemoveAmount
		if (toRemoveAmount < toRemove.length >> 1) {
			toRemove = new int[toRemoveAmount];
		}
		toRemoveAmount = 0;
	}

	@SuppressWarnings("unchecked")
	public E remove(int index) {
		for (int i = 0; i < toRemoveAmount; i++)
			if (index < toRemove[i])
				index--;
		applyRemoves();
		Objects.checkIndex(index, len);
		E res = (E) arr[index];
		quickRemove(index);
		return res;
	}

	public boolean remove(Object o) {
		int idx = indexOf(o);
		if (idx == -1)
			return false;
		remove(idx);
		return true;
	}

	public boolean removeAll(Collection<?> c) {
		boolean res = false;
		for (Object x : c) {
			if (remove(x))
				res = true;
		}
		shrink();
		return res;
	}

	@SuppressWarnings("unchecked")
	public boolean removeIf(Predicate<? super E> filter) {
		boolean res = false;
		int i = 0;
		while (i < len) {
			if (filter.test((E) arr[i])) {
				remove(i);
				res = true;
			} else {
				i++;
			}
		}
		shrink();
		return res;
	}

	@SuppressWarnings("unchecked")
	public boolean retainAll(Collection<?> c) {
		boolean res = false;
		int i = 0;
		while (i < len) {
			if (!c.contains((E) arr[i])) {
				remove(i);
				res = true;
			} else {
				i++;
			}
		}
		shrink();
		return res;
	}

	public void clear() {
		for (int i = 0; i < len; i++)
			arr[i] = null;
		len = 0;
		shrink();
	}

	public boolean contains(Object o) {
		return indexOf(o) != -1;
	}

	public boolean containsAll(Collection<?> c) {
		for (Object x : c) {
			if (indexOf(x) == -1)
				return false;
		}
		return true;
	}

	public boolean addAll(int index, Collection<? extends E> c) {
		if (index >= len)
			return addAll(c);
		ensureCapacity(len + c.size());
		int i = index;
		for (Object x : c) {
			arr[len] = arr[i];
			arr[i] = x;
			len++;
			i++;
		}
		return i != index;
	}

	public boolean addAll(Collection<? extends E> c) {
		ensureCapacity(len + c.size());
		for (Object x : c) {
			arr[len] = x;
			len++;
		}
		return c.size() > 0;
	}

	public static void subListRangeCheck(int fromIndex, int toIndex, int size) {
		if (fromIndex < 0)
			throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
		if (toIndex > size)
			throw new IndexOutOfBoundsException("toIndex = " + toIndex);
		if (fromIndex > toIndex)
			throw new IllegalArgumentException("fromIndex(" + fromIndex +
					") > toIndex(" + toIndex + ")");
	}

	public List<E> subList(int fromIndex, int toIndex) {
		// TODO
		subListRangeCheck(fromIndex, toIndex, len);
		return new SubList<>(this, fromIndex, toIndex);
	}

	private static class SubList<E> implements List<E>, RandomAccess {
		private UnorderedList<E> root;
		private int from;
		private int to;

		public SubList(UnorderedList<E> root, int from, int to) {
			this.root = root;
			this.from = from;
			this.to = to;
		}

		public E set(int index, E element) {
			return root.set(from + index, element);
		}

		public boolean retainAll(Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		public boolean contains(Object o) {
			for (int i = from; i < to; i++) {
				if (root.get(i) == o)
					return true;
			}
			return false;
		}

		public void clear() {
			for (int i = from; i < to; i++) {
				root.remove(from);
			}
		}

		public Iterator<E> iterator() {
			throw new UnsupportedOperationException();
		}

		public boolean removeAll(Collection<?> c) {
			boolean changed = false;
			for (int i = from; i < to; i++) {
				if (c.contains(root.get(i))) {
					root.remove(i);
					i--;
					to--;
					changed = true;
				}
			}
			return changed;
		}

		public ListIterator<E> listIterator() {
			throw new UnsupportedOperationException();
		}

		public ListIterator<E> listIterator(int index) {
			throw new UnsupportedOperationException();
		}

		public Object[] toArray() {
			return Arrays.copyOfRange(root.arr, from, to);
		}

		@SuppressWarnings("unchecked")
		public <T> T[] toArray(T[] a) {
			int size = to - from;
			if (a.length < size) {
				return (T[]) Arrays.copyOfRange(
						root.arr, from, to, a.getClass());
			} else {
				System.arraycopy(root.arr, from, a, 0, size);
				if (a.length > size)
					a[size] = null;
				return a;
			}
		}

		public boolean containsAll(Collection<?> c) {
			for (Object x : c) {
				if (!contains(x))
					return false;
			}
			return true;
		}

		public int indexOf(Object o) {
			int index = root.indexOfRange(o, from, to);
			return index >= 0 ? index - from : -1;
		}

		public int lastIndexOf(Object o) {
			int index = root.lastIndexOfRange(o, from, to);
			return index >= 0 ? index - from : -1;
		}

		public E get(int index) {
			return root.get(from + index);
		}

		public boolean isEmpty() {
			return to - from == 0;
		}

		public boolean add(E e) {
			root.add(to, e);
			to++;
			return true;
		}

		public void add(int index, E element) {
			root.add(from + index, element);
			to++;
		}

		public int size() {
			return to - from;
		}

		public E remove(int index) {
			E res = root.get(from + index);
			root.arr[from + index] = root.arr[to];
			root.remove(to);
			to--;
			return res;
		}

		public boolean remove(Object o) {
			int i = indexOf(o);
			if (i < 0)
				return false;
			remove(i);
			return true;
		}

		public boolean addAll(int index, Collection<? extends E> c) {
			boolean res = root.addAll(from + index, c);
			to += c.size();
			return res;
		}

		public boolean addAll(Collection<? extends E> c) {
			return addAll(to - from, c);
		}

		public List<E> subList(int fromIndex, int toIndex) {
			subListRangeCheck(fromIndex, toIndex, size());
			return new SubList<>(root, from + fromIndex, from + toIndex);
		}
	}

	public Iterator<E> iterator() {
		return new Iter();
	}

	public ListIterator<E> listIterator() {
		return new ListIter(0);
	}

	public ListIterator<E> listIterator(int index) {
		return new ListIter(index);
	}

	private class Iter implements Iterator<E> {
		int cur = 0;

		// prevent creating a synthetic constructor
		Iter() {
		}

		@SuppressWarnings("unchecked")
		public E next() {
			if (cur >= UnorderedList.this.len)
				throw new NoSuchElementException();
			return (E) UnorderedList.this.arr[cur++];
		}

		public boolean hasNext() {
			return cur < UnorderedList.this.len;
		}

		public void remove() {
			if (cur == 0)
				throw new NoSuchElementException();
			int l = UnorderedList.this.len;
			UnorderedList.this.arr[cur - 1] = UnorderedList.this.arr[l - 1];
			UnorderedList.this.arr[l - 1] = null;
			if (l != UnorderedList.this.len)
				throw new ConcurrentModificationException();
			UnorderedList.this.len--;
		}
	}

	private class ListIter extends Iter implements ListIterator<E> {
		ListIter(int index) {
			super();
			cur = index;
		}

		public boolean hasPrevious() {
			return cur != 0;
		}

		public int previousIndex() {
			return cur - 1;
		}

		public int nextIndex() {
			return cur;
		}

		@SuppressWarnings("unchecked")
		public E previous() {
			cur--;
			return (E) UnorderedList.this.arr[cur];
		}

		public void set(E e) {
			UnorderedList.this.set(cur - 1, e);
		}

		public void add(E e) {
			UnorderedList.this.add(cur, e);
			cur++;
		}

	}
}