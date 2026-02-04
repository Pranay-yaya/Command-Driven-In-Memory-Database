package services;

public interface IDatabaseService<T> {

    void put(Integer key, Object value);

    void put(Integer key, Object value, long ttl);

    T get(Integer key);

    void delete(Integer key);
}
