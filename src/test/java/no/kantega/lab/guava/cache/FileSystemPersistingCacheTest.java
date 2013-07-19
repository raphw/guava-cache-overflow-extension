package no.kantega.lab.guava.cache;

import com.google.common.cache.CacheBuilder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.LinkedList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

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
        List<KeyValuePair> keyValuePairs = makeTestElements(testSize);

        for (KeyValuePair keyValuePair : keyValuePairs) {
            fileSystemPersistingCache.put(keyValuePair.getKey(), keyValuePair.getValue());
        }

        for (KeyValuePair keyValuePair : keyValuePairs) {
            String valueFromCache = fileSystemPersistingCache.getIfPresent(keyValuePair.getKey());
            assertNotNull(valueFromCache);
            assertEquals(valueFromCache, keyValuePair.getValue());
        }

        assertEquals(fileSystemPersistingCache.size(), testSize);

        fileSystemPersistingCache.invalidateAll();
        assertEquals(fileSystemPersistingCache.size(), 0);
    }

    private List<KeyValuePair> makeTestElements(int size) {
        List<KeyValuePair> testElements = new LinkedList<KeyValuePair>();
        for (int i = 0; i < size; i++) {
            testElements.add(new KeyValuePair(makeKey(i), makeValue(i)));
        }
        return testElements;
    }

    private static final class KeyValuePair {

        private final String key, value;

        private KeyValuePair(String key, String value) {
            this.key = key;
            this.value = value;
        }

        private String getKey() {
            return key;
        }

        private String getValue() {
            return value;
        }
    }

    private String makeKey(int i) {
        return "key" + i;
    }

    private String makeValue(int i) {
        return "value" + i;
    }
}
