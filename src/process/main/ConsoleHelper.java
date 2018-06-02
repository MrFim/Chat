package process.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleHelper {
    private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public static void writeMessage(String message){
        System.out.println(message);
    }
    public static String readString(){
        String result = "";
        try {
            result = reader.readLine();
        } catch (IOException e) {
            writeMessage("There is a mistake during entering a string. Try again.");
            readString();
        }
        return result;
    }
    public static int readInt(){
        int result = 0;
        try{
            result = Integer.parseInt(readString());
        }catch (NumberFormatException e){
            writeMessage("There is a mistake during enter a number. Try again.");
            readInt();
        }
        return result;
    }
}

