package services;
import entity.Entry;
import java.util.HashMap;

public class DatabaseService<T>  implements IDatabaseService{
    private HashMap<Integer , Entry<T>> data ;
    public DatabaseService() {

    }


    @Override
    public void put(Integer key, Object data) {

    }

    @Override
    public Object get(Integer key) {
        return null;
    }

    @Override
    public void delete(Integer key) {

    }
}