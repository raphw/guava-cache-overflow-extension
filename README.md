A Guava cache extension that allows caches to persist cache entries when they cannot longer be stored in memory.
An implementation that overflows to the file system is provided including a corresponding `CacheBuilder` with similar semantics than the Guava `CacheBuilder`.

For creating a cache that overflows to disk, just proceed as when using the Guava CacheBuilder:

```java
Cache<String, String> stringCache =
  FileSystemCacheBuilder.newBuilder()
    .maximumSize(100L)
    .softValues()
    .build();
```

Note: This cache implementation has slightly different semantics than the `Cache` / `LoadingCache` interface contracts specify:
* Any limits set for this cache do only concern the cache's memory size. Cache entries exceeding this limit will overflow to disk.
* When calling the non-argument `invalidateAll()` method, the RemovalListener is only informed about the expiration of entries that are still stored in memory.
* When the cache is not longer in use, its `invalidateAll()` method should be called if the cache's overflow folder is not cleared by the operating system.
* There is a minimal risk of concurrency issues since cache entries are still accessible when the `RemovalListener` which is responsible for serializing the cache entry writes the entry to disk. This problem does not matter for immutable cache objects, but mutable state might get lost when cache entries are retreived while they are serialized. 

Licensed under the Apache Software License, Version 2.0
