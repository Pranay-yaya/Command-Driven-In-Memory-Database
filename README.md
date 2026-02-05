**Overview**
This mini‑project implements a command‑driven, in‑memory database that:

Uses Integer keys and generic values (T)
Accepts commands from the terminal (PUT, GET, DELETE, START, STOP, EXIT)
Supports optional TTL (time‑to‑live) for entries
Performs lazy TTL expiration on GET
Runs a background cleanup thread to remove expired keys
Supports starting and stopping the database at runtime

The main classes are:
entity.Command, entity.CommandType
entity.Entry<T>
services.CommandService
services.IDatabaseService<T>
services.DatabaseService<T>
services.CommandExecutorService (in multi‑threaded phase, if you use it)


Main (command‑line driver)
OOP design
The project is structured using single responsibility and encapsulation:
1. Command model layer
Command
Represents a parsed user command: type, key, rawValue, ttl.
Pure data holder, no business logic.
CommandType
Enum of all supported commands: PUT, GET, DELETE, START, STOP, EXIT.

3. Parsing layer
CommandService
Responsible only for parsing raw text input (e.g. "PUT 1 hello 5000") into a Command object.
Validates command name, key format, and TTL format.
Throws InvalidCommandException for invalid commands.
This separation allows parsing to be tested independently of database logic and follows the separation of concerns principle.

3. Domain model
Entry<T>
Wraps the stored value and its TTL information.
Fields: data (value), expiryTime (-1 = no expiry).
Method isExpired() encapsulates the expiration logic for an entry, hiding raw timestamp handling from higher layers.

4. Database abstraction
IDatabaseService<T>
Interface defining the core database operations:
void put(Integer key, Object value)
void put(Integer key, Object value, long ttl)
T get(Integer key)
void delete(Integer key)
Allows different implementations (single‑threaded, concurrent) to be swapped without changing callers.

5. Database implementation
DatabaseService<T>
Implements IDatabaseService<T>.
Holds the actual in‑memory storage: ConcurrentHashMap<Integer, Entry<T>> data.
Implements TTL logic:
put(key, value) stores a non‑expiring entry.
put(key, value, ttl) computes an absolute expiry time using System.currentTimeMillis() + ttl.
get(key) performs lazy expiration: if the entry is expired, it removes it and reports it as missing.
Manages lifecycle:
volatile boolean state indicates whether DB is running or stopped.
start() and stop() control the state.
Manages background cleanup:
backGroundTask() starts a daemon thread that periodically calls cleanUp() to remove expired keys.

Application / UI layer
Main
Reads commands from System.in.
Uses CommandService to parse each line into a Command.
Executes the Command by calling DatabaseService methods.
Handles START / STOP / EXIT and prints appropriate messages to the console.
This layer is the only one that knows about the console I/O, keeping the core logic independent of the user interface.
This design keeps each class focused on one job, makes it easy to test components separately, and follows standard OOP practices (encapsulation, abstraction, and clear boundaries between layers).



**Thread-safety strategy**
The primary thread‑safety strategy is:
Concurrent data structure
The database uses ConcurrentHashMap<Integer, Entry<T>> as the main storage.
ConcurrentHashMap allows safe concurrent reads and writes from multiple threads without external synchronization.
The background cleanup thread and user command threads can operate on the map at the same time.

State flag for lifecycle
A volatile boolean state flag controls whether the database is running.
Each mutating or reading operation (put, get, delete) checks the flag:
If state == false, operations throw DatabaseStoppedException.
This prevents operations from executing when the database is logically “stopped”.

Background TTL cleanup thread
backGroundTask() starts a daemon thread that runs in a loop:
Calls cleanUp() (which uses removeIf on the entrySet) to remove expired entries.
Sleeps for a fixed interval (cleanupTime).
Because the map is a ConcurrentHashMap, iteration and modification from multiple threads do not cause ConcurrentModificationException.

Command execution

In the single‑threaded CLI mode (Main), commands are processed sequentially on the main thread.
In a more advanced, multi‑threaded version (Phase 5+), command executors can run in parallel, but they all share the same DatabaseService instance and rely on ConcurrentHashMap for safe access.
Overall, thread safety is achieved by combining:
A concurrent collection (ConcurrentHashMap) for shared state,
A volatile flag for lifecycle coordination,
Limited critical state (no complex shared mutable objects beyond the map and the state flag).


**synchronized usage**
In the final version of the code you are using now, synchronized is not strictly necessary because:
ConcurrentHashMap already provides internal synchronization and safe concurrent access.
The lifecycle flag (state) uses volatile for visibility instead of synchronized blocks.
However, in the intermediate design (Phase 6), the intended approach is:

Wrap all DB operations with a single monitor lock:
Either by using synchronized methods:
java
public synchronized void put(Integer key, Object value) { ... }
public synchronized T get(Integer key) { ... }
public synchronized void delete(Integer key) { ... }

Or by using a private lock object:
java
private final Object lock = new Object();
public void put(Integer key, Object value) {
    synchronized (lock) {
        // modify map
    }
}

This ensures atomicity of operations:
No two threads can execute put, get, or delete at the same time on the same database instance.

Trade‑off:
Very simple to reason about but low concurrency: one global lock means every DB call waits for others.
In your final concurrent version with ConcurrentHashMap, explicit synchronized around map operations is no longer required and would only reduce concurrency. So:
Intermediate educational phase: show synchronized for atomic operations.
Final implementation: rely on ConcurrentHashMap and remove unnecessary synchronized to improve scalability.
You can mention this in the README as “we used synchronized in Phase 6 to demonstrate coarse‑grained locking; later replaced by ConcurrentHashMap in Phase 10 for better concurrency.”


**volatile usage**
The project uses a volatile field to control the database lifecycle:
java
volatile private boolean state;
Why volatile?
volatile ensures visibility across threads:
When one thread calls start() or stop() and writes to state, all other threads reading state see the updated value immediately.

This is crucial for:
User command threads (PUT/GET/DELETE) that check state to decide whether to proceed.
Future extensions where multiple worker threads might be reading and writing concurrently.

How it is used
In DatabaseService:
java
public void start() {
    state = true;
}
public void stop() {
    state = false;
}
@Override
public void put(Integer key, Object value) {
    if (!state) {
        throw new DatabaseStoppedException("Unable to connect to Db");
    }
    // normal put logic...
}

@Override
public T get(Integer key) {
    if (!state) {
        throw new DatabaseStoppedException("Database is stopped");
    }
    // normal get logic...
}

@Override
public void delete(Integer key) {
    if (!state) {
        throw new DatabaseStoppedException("Database is stopped");
    }
    // normal delete logic...
}
Any thread calling put, get, or delete reads the current value of state, which is guaranteed to be up‑to‑date because of volatile.

What would break without volatile?
Without volatile, one thread might set state = false in stop(), but other threads could still see a cached true value in state and continue to operate on the database even though it is supposed to be stopped.
This would violate the lifecycle contract and lead to inconsistent behavior.

So volatile is the correct tool here:
It provides just enough synchronization for a simple flag that is read often and written infrequently.
It avoids the overhead and complexity of using synchronized or other locks for this use case.
