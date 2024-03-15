package com.mycompany.coherence.hotkeys.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import com.mycompany.coherence.hotkeys.invocable.HotKeyData;

/**
 * Simple sorted Set with a max capacity. Once the max capacity is reached, new
 * entries added will force the lowest comparable to be removed from the Set.
 * 
 * @param K The type of the key, should implement Comparable
 */
public class SortedCollectionWithCapacity<K extends Comparable<K>> implements Serializable {

	private static final long serialVersionUID = 1L;

	private final TreeSet<K> sortedSet;
	private final int maxCapacity;
	private long created;
	private long lastTouch;

	/**
	 * SortedCollectionWithCapacity sole constructor.
	 * 
	 * @param maxCapacity
	 */
	public SortedCollectionWithCapacity(int maxCapacity) {
		this.maxCapacity = maxCapacity;
		this.sortedSet = new TreeSet<>();
	}

	/**
	 * Adds an element to the Set.
	 * 
	 * @param element
	 */
	public void add(K element) {
		if (sortedSet.size() == 0) {
			resetTime();
		}
		if (sortedSet.size() < maxCapacity) {
			sortedSet.add(element);
		} else {
			K smallestElement = sortedSet.first();
			if (element.compareTo(smallestElement) > 0) {
				sortedSet.remove(smallestElement);
				sortedSet.add(element);
			}
		}
		touch();
	}

	/**
	 * Returns the underlying TreeSet<K>.
	 */
	public TreeSet<K> getSortedSet() {
		return sortedSet;
	}

	/**
	 * Merges the underlying TreeSet<K> with another one.
	 * 
	 * @param SortedCollectionWithCapacity<K>
	 */
	public void merge(SortedCollectionWithCapacity<K> setToMerger) {
		synchronized (this) {
			for (K item : setToMerger.getSortedSet()) {
				this.add(item);
			}
		}
	}

	/**
	 * Returns the Set content, ordered form high to low.
	 * 
	 * @return
	 */
	public List<K> getReversedOrder() {
		List<K> sortedList = new ArrayList<>(sortedSet);
		Collections.reverse(sortedList);
		return sortedList;
	}

	/**
	 * Resets the baseline for last modified time.
	 */
	private void resetTime() {
		this.created = System.currentTimeMillis();
	}
	
	/**
	 * Updates time modified time.
	 */
	private void touch() {
		this.lastTouch = System.currentTimeMillis();
	}

	/**
	 * Returns the time elapsed between first entry was added and last one added
	 * 
	 * @return
	 */
	public long getElapsed() {
		return this.lastTouch - this.created;
	}

	/**
	 * For test purposes
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		SortedCollectionWithCapacity<HotKeyData<Integer>> sortedCollection = new SortedCollectionWithCapacity<HotKeyData<Integer>>(
				10);

		sortedCollection.add(new HotKeyData<Integer>(new Integer(0), 0));
		sortedCollection.add(new HotKeyData<Integer>(new Integer(2), 1));
		sortedCollection.add(new HotKeyData<Integer>(new Integer(3), 3));
		sortedCollection.add(new HotKeyData<Integer>(new Integer(4), 3));
		sortedCollection.add(new HotKeyData<Integer>(new Integer(5), 4));
		sortedCollection.add(new HotKeyData<Integer>(new Integer(6), 10));

		System.out.println("Sorted Collection with Maximum Capacity (min to max): " + sortedCollection.getSortedSet());
		System.out.println(
				"Sorted Collection with Maximum Capacity (max to min): " + sortedCollection.getReversedOrder());
	}

	/**
	 * Returns the sorted set content and the max capacity value.
	 * 
	 * @return String
	 */
	public String toString() {
		return "SortedCollectionWithCapacity [sortedSet=" + this.getReversedOrder() + ", maxCapacity="
				+ this.maxCapacity + "]";
	}

}
