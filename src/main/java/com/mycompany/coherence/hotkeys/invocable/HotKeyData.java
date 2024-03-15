package com.mycompany.coherence.hotkeys.invocable;

import java.util.Objects;

/**
 * Invocable's holder for touch count and key (K type) of an entry.
 * 
 * @param K The type of the key, should implement Comparable
 */
public class HotKeyData<K extends Comparable<K>> implements java.io.Serializable, Comparable<HotKeyData<K>> {

	private static final long serialVersionUID = 1L;

	private K key;
	private Integer touchCount = new Integer(0);
	
	/**
	 * HotKeyData sole constructor.
	 * 
	 * @param key key of the entry
	 * @param count touch count
	 */
	public HotKeyData(K key, int count) {
		this.key = key;
		this.touchCount = count;
	}
	
	public K getKey() {
		return key;
	}

	public void setKey(K key) {
		this.key = key;
	}

	public int getTouchCount() {
		return touchCount;
	}

	public void setTouchCount(int touchCount) {
		this.touchCount = touchCount;
	}

	/**
	 * 
	 */
	public int compareTo(HotKeyData<K> o) {
		int result = this.touchCount.compareTo(o.touchCount);
		if (result == 0) {
			// return order based on the key
			return this.key.compareTo(o.getKey());
		} else {
			return result;
		}
	}
	
	public int hashCode() {
		return Objects.hash(key);
	}

	@SuppressWarnings("unchecked")
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HotKeyData<K> other = (HotKeyData<K>) obj;
		return Objects.equals(key, other.key);
	}

   /**
    * Returns the key and the touch count value.
	* 
	* @return String
	*/
	public String toString() {
		return "[key="+key+", touchCount="+touchCount+"]";
	}
}
