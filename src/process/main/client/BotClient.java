package process.main.client;

import process.main.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class BotClient extends Client {

    private static final String WELOME_TEXT = "Hi everyone. I'm a bot. I understand these commands: date, day, month, year, time, hours, minutes, seconds.";
    private volatile static Set<String> botsList = new HashSet<>();
    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSentTextFromConsole() {
        return false;
    }

    @Override
    protected String getUserName() {
        if (botsList.size() >= 100) throw new RuntimeException("The list of bots is full.");
        String botName;
        do {
            botName = "date_bot_" + new Random().nextInt(100);
        } while(botsList.contains(botName));
        botsList.add(botName);
        return botName;
    }

    public static void main(String[] args) {
        new BotClient().run();
    }

    public class BotSocketThread extends SocketThread{
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage(WELOME_TEXT);
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
            String[] messageParts = message.split(": ");
            if (messageParts.length == 2){
                String name = message.split(": ")[0];
                String data = message.split(": ")[1].toLowerCase();
                String datePattern = null;
                switch(data){
                    case "date": datePattern = "d.MM.YYYY"; break;
                    case "day": datePattern = "d"; break;
                    case "month": datePattern = "MMMM"; break;
                    case "year": datePattern = "YYYY"; break;
                    case "time": datePattern = "H:mm:ss"; break;
                    case "hours": datePattern = "H"; break;
                    case "minutes": datePattern = "m"; break;
                    case "seconds": datePattern = "s"; break;
                }
                if (datePattern != null){
                    String answer = String.format(String.format("Information for %s: %s", name
                            , new SimpleDateFormat(datePattern).format(Calendar.getInstance().getTime())));
                    sendTextMessage(answer);
                }
            }

        }
    }
}
