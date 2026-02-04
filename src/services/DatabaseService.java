package services;

import entity.Entry;
import exception.KeyNotFoundException;

import java.util.HashMap;

public class DatabaseService<T> implements IDatabaseService<T> {

    private final HashMap<Integer, Entry<T>> data;

    public DatabaseService() {
        this.data = new HashMap<>();
    }

    @Override
    public void put(Integer key, Object value) {
        Entry<T> entry = new Entry<>((T) value, -1);
        this.data.put(key, entry);
    }
    @Override
    public void put(Integer key, Object value, long ttl) {
        if (ttl <= 0) {
            throw new IllegalArgumentException("TTL must be > 0");
        }
        Entry<T> entry = new Entry<>((T) value, ttl);
        this.data.put(key, entry);
    }
    // PHASE 4: Lazy expiration on GET
    @Override
    public T get(Integer key) {
        Entry<T> entry = data.get(key);
        if (entry == null) {
            throw new KeyNotFoundException("Key not found");
        }if(data.containsKey(key)){
            if (entry.isExpired()) {
                data.remove(key); // delete expired key
                throw new KeyNotFoundException("Key expired");
            }
        }
        return entry.data;
    }
    @Override
    public void delete(Integer key) {
        Entry<T> removed = data.remove(key);
        if (removed == null) {
            throw new KeyNotFoundException("Key not found");
        }
    }
}
