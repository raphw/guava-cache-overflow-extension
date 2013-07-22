package no.kantega.lab.guava.cache;

import java.util.LinkedList;
import java.util.List;

class KeyValuePair {

    private final String key, value;

    KeyValuePair(String key, String value) {
        this.key = key;
        this.value = value;
    }

    String getKey() {
        return key;
    }

    String getValue() {
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

    static String makeKey(int i) {
        return "key" + i;
    }

    static String makeValue(int i) {
        return "value" + i;
    }

    static List<KeyValuePair> makeTestElements(int size) {
        List<KeyValuePair> testElements = new LinkedList<KeyValuePair>();
        for (int i = 0; i < size; i++) {
            testElements.add(new KeyValuePair(makeKey(i), makeValue(i)));
        }
        return testElements;
    }
}