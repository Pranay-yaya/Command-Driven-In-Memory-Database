package entity;


public class Entry<T> {
    public T data;
    public long expiryTime; // -1 = no expiry (Phase 3)

    public Entry(T data, long ttl) {
        this.data = data;
        if (ttl <= 0) {
            this.expiryTime = -1;
        } else {
            this.expiryTime = System.currentTimeMillis() + ttl;
        }
    }

    public boolean isExpired() {
        if (expiryTime == -1) return false;
        return System.currentTimeMillis() > expiryTime;
    }
}
