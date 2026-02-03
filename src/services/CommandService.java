package services;


import entity.Command;
import entity.CommandType;
import exception.InvalidCommandException;

import static entity.CommandType.PUT;

public class CommandService {

    Command parse(String command) {

        String[] arr = command.split(" ");
        CommandType type = CommandType.valueOf(arr[0].toUpperCase());
        Command cmd = new Command() ;
        cmd.type = type ;


        switch (type) {
            case PUT:  parsePut(arr , cmd);
                break;
            case GET , DELETE : parseKey(arr , cmd);
                break;
            default:
                throw new InvalidCommandException("Unknown command type");


        }

        return cmd  ;

    }

    void parseKey(String  [] arr , Command command) {

        try{
            int key = Integer.parseInt(arr[1]);
            command.key = key;
        }
        catch (NumberFormatException e){
            throw new InvalidCommandException("key value has to be numeric");
        }


    }

    void parsePut(String[] arr , Command command) {

        try {
            parseKey(arr , command);
            String value = arr[2];
            command.rawValue = value;
            if (arr.length == 4) {
                parseTtl(arr , command);
            }
        }
        catch (Exception e){
            throw new InvalidCommandException("PUT command is invalid ");
        }

    }

    void parseTtl(String[] arr , Command command) {
        try {
            long ttl = Long.parseLong(arr[3]);
            command.ttl = ttl;
        }
        catch (Exception e){
            throw new InvalidCommandException("TTL must be numeric");
        }

    }

}
