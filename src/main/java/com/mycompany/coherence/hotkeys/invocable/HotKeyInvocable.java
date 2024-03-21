package com.mycompany.coherence.hotkeys.invocable;

import java.io.IOException;
import java.io.Serializable;
import java.util.Set;

import org.apache.log4j.Logger;

import com.mycompany.coherence.hotkeys.util.SortedCollectionWithCapacity;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.net.AbstractInvocable;
import com.tangosol.net.BackingMapContext;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.DistributedCacheService;
import com.tangosol.net.Member;
import com.tangosol.net.cache.LocalCache;
import com.tangosol.util.Converter;

/**
 * Invocable that will handle fetching touch count on each entry of a member on
 * a specific cache's backing map.
 * 
 * @param K The type of the key of cache entries that invocable is running
 *          against, should implement Comparable
 * @see com.tangosol.net.Invocable
 */
public class HotKeyInvocable<K extends Comparable<K>> extends AbstractInvocable
	implements PortableObject, Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLogger("Coherence");

    private String cacheName;
    private String cacheServiceName;
    private int topN;

    /**
     * HotKeyInvocable sole constructor.
     * 
     * @param cacheName        cache name
     * @param cacheServiceName cache service name
     * @param topN
     * @param verbose
     */
    public HotKeyInvocable(String cacheName, String cacheServiceName, int topN) {
	this.cacheName = cacheName;
	this.cacheServiceName = cacheServiceName;
	this.topN = topN;
    }

    /**
     * Fetches the touch count of each entry of the backing map.
     */
    public void run() {

	SortedCollectionWithCapacity<HotKeyData<K>> results = new SortedCollectionWithCapacity<HotKeyData<K>>(
		this.topN);

	DistributedCacheService cacheService = (DistributedCacheService) CacheFactory.getService(cacheServiceName);
	Member localMember = cacheService.getCluster().getLocalMember();
	int localMemberId = localMember.getId();
	Set backingMapEntries = cacheService.getBackingMapManager().getContext().getBackingMap(cacheName).entrySet();

	BackingMapContext backingMapContext = cacheService.getBackingMapManager().getContext()
		.getBackingMapContext(cacheName);
	Converter converter = backingMapContext.getManagerContext().getKeyFromInternalConverter();

	if (logger.isDebugEnabled()) {
	    logger.debug("There are " + backingMapEntries.size() + " entries in the " + cacheName
		    + " cache on local member id " + localMemberId);
	}

	for (Object entry : backingMapEntries) {
	    LocalCache.Entry localCacheEntry = (LocalCache.Entry) entry;
	    K key = (K) converter.convert(localCacheEntry.getKey());
	    results.add(new HotKeyData<K>(key, localCacheEntry.getTouchCount()));
	    if (logger.isDebugEnabled()) {
		logger.debug("key=" + converter.convert(localCacheEntry.getKey()) + ", touchCount="
			+ localCacheEntry.getTouchCount());
	    }
	}

	this.setResult(results);
    }

    /**
     * POF readExternal
     */
    public void readExternal(PofReader pofReader) throws IOException {
	cacheName = pofReader.readString(0);
	cacheServiceName = pofReader.readString(1);
	topN = pofReader.readInt(2);
    }

    /**
     * POF writeExternal
     */
    public void writeExternal(PofWriter pofWriter) throws IOException {
	pofWriter.writeString(0, cacheName);
	pofWriter.writeString(1, cacheServiceName);
	pofWriter.writeInt(2, topN);
    }

}
