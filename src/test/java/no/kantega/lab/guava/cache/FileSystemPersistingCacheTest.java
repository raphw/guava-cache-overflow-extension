package no.kantega.lab.guava.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

public class FileSystemPersistingCacheTest {

    private Cache<String, String> fileFallbackCache;

    @BeforeMethod
    public void setUp() throws Exception {
        fileFallbackCache = new FileSystemPersistingCache<String, String>(
                CacheBuilder.newBuilder().maximumSize(1L));
    }

    @Test
    public void testFileWrite() throws Exception {

        fileFallbackCache.put("1", "1");
        fileFallbackCache.put("2", "2");

        assertEquals("1", fileFallbackCache.getIfPresent("1"));
    }
}
