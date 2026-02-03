package entity;



public class Entry<T>{
    T data ;
    long ttl ;
    public Entry(T data, long ttl) {
        this.data = data ;
        this.ttl = ttl ;
    }

    public Entry(T data) {
        this.data = data ;
        this.ttl = -1 ;
    }
}