package com.sina.book.control;

import java.util.HashMap;

/**
 * 调用Task可添加的参数
 * 
 * @author Tsimle
 * 
 */
public class TaskParams {

    private HashMap<String, Object> params = null;

    public TaskParams() {
        params = new HashMap<String, Object>();
    }

    public TaskParams(String key, Object value) {
        this();
        put(key, value);
    }

    public void put(String key, Object value) {
        params.put(key, value);
    }

    public Object get(String key) {
        return params.get(key);
    }

    /**
     * Get the string associated with a key.
     * 
     * @param key
     *            A key string.
     * @return A string which is the value.
     */
    public String getString(String key) {
        Object object = get(key);
        return object == null ? null : object.toString();
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    /**
     * Returns the value associated with the given key, or defaultValue if no
     * mapping of the desired type exists for the given key.
     * 
     * @param key
     *            a String
     * @return an int value
     */
    public int getInt(String key, int defaultValue) {
        Object o = params.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return (Integer) o;
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }

    /**
     * Determine if params contains a specific key.
     * 
     * @param key
     *            A key string.
     * @return true if the key exists
     */
    public boolean has(String key) {
        return params.containsKey(key);
    }

}