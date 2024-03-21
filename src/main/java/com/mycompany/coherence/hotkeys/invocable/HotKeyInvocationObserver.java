package com.mycompany.coherence.hotkeys.invocable;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

import com.mycompany.coherence.hotkeys.util.SortedCollectionWithCapacity;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.InvocationObserver;
import com.tangosol.net.Member;

/**
 * InvocationObserver for observing the progress of asynchronous Invocable
 * execution.
 */
public class HotKeyInvocationObserver<K extends Comparable<K>> implements InvocationObserver {

    private CountDownLatch countDownLatch;
    private long startTime;
    private Map<Member, SortedCollectionWithCapacity<HotKeyData<K>>> invocationResults;

    public HotKeyInvocationObserver(CountDownLatch countDownLatch, long startTime,
	    Map<Member, SortedCollectionWithCapacity<HotKeyData<K>>> invocationResults) {
	this.countDownLatch = countDownLatch;
	this.startTime = startTime;
	this.invocationResults = invocationResults;
    }

    public void memberCompleted(Member member, Object result) {
	invocationResults.put(member, (SortedCollectionWithCapacity<HotKeyData<K>>) result);
	countDownLatch.countDown();
	CacheFactory.log(String.format("Task completed on %s.", member));
    }

    public void memberFailed(Member member, Throwable throwable) {
	invocationResults.put(member, null);
	countDownLatch.countDown();
	CacheFactory.log(String.format("Task failed on %s.", member));
	CacheFactory.log(throwable);
    }

    public void memberLeft(Member member) {
	invocationResults.put(member, null);
	countDownLatch.countDown();
	CacheFactory.log(String.format("Member left before task completed: %s", member));
    }

    public void invocationCompleted() {
	CacheFactory.log("invocation completed");
    }

    public CountDownLatch getCountDownLatch() {
	return countDownLatch;
    }

    public long getStartTime() {
	return startTime;
    }

    public Map<Member, SortedCollectionWithCapacity<HotKeyData<K>>> getInvocationResults() {
	return invocationResults;
    }

}
