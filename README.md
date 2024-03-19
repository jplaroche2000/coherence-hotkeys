# coherence-hotkeys

Sample code to retrieve the n most accessed keys of a cache using the touch count attribute of a Coherence local cache backing map.

## Prerequisites

- the cache's backing map must be configured to use a local cache (local-scheme for the backing-map-scheme)
- you need an invocation service to run an invocable (com.tangosol.net.Invocable)
- your cache keys must be serializable ()

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
