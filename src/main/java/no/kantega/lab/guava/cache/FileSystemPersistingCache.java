package no.kantega.lab.guava.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.channels.FileLock;
import java.util.Arrays;
import java.util.List;

public class FileSystemPersistingCache<K, V> extends AbstractPersistingCache<K, V> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemPersistingCache.class);

    private final File persistenceRootDirectory;

    protected FileSystemPersistingCache(CacheBuilder<Object, Object> cacheBuilder) {
        this(cacheBuilder, Files.createTempDir());
    }

    protected FileSystemPersistingCache(CacheBuilder<Object, Object> cacheBuilder, File persistenceDirectory) {
        super(cacheBuilder);
        this.persistenceRootDirectory = validateDirectory(persistenceDirectory);
        LOGGER.info("Persisting to {}", persistenceDirectory.getAbsolutePath());

    }

    private File validateDirectory(File directory) {
        directory.mkdirs();
        if (!directory.exists() || !directory.isDirectory() || !directory.canRead() || !directory.canWrite()) {
            throw new IllegalArgumentException();
        }
        return directory;
    }

    private File pathToFileFor(K key) {
        List<String> pathSegments = directoryFor(key);
        File persistenceFile = persistenceRootDirectory;
        for (String pathSegment : pathSegments) {
            persistenceFile = new File(persistenceFile, pathSegment);
        }
        if (persistenceRootDirectory.equals(persistenceFile) || persistenceFile.isDirectory()) {
            throw new IllegalArgumentException();
        }
        return persistenceFile;
    }

    @Override
    protected V findPersisted(K key) throws IOException {
        if (!isPersist(key)) return null;
        File persistenceFile = pathToFileFor(key);
        if (!persistenceFile.exists()) return null;
        FileInputStream fileInputStream = new FileInputStream(persistenceFile);
        try {
            FileLock fileLock = fileInputStream.getChannel().lock(0, Long.MAX_VALUE, true);
            try {
                return readPersisted(key, fileInputStream);
            } finally {
                fileLock.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            fileInputStream.close();
        }
    }

    @Override
    protected void persistValue(K key, V value) throws IOException {
        if (!isPersist(key)) return;
        File persistenceFile = pathToFileFor(key);
        persistenceFile.getParentFile().mkdirs();
        FileOutputStream fileOutputStream = new FileOutputStream(persistenceFile);
        try {
            FileLock fileLock = fileOutputStream.getChannel().lock();
            try {
                persist(key, value, fileOutputStream);
            } finally {
                fileLock.release();
            }
        } finally {
            fileOutputStream.close();
        }
    }

    @Override
    protected void persist(K key, V value, OutputStream outputStream) throws IOException {
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(value);
        objectOutputStream.flush();
    }

    @Override
    protected boolean isPersist(K key) {
        return true;
    }

    @Override
    protected List<String> directoryFor(K key) {
        return Arrays.asList(key.toString());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected V readPersisted(K key, InputStream inputStream) throws IOException {
        try {
            return (V) new ObjectInputStream(inputStream).readObject();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(String.format("Serialized version assigned by %s was invalid", key), e);
        }
    }

    @Override
    protected void deletePersistedIfExistent(K key) {
        File file = pathToFileFor(key);
        if (!file.delete()) {
            throw new RuntimeException(String.format("Could not delete persisted file %s to key %s",
                    file.getAbsolutePath(), key));
        }
    }
}
