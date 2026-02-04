package services;

import entity.Entry;
import exception.DatabaseStoppedException;
import exception.KeyNotFoundException;

import java.util.concurrent.ConcurrentHashMap;

public class DatabaseService<T> implements IDatabaseService<T> {
    private final ConcurrentHashMap<Integer, Entry<T>> data;
    volatile private boolean state;
    final private int cleaupTime = 1000 ;


    public DatabaseService() {
        this.data = new ConcurrentHashMap<>();
        state=true;
        backGroundTask();
    }



    private void cleanUp() {
        data.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    private void backGroundTask() {
        System.out.println("Background clear started");

        Thread thread = new Thread(()->{

            while(true){

                try {
                    cleanUp() ;
                    Thread.sleep(cleaupTime);

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

        }) ;

        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void put(Integer key, Object value) {
        if(!state){
            throw new DatabaseStoppedException("Unable to connect to Db") ;
        }
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

    public void start() {
        boolean running = true;
    }

    public void stop() {
        boolean running = false;
    }
}
