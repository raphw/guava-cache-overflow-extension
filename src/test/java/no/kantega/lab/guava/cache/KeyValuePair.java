package no.kantega.lab.guava.cache;

import java.util.LinkedList;
import java.util.List;

public class KeyValuePair {

    private static final String KEY_PREFIX = "key";
    private static final String VALUE_PREFIX = "value";

    private final String key, value;

    public KeyValuePair(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KeyValuePair that = (KeyValuePair) o;

        return !(key != null ? !key.equals(that.key) : that.key != null)
                && !(value != null ? !value.equals(that.value) : that.value != null);

    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "KeyValuePair{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }

    public static String makeKey(int i) {
        return KEY_PREFIX + i;
    }

    public static String makeValue(int i) {
        return VALUE_PREFIX + i;
    }

    public static int fromKey(String value) {
        return Integer.valueOf(value.substring(KEY_PREFIX.length(), value.length()));
    }

    public static int fromValue(String value) {
        return Integer.valueOf(value.substring(VALUE_PREFIX.length(), value.length()));
    }

    public static List<KeyValuePair> makeTestElements(int size) {
        List<KeyValuePair> testElements = new LinkedList<KeyValuePair>();
        for (int i = 0; i < size; i++) {
            testElements.add(new KeyValuePair(makeKey(i), makeValue(i)));
        }
        return testElements;
    }
}