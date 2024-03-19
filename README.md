# coherence-hotkeys

Sample code to retrieve the n most accessed keys of a cache using the touch count attribute of a Coherence local cache backing map.

## Prerequisites

- the cache's backing map must be configured to use a local cache (local-scheme for the backing-map-scheme)
- you need an invocation service to run the provided invocable (com.tangosol.net.Invocable)
- your cache keys must be serializable and implement Comparable

## What is provided in this repository

- ```com.mycompany.coherence.hotkeys.invocable.HotKeyInvocable```: an invocable repsonsible for fetching the most accessed keys from each Coherence storage members' backing map
- ```com.mycompany.coherence.hotkeys.invocable.HotKeyData```: holder of a collected hot key with its touch count value
- ```com.mycompany.coherence.hotkeys.util.SortedCollectionWithCapacity```: a sorted set to store ordered top n collected HotKeyData
- ```com.mycompany.coherence.hotkeys.HotKeys```: a sample application that invokes the HotKeyInvocable
- ```cache-config.xml```: a sample cache config

## Running the example

```
usage: com.telus.coherence.hotkeys.HotKeys
 -c <arg>   optional name of cache; defaults to default_cache
 -d <arg>   optional name of DistributedCache service; defaults to
            DistributedCache
 -help      print command line usage
 -i <arg>   optional name of InvocationService; defaults to
            InvocationService
 -n <arg>   optional number of maximum hot keys to fetch; defaults to 100
 -v         optional verbose mode; defaults to false
```
Example:
<p>
```java -Dtangosol.coherence.cacheconfig=cache-config.xml com.telus.coherence.hotkeys.HotKeys -c PricePlan -d PricePlanCacheService -i InvocationService-TouchCount -n 5 -v true```
