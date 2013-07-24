package com.blogspot.mydailyjava;

import com.google.common.cache.Cache;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.Callable;

import static org.testng.Assert.*;

public class FileSystemPersistingCacheTest {

    private Cache<String, String> fileSystemPersistingCache;

    @BeforeMethod
    public void setUp() throws Exception {
        fileSystemPersistingCache = FileSystemCacheBuilder.newBuilder()
                .maximumSize(1L)
                .build();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        fileSystemPersistingCache.invalidateAll();
    }

        @Test
    public void testCachePersistence() throws Exception {

        final int testSize = 100;
        List<KeyValuePair> keyValuePairs = KeyValuePair.makeTestElements(testSize);

        for (KeyValuePair keyValuePair : keyValuePairs) {
            fileSystemPersistingCache.put(keyValuePair.getKey(), keyValuePair.getValue());
        }

        assertEquals(fileSystemPersistingCache.size(), testSize);

        for (KeyValuePair keyValuePair : keyValuePairs) {
            String valueFromCache = fileSystemPersistingCache.getIfPresent(keyValuePair.getKey());
            assertNotNull(valueFromCache);
            assertEquals(valueFromCache, keyValuePair.getValue());
            assertEquals(fileSystemPersistingCache.size(), testSize);
        }

        final int manualDeleteSize = 10, factor = 3;
        assertTrue(manualDeleteSize * factor < testSize);

        for (int i = 0; i < manualDeleteSize; i++) {
            int index = i * factor;
            fileSystemPersistingCache.invalidate(KeyValuePair.makeKey(index));
            String value = fileSystemPersistingCache.getIfPresent(KeyValuePair.makeKey(index));
            assertNull(value);
            assertEquals(fileSystemPersistingCache.size(), testSize - i - 1);
        }

        fileSystemPersistingCache.invalidateAll();
        assertEquals(fileSystemPersistingCache.size(), 0);
    }

    @Test
    public void testAddByCallable() throws Exception {

        final String callableReturnValue = "individual";
        final Callable<String> callable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                return callableReturnValue;
            }
        };

        String value0 = fileSystemPersistingCache.get(KeyValuePair.makeKey(0), callable);
        assertNotNull(value0);
        assertEquals(value0, callableReturnValue);

        fileSystemPersistingCache.put(KeyValuePair.makeKey(1), KeyValuePair.makeValue(1));
        String value1 = fileSystemPersistingCache.getIfPresent(KeyValuePair.makeKey(1));
        assertNotNull(value1);
        assertEquals(value1, KeyValuePair.makeValue(1));

        value1 = fileSystemPersistingCache.get(KeyValuePair.makeKey(1), callable);
        assertNotNull(value1);
        assertEquals(value1, KeyValuePair.makeValue(1));

        value0 = fileSystemPersistingCache.getIfPresent(KeyValuePair.makeKey(0));
        assertNotNull(value0);
        assertEquals(value0, callableReturnValue);
    }
}
