package services;

public interface IDatabaseService<T> {

    void put(Integer key, T value);
    T get(Integer key);
    void delete(Integer key);
}
