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

        // Phase 2–4: database implementation
        IDatabaseService<String> db = new DatabaseService<>();

        // Phase 1: command parser
        CommandService commandService = new CommandService();

        Scanner scanner = new Scanner(System.in);
        System.out.println("Mini In-Memory DB (Phase 1–4).");
        System.out.println("Commands: PUT <key> <value> [ttlMillis], GET <key>, DELETE <key>, EXIT");

        while (true) {
            System.out.print("> ");
            String line;
            if (!scanner.hasNextLine()) {
                break;
            }
            line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;

            Command cmd;
            try {
                // Phase 1: parse raw line into Command object
                cmd = commandService.parse(line);
            } catch (InvalidCommandException e) {
                System.out.println("Invalid command: " + e.getMessage());
                continue;
            }

            // Phase 2–4: execute on DB
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
                            System.out.println("OK (PUT with TTL)");
                        } else {
                            // Phase 2: PUT without TTL
                            db.put(cmd.key, cmd.rawValue);
                            System.out.println("OK (PUT)");
                        }
                        break;

                    case GET:
                        try {
                            // Phase 4: GET with lazy expiration inside db.get()
                            String value = db.get(cmd.key);
                            System.out.println("VALUE: " + value);
                        } catch (KeyNotFoundException e) {
                            System.out.println("NOT FOUND: " + e.getMessage());
                        }
                        break;

                    case DELETE:
                        try {
                            db.delete(cmd.key);
                            System.out.println("DELETED");
                        } catch (KeyNotFoundException e) {
                            System.out.println("NOT FOUND: " + e.getMessage());
                        }
                        break;

                    default:
                        System.out.println("Command not supported in this phase: " + cmd.type);
                        break;
                }
            } catch (RuntimeException e) {
                // covers Invalid TTL or any other runtime error
                System.out.println("ERROR: " + e.getMessage());
            }
        }

        scanner.close();
    }
}
