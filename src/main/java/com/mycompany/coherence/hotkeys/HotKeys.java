package com.mycompany.coherence.hotkeys;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.mycompany.coherence.hotkeys.invocable.HotKeyData;
import com.mycompany.coherence.hotkeys.invocable.HotKeyInvocable;
import com.mycompany.coherence.hotkeys.invocable.HotKeyInvocationObserver;
import com.mycompany.coherence.hotkeys.util.SortedCollectionWithCapacity;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.DistributedCacheService;
import com.tangosol.net.Invocable;
import com.tangosol.net.InvocationObserver;
import com.tangosol.net.InvocationService;
import com.tangosol.net.Member;
import com.tangosol.net.NamedCache;

/**
 * This will trigger the invocation of the HotKeyInvocable. This application gas
 * arguments, see command line arguments.
 */
public class HotKeys {

	protected static final int DEFAULT_TOP_N = 100;
	protected static final String DEFAULT_CACHE_SERVICE_NAME = "DistributedCache";
	protected static final String DEFAULT_INVOCATION_SERVICE_NAME = "InvocationService";
	protected static final boolean DEFAULT_VERBOSITY = false;
	protected static final String DEFAULT_CACHE_NAME = "default_cache";

	private static final int WARMUP_MAX = 1000000;
	private static final int WARMUP_MIN = 0;
	private static final int WARMUP_MAX_GETS = 1000;

	private static Options cliOptions;

	private String cacheName;
	private String distributedCacheServicename;
	private String invocationServiceName;
	private int topN;
	private Map<Member, SortedCollectionWithCapacity<HotKeyData<Integer>>> invocationResults = new HashMap<Member, SortedCollectionWithCapacity<HotKeyData<Integer>>>();

	static {
		cliOptions = new Options();
		cliOptions.addOption(new Option("help", "print command line usage"));
		cliOptions.addOption("n", true,
				String.format("optional number of maximum hot keys to fetch; defaults to %d", DEFAULT_TOP_N));
		cliOptions.addOption("c", true, String.format("optional name of cache; defaults to %s", DEFAULT_CACHE_NAME));
		cliOptions.addOption("d", true,
				String.format("optional name of DistributedCache service; defaults to %s", DEFAULT_CACHE_SERVICE_NAME));
		cliOptions.addOption("i", true,
				String.format("optional name of InvocationService; defaults to %s", DEFAULT_INVOCATION_SERVICE_NAME));
	}

	public static void main(String[] args) {

		CommandLine commandLine = null;
		try {
			commandLine = new BasicParser().parse(cliOptions, args);
		} catch (ParseException ex) {
			System.err.println(String.format("Command line parsing failed with message %s", ex.getMessage()));
			printCommandLineUsage();
			return;
		}

		if (commandLine.hasOption("help")) {
			printCommandLineUsage();
			return;
		}

		String cacheServiceName = commandLine.getOptionValue("d", DEFAULT_CACHE_SERVICE_NAME);
		String cacheName = commandLine.getOptionValue("c", DEFAULT_CACHE_NAME);
		String invocationServiceName = commandLine.getOptionValue("i", DEFAULT_INVOCATION_SERVICE_NAME);
		int topN = Integer.parseInt(commandLine.getOptionValue("n", String.valueOf(DEFAULT_TOP_N)));

		printOptions(commandLine);

		warmup(cacheName);

		HotKeys hotKeys = new HotKeys(cacheName, cacheServiceName, invocationServiceName, topN);
		hotKeys.fetch();
	}

	/**
	 * Warms up the cache with sample data.
	 * 
	 * @param cacheName
	 */
	private static void warmup(String cacheName) {

		NamedCache cache = CacheFactory.getCache(cacheName);

		cache.clear();

		HashMap<Integer, String> newEntries = new HashMap<Integer, String>();
		for (int i = 0; i < WARMUP_MAX; i++) {
			newEntries.put(new Integer(i), "Price plan #" + i);
		}
		cache.putAll(newEntries);

		// Sample random gets

		for (int i = 0; i < WARMUP_MAX_GETS; i++) {
			Integer key = new Integer(nextRandom(WARMUP_MIN, WARMUP_MAX));
			cache.get(new Integer(key));
		}

		// Gets for the following price plans: #50, #25 and #10

		for (int i = 0; i < 50; i++) {
			cache.get(new Integer(50));
		}
		for (int i = 0; i < 25; i++) {
			cache.get(new Integer(25));
		}
		for (int i = 0; i < 10; i++) {
			cache.get(new Integer(10));
		}

	}

	protected static int nextRandom(int min, int max) {
		return (int) Math.floor(Math.random() * (max - min + 1) + min);
	}

	private static void printOptions(CommandLine commandLine) {
		for (Option option : commandLine.getOptions()) {
			System.out.printf("-%s=%s%n", option.getOpt(), commandLine.getOptionValue(option.getOpt()));
		}
	}

	public HotKeys(String cacheName, String distributedCacheServicename, String invocationServiceName, int topN) {
		super();
		this.cacheName = cacheName;
		this.distributedCacheServicename = distributedCacheServicename;
		this.invocationServiceName = invocationServiceName;
		this.topN = topN;
	}

	/**
	 * Orchestrate collection of top n keys of partitioned cache contents as
	 * configured by constructor parameters.
	 */
	private void fetch() {

		if (getStorageMembers().size() == 0) {
			throw new IllegalStateException(
					"There must be storage members in the cluster for fetch top n keys to run.");
		}

		showStorageMembers(getStorageMembers());
		
		long startTime = System.currentTimeMillis();
		Map<Member, Invocable> tasks = createTasks();
		final CountDownLatch countDownLatch = new CountDownLatch(tasks.size());
		InvocationObserver observer = new HotKeyInvocationObserver<Integer>(countDownLatch, startTime, invocationResults);

		for (Map.Entry<Member, Invocable> entry : tasks.entrySet()) {
			Member member = entry.getKey();
			Invocable invocable = entry.getValue();
			getInvocationService().execute(invocable, Collections.singleton(member), observer);
		}

		try {
			countDownLatch.await();
			reportResults(System.currentTimeMillis() - startTime);
		} catch (InterruptedException ex) {
			CacheFactory.log("Interrupted while awaiting invocation completion.");
			CacheFactory.log(ex);
		}
	}

	private InvocationService getInvocationService() {
		// TODO Auto-generated method stub
		return (InvocationService) CacheFactory.getService(invocationServiceName);
	}

	private void showStorageMembers(Set<Member> storageMembers) {
		for (Member member : storageMembers) {
			System.out.println(member);
		}

	}

	protected Set<Member> getStorageMembers() {
		return getDistributedCacheService().getOwnershipEnabledMembers();
	}

	protected DistributedCacheService getDistributedCacheService() {
		return (DistributedCacheService) CacheFactory.getService(getCacheServiceName());
	}

	protected String getCacheServiceName() {
		return distributedCacheServicename;
	}

	private static void printCommandLineUsage() {
		new HelpFormatter().printHelp(HotKeys.class.getSimpleName(), cliOptions);
	}

	protected Map<Member, Invocable> createTasks() {
		Set<Member> storageMembers = getStorageMembers();
		Map<Member, Invocable> fetchHotKeysByMember = new HashMap<>(storageMembers.size());
		for (Member member : storageMembers) {
			HotKeyInvocable<Integer> task = new HotKeyInvocable<Integer>(getCacheName(), getCacheServiceName(),
					getTopN());
			fetchHotKeysByMember.put(member, task);
		}
		return fetchHotKeysByMember;
	}

	private String getCacheName() {
		return this.cacheName;
	}

	private int getTopN() {
		return this.topN;
	}

	private void reportResults(long executionTime) {

		SortedCollectionWithCapacity<HotKeyData<Integer>> mergedResult = new SortedCollectionWithCapacity<HotKeyData<Integer>>(
				getTopN());

		for (Map.Entry<Member, SortedCollectionWithCapacity<HotKeyData<Integer>>> entry : invocationResults
				.entrySet()) {
			Member member = entry.getKey();
			if (entry.getValue() != null) {
				SortedCollectionWithCapacity<HotKeyData<Integer>> result = entry.getValue();
				System.out.println(
						"Execution on member " + getShortMemberInfo(member) + " took " + result.getElapsed() + " ms");
				mergedResult.merge(result);
			} else {
				System.out.println("Error: missing result for member " + member);
			}
		}

		System.out.println("Total gathering of top " + getTopN() + " hot keys took " + executionTime + " ms");
		System.out.println(mergedResult);
	}

	private String getShortMemberInfo(Member member) {
		// TODO Auto-generated method stub
		return "Member(Id=" + member.getId() + ", Address=" + member.getAddress().getHostAddress() + ":"
				+ member.getPort() + ", Role=" + member.getRoleName() + ")";
	}

}
