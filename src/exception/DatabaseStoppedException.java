package exception;

public class DatabaseStoppedException extends RuntimeException {
    public DatabaseStoppedException(String message) {
        super(message);
    }
}
