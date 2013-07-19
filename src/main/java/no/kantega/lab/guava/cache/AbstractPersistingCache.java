package no.kantega.lab.guava.cache;

import com.google.common.cache.*;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

public abstract class AbstractPersistingCache<K, V> implements Cache<K, V> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPersistingCache.class);

    private final LoadingCache<K, V> underlyingCache;

    protected AbstractPersistingCache(CacheBuilder<Object, Object> cacheBuilder) {
        this.underlyingCache = makeCache(cacheBuilder);
    }

    private LoadingCache<K, V> makeCache(CacheBuilder<Object, Object> cacheBuilder) {
        return cacheBuilder.removalListener(new PersistingRemovalListener()).build(new PersistedStateCacheLoader());
    }

    private class PersistingRemovalListener implements RemovalListener<K, V> {
        @Override
        public void onRemoval(RemovalNotification<K, V> notification) {
            if (isPersistenceRelevant(notification.getCause())) {
                try {
                    persistValue(notification.getKey(), notification.getValue());
                } catch (IOException e) {
                    LOGGER.warn(String.format("Could not persist value %s to key %s",
                            notification.getKey(), notification.getValue()), e);
                }
            }
        }
    }

    private class PersistedStateCacheLoader extends CacheLoader<K, V> {
        @Override
        public V load(K key) throws Exception {
            V value = null;
            try {
                value = findPersisted(key);
                if(value != null) deletePersistedIfExistent(key);
            } catch (Exception e) {
                LOGGER.warn(String.format("Could not load persisted value to key %s", key), e);
            }
            if (value != null) {
                return value;
            } else {
                throw new NotPersistedException();
            }
        }
    }

    private class PersistedStateValueLoader extends PersistedStateCacheLoader implements Callable<V> {

        private final K key;
        private final Callable<? extends V> valueLoader;

        private PersistedStateValueLoader(K key, Callable<? extends V> valueLoader) {
            this.key = key;
            this.valueLoader = valueLoader;
        }

        @Override
        public V call() throws Exception {
            V value = load(key);
            if (value != null) return value;
            return valueLoader.call();
        }
    }

    protected boolean isPersistenceRelevant(RemovalCause removalCause) {
        return removalCause != RemovalCause.COLLECTED;
    }

    protected Cache<K, V> getUnderlyingCache() {
        return underlyingCache;
    }

    protected abstract V findPersisted(K key) throws IOException;

    protected abstract void persistValue(K key, V value) throws IOException;

    protected abstract List<String> directoryFor(K key);

    protected abstract void persist(K key, V value, OutputStream outputStream) throws IOException;

    protected abstract V readPersisted(K key, InputStream inputStream) throws IOException;

    protected abstract boolean isPersist(K key);

    protected abstract void deletePersistedIfExistent(K key);

    protected abstract void deleteAllPersisted();

    protected abstract int sizeOfPersisted();

    @Override
    @SuppressWarnings("unchecked")
    public V getIfPresent(Object key) {
        try {
            K castKey = (K) key;
            return underlyingCache.get(castKey);
        } catch (ClassCastException e) {
            LOGGER.info(String.format("Could not cast key %s to desired type", key), e);
        } catch (ExecutionException e) {
            if (e.getCause().getClass() == NotPersistedException.class) {
                LOGGER.info(String.format("Key %s is not persisted", key), e);
            } else {
                LOGGER.warn(String.format("Persisted value to key %s could not be retrieved", key), e);
                throw new RuntimeException("Error while loading persisted value", e);
            }
        }
        return null;
    }

    @Override
    public V get(K key, Callable<? extends V> valueLoader) throws ExecutionException {
        return underlyingCache.get(key, new PersistedStateValueLoader(key, valueLoader));
    }

    @Override
    @SuppressWarnings("unchecked")
    public ImmutableMap<K, V> getAllPresent(Iterable<?> keys) {
        Map<K, V> allPresent = new HashMap<K, V>();
        for (Object key : keys) {
            V value = getIfPresent(key);
            if (value != null) allPresent.put((K) key, value);
        }
        return ImmutableMap.copyOf(allPresent);
    }

    @Override
    public void put(K key, V value) {
        underlyingCache.put(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        underlyingCache.putAll(m);
    }

    @Override
    public void invalidate(Object key) {
        underlyingCache.invalidate(key);
        invalidatePersisted(key);
    }

    @Override
    public void invalidateAll(Iterable<?> keys) {
        underlyingCache.invalidateAll(keys);
        for (Object key : keys) {
            invalidatePersisted(key);
        }
    }

    @SuppressWarnings("unchecked")
    private void invalidatePersisted(Object key) {
        try {
            K castKey = (K) key;
            deletePersistedIfExistent(castKey);
        } catch (ClassCastException e) {
            LOGGER.info(String.format("Could not cast key %s to desired type", key), e);
        }
    }

    @Override
    public void invalidateAll() {
        underlyingCache.invalidateAll();
        deleteAllPersisted();
    }

    @Override
    public long size() {
        return underlyingCache.size() + sizeOfPersisted();
    }

    @Override
    public CacheStats stats() {
        return underlyingCache.stats();
    }

    @Override
    public ConcurrentMap<K, V> asMap() {
        return underlyingCache.asMap();
    }

    @Override
    public void cleanUp() {
        underlyingCache.cleanUp();
    }
}
