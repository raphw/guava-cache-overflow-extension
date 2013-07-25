package com.blogspot.mydailyjava.guava.cache.overflow;

import com.google.common.cache.Cache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

public class RemovalListenerTest {

    private Cache<String, String> fileSystemPersistingCache;
    private boolean[] listenerResults;

    @BeforeMethod
    public void setUp() throws Exception {
        listenerResults = new boolean[100];
        fileSystemPersistingCache = FileSystemCacheBuilder.newBuilder()
                .maximumSize(1L)
                .removalListener(new RemovalListener<String, String>() {
                    @Override
                    public void onRemoval(RemovalNotification<String, String> notification) {
                        listenerResults[KeyValuePair.fromKey(notification.getKey())] = true;
                    }
                })
                .build();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        fileSystemPersistingCache.invalidateAll();
    }

    @Test
    public void testRemovalListener() throws Exception {

        for (int i = 0; i < listenerResults.length; i++) {
            fileSystemPersistingCache.put(KeyValuePair.makeKey(i), KeyValuePair.makeValue(i));
        }

        for (int i = 0; i < listenerResults.length; i++) {
            fileSystemPersistingCache.invalidate(KeyValuePair.makeKey(i));
        }

        for (boolean listenerResult : listenerResults) {
            assertTrue(listenerResult);
        }

    }
}
