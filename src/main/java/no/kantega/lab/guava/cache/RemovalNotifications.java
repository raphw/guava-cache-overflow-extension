package no.kantega.lab.guava.cache;

import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

class RemovalNotifications {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemovalNotifications.class);

    private static RemovalNotifications INSTANCE;

    private static RemovalNotifications getInstance() {
        if (INSTANCE != null) return INSTANCE;
        synchronized (RemovalNotifications.class) {
            if (INSTANCE != null) return INSTANCE;
            return INSTANCE = new RemovalNotifications();
        }
    }

    public static <K, V> RemovalNotification<K, V> make(K key, V value) {
        return getInstance().makeInternal(key, value);
    }

    private final Constructor<RemovalNotification> constructor;

    private RemovalNotifications() {
        try {
            constructor = RemovalNotification.class.getDeclaredConstructor(Object.class, Object.class, RemovalCause.class);
            constructor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            String message = String.format("Could not find known constructor for %s", RemovalNotification.class.getCanonicalName());
            LOGGER.error(message, e);
            throw new IllegalStateException(message, e);
        }
    }

    @SuppressWarnings("unchecked")
    private <K, V> RemovalNotification<K, V> makeInternal(K key, V value) {
        try {
            try {
                return (RemovalNotification<K, V>) constructor.newInstance(key, value, RemovalCause.EXPLICIT);
            } catch (InvocationTargetException e) {
                throw new IllegalStateException(String.format("Creating an instance of %s for key %s and value %s caused an exception to be thrown",
                        RemovalNotification.class.getCanonicalName(), key, value), e);
            } catch (InstantiationException e) {
                throw new IllegalStateException(String.format("Could not call %s's constructor for key %s and value %s",
                        RemovalNotification.class.getCanonicalName(), key, value), e);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(String.format("Could not access %s's constructor for key %s and value %s",
                        RemovalNotification.class.getCanonicalName(), key, value), e);
            }
        } catch (RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }
}
