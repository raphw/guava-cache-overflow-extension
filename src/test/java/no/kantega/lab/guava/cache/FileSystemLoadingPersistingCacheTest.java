package no.kantega.lab.guava.cache;

import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class FileSystemLoadingPersistingCacheTest {

    private LoadingCache<String, String> fileSystemPersistingCache;

    @BeforeMethod
    public void setUp() throws Exception {
        fileSystemPersistingCache = FileSystemCacheBuilder.newBuilder()
                .maximumSize(1L)
                .build(new CacheLoader<String, String>() {
                    @Override
                    public String load(String key) throws Exception {
                        return KeyValuePair.makeValue(KeyValuePair.fromKey(key));
                    }
                });
    }

    @AfterMethod
    public void tearDown() throws Exception {
        fileSystemPersistingCache.invalidateAll();
    }

    @Test
    public void testLoading() throws Exception {

        final int size = 100;

        for (int i = 0; i < size; i++) {
            assertEquals(fileSystemPersistingCache.getUnchecked(KeyValuePair.makeKey(i)), KeyValuePair.makeValue(i));
            assertEquals(fileSystemPersistingCache.size(), i + 1);
        }

    }
}
