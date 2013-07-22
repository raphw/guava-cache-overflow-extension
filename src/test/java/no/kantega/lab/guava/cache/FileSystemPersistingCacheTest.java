package no.kantega.lab.guava.cache;

import com.google.common.cache.CacheBuilder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

public class FileSystemPersistingCacheTest {

    private FileSystemPersistingCache<String, String> fileSystemPersistingCache;

    @BeforeMethod
    public void setUp() throws Exception {
        fileSystemPersistingCache = new FileSystemPersistingCache<String, String>(
                CacheBuilder.newBuilder().maximumSize(1L));
    }

    @Test
    public void testFileWrite() throws Exception {

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
}
