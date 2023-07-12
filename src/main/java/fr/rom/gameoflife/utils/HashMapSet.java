package fr.rom.gameoflife.utils;

import java.util.*;

@SuppressWarnings("unused")
public class HashMapSet<S, T> {

    protected Map<S, Set<T>> map;

    public HashMapSet() {
        map = new HashMap<>();
    }

    public HashMapSet(final HashMapSet<S,T> set) {
        map = set.getMap();
    }

    public boolean isEmpty() {
        for (Set<T> set : map.values()) {
            if (!set.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public boolean containsKey(final S key) {
        return map.containsKey(key);
    }

    public boolean containsValue(final S key, final T value) {
        return map.containsKey(key) && map.get(key).contains(value);
    }

    public Set<T> get(final S key) {
        return map.get(key);
    }

    public Set<T> safeGet(final S key) {
        map.computeIfAbsent(key, k -> new HashSet<>());
        return map.get(key);
    }

    public void put(final S key) {
        safeGet(key);
    }

    public void put(final S key, final T value) {
        safeGet(key).add(value);
    }

    public void put(final S key, final Collection<T> values) {
        safeGet(key).addAll(values);
    }

    public void remove(final S key) {
        map.remove(key);
    }

    public void removeFromAll(final T value) {
        for (Set<T> set : map.values()) {
            set.remove(value);
        }
    }

    public void remove(final S key, final T value) {
        if (map.containsKey(key)) {
            map.get(key).remove(value);
        }
    }

    public void clear() {
        map.clear();
    }

    public void clear(final S key) {
        safeGet(key).clear();
    }

    public Set<S> keySet() {
        return map.keySet();
    }

    public Collection<T> values(final S key) {
        return map.get(key);
    }

    public Set<Map.Entry<S, Set<T>>> entrySet() {
        return map.entrySet();
    }

    public Map<S, Set<T>> getMap() {
        return new HashMap<>(map);
    }
}
