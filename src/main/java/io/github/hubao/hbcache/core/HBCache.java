package io.github.hubao.hbcache.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/*
 *
 *
 * @author hubao
 * @since 2024/6/19 11:48
 */
public class HBCache {



    Map<String, String> map = new HashMap<>();

    public String get(String key) {
        return map.get(key);
    }

    public void set(String key, String value) {
        map.put(key, value);
    }

    public int del(String... keys) {
        return keys == null ? 0 : (int) Arrays.stream(keys)
                .map(map::remove).filter(Objects::nonNull).count();
    }

    public int exists(String... keys) {
        return keys == null ? 0 : (int) Arrays.stream(keys).filter(map::containsKey).count();
    }

    public String[] mget(String... keys) {
        return keys == null ? new String[0] : keys.length == 0 ? new String[0] :
                Arrays.stream(keys).map(map::get).toArray(String[]::new);
    }

    public void mset(String[] keys, String[] vals) {
        if (keys == null || keys.length == 0) {
            return;
        }
        for (int i = 0; i < keys.length; i++) {
            set(keys[i], vals[i]);
        }
    }

    public int incr(String key) {
        String str = get(key);
        int val = 0;
        try {
            if (str != null) {
                val = Integer.parseInt(str);
            }
            val++;
            set(key, String.valueOf(val));
        } catch (NumberFormatException e) {
            throw e;
        }
        return val;
    }

    public int decr(String key) {
        String str = get(key);
        int val = 0;
        try {
            if (str != null) {
                val = Integer.parseInt(str);
            }
            val--;
            set(key, String.valueOf(val));
        } catch (NumberFormatException e) {
            throw e;
        }
        return val;
    }
}
