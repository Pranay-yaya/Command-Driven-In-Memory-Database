import entity.Command;
import entity.CommandType;
import services.CommandService;
import services.DatabaseService;
import services.IDatabaseService;
import exception.InvalidCommandException;
import exception.KeyNotFoundException;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        // Our in-memory DB (Phase 2–4)
        IDatabaseService<String> db = new DatabaseService<>();

        // Command parser (Phase 1)
        CommandService commandService = new CommandService();

        Scanner scanner = new Scanner(System.in);
        System.out.println("In-Memory DB (data-driven).");
        System.out.println("Use:");
        System.out.println("  PUT <key> <value>");
        System.out.println("  PUT <key> <value> <ttlMillis>");
        System.out.println("  GET <key>");
        System.out.println("  DELETE <key>");
        System.out.println("  EXIT");

        while (true) {
            System.out.print("> ");
            if (!scanner.hasNextLine()) break;
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;

            Command cmd;
            try {
                // Phase 1: parse user input into a Command object
                cmd = commandService.parse(line);
            } catch (InvalidCommandException e) {
                System.out.println("Invalid command: " + e.getMessage());
                continue;
            }

            // EXIT just stops the loop (no DB call)
            if (cmd.type == CommandType.EXIT) {
                System.out.println("Exiting...");
                break;
            }

            try {
                switch (cmd.type) {
                    case PUT:
                        if (cmd.ttl != null) {
                            // Phase 3: PUT with TTL
                            db.put(cmd.key, cmd.rawValue, cmd.ttl);
                            System.out.println("OK (PUT key=" + cmd.key + ", value=" + cmd.rawValue + ", ttl=" + cmd.ttl + ")");
                        } else {
                            // Phase 2: PUT without TTL
                            db.put(cmd.key, cmd.rawValue);
                            System.out.println("OK (PUT key=" + cmd.key + ", value=" + cmd.rawValue + ")");
                        }
                        break;

                    case GET:
                        try {
                            // Phase 4: lazy expiration happens inside db.get()
                            String value = db.get(cmd.key);
                            System.out.println("VALUE: " + value);
                        } catch (KeyNotFoundException e) {
                            System.out.println("NOT FOUND: " + e.getMessage());
                        }
                        break;

                    case DELETE:
                        try {
                            db.delete(cmd.key);
                            System.out.println("DELETED key=" + cmd.key);
                        } catch (KeyNotFoundException e) {
                            System.out.println("NOT FOUND: " + e.getMessage());
                        }
                        break;
                    case START:
                        ((DatabaseService<String>) db).start();
                        System.out.println("DB STARTED");
                        break;

                    case STOP:
                        ((DatabaseService<String>) db).stop();
                        System.out.println("DB STOPPED");
                        break;

                    default:
                        System.out.println("Command not supported in this phase: " + cmd.type);
                        break;
                }
            } catch (RuntimeException e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }

        scanner.close();
    }
}
