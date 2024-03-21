# coherence-hotkeys

Sample code to retrieve the n most accessed keys of a cache using the touch count attribute of a Coherence local cache backing map.

## Prerequisites

- the cache's backing map must be configured to use a local cache (local-scheme for the backing-map-scheme)
- you need an invocation service to run the provided invocable (com.tangosol.net.Invocable)
- your cache keys must be serializable and implement Comparable

## What is provided in this repository

- ```com.mycompany.coherence.hotkeys.invocable.HotKeyInvocable```: an invocable responsible for fetching the most accessed keys from each Coherence storage members' backing map
- ```com.mycompany.coherence.hotkeys.invocable.HotKeyData```: holder of a collected hot key with its touch count value
- ```com.mycompany.coherence.hotkeys.invocable.HotKeyInvocationObserver```: invocation observer to collect results from each member
- ```com.mycompany.coherence.hotkeys.util.SortedCollectionWithCapacity```: a sorted set to store ordered top n collected HotKeyData
- ```com.mycompany.coherence.hotkeys.HotKeysExample```: a sample application that invokes the HotKeyInvocable
- ```cache-config.xml```: a sample cache config

## Running the example

```
usage: com.mycompany.coherence.hotkeys.HotKeysExample
 -c <arg>   optional name of cache; defaults to default_cache
 -d <arg>   optional name of DistributedCache service; defaults to
            DistributedCache
 -help      print command line usage
 -i <arg>   optional name of InvocationService; defaults to
            InvocationService
 -n <arg>   optional number of maximum hot keys to fetch; defaults to 100
```
Example:
<br>
```java -Dcoherence.cacheconfig=cache-config.xml -Dcoherence.log.level=1 com.mycompany.coherence.hotkeys.HotKeysExample -c PricePlan -d PricePlanCacheService -i InvocationService-TouchCount -n 5 ```

Output:
<br>
```
-c=PricePlan
-d=PricePlanCacheService
-i=InvocationService-TouchCount
-n=5

Oracle Coherence Version 12.2.1.4.20 Build 105485
 Grid Edition: Development mode
Copyright (c) 2000, 2023, Oracle and/or its affiliates. All rights reserved.

Member(Id=1, Timestamp=2024-03-19 10:51:58.659, Address=192.168.2.165:57070, MachineId=9749, Location=process:15616, Role=MycompanyCoherenceHotkeysHotKeys)
Execution on member Member(Id1, Address=192.168.2.165:57070, Role=MycompanyCoherenceHotkeysHotKeys) took 666 ms
Total gathering of top 5 hot keys took 686 ms
invocation completed
SortedCollectionWithCapacity [sortedSet=[[key=50, touchCount=50], [key=25, touchCount=25], [key=10, touchCount=10], [key=998287, touchCount=1], [key=997442, touchCount=1]], maxCapacity=5]
```
