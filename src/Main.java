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

        // Our in-memory DB
        DatabaseService<String> dbImpl = new DatabaseService<>();
        IDatabaseService<String> db = dbImpl; // use interface where possible

        // Command parser (Phase 1)
        CommandService commandService = new CommandService();

        Scanner scanner = new Scanner(System.in);
        System.out.println("In-Memory DB (data-driven).");
        System.out.println("Use:");
        System.out.println("  Command  ");
        System.out.println("  PUT  key  Value");
        System.out.println("  PUT  key  Value ttl");
        System.out.println("  GET  key");
        System.out.println("  DELETE  key ");
        System.out.println("  START");
        System.out.println("  STOP");
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
                            // PUT with TTL
                            db.put(cmd.key, cmd.rawValue, cmd.ttl);
                            System.out.println("OK (PUT key=" + cmd.key + ", value=" + cmd.rawValue + ", ttl=" + cmd.ttl + ")");
                        } else {
                            // PUT without TTL
                            db.put(cmd.key, cmd.rawValue);
                            System.out.println("OK (PUT key=" + cmd.key + ", value=" + cmd.rawValue + ")");
                        }
                        break;

                    case GET:
                        try {
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
                        dbImpl.start();
                        System.out.println("DB STARTED");
                        break;

                    case STOP:
                        dbImpl.stop();
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
