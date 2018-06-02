package process.main.client;

import process.main.Connection;
import process.main.ConsoleHelper;
import process.main.Message;
import process.main.MessageType;

import java.io.IOException;
import java.net.Socket;

public class Client {

    protected Connection connection;
    private volatile boolean clientConnected;

    public static void main(String[] args) throws UnknownError {
        Client client = new Client();
        client.run();
    }
    public void run() {
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();
        synchronized (this){
            try {
                this.wait();
            } catch (InterruptedException e) {
                ConsoleHelper.writeMessage("There was a mistake. Program was closed.");
                return;
            }
        }

        if (clientConnected){
            ConsoleHelper.writeMessage("Connection was settled. To exit please write 'exit'.");
        } else {
            ConsoleHelper.writeMessage("There is a mistake during work with a client");
        }

        while(clientConnected){
            String message = ConsoleHelper.readString();
            if (message.equalsIgnoreCase("exit"))
                break;
            if (shouldSentTextFromConsole())
                sendTextMessage(message);
        }
    }
    protected String getServerAddress(){
        ConsoleHelper.writeMessage("Enter the address, please: ");
        return ConsoleHelper.readString();
    }
    protected int getServerPort(){
        ConsoleHelper.writeMessage("Enter the port, please: ");
        return ConsoleHelper.readInt();
    }
    protected String getUserName(){
        ConsoleHelper.writeMessage("Enter your name, please: ");
        return ConsoleHelper.readString();
    }
    protected boolean shouldSentTextFromConsole(){
        return true;
    }
    protected SocketThread getSocketThread(){
        return new SocketThread();
    }
    protected void sendTextMessage(String text){
        try {
            connection.send(new Message(MessageType.TEXT, text));
        } catch (IOException e) {
           ConsoleHelper.writeMessage("There is a IO mistake");
           clientConnected = false;
        } catch (ClassNotFoundException e) {
            ConsoleHelper.writeMessage("There is a ClassNotFound mistake!");
            clientConnected = false;
        }

    }


    public class SocketThread extends Thread{

        @Override
        public void run() {
            int port = getServerPort();
            String serverAddress = getServerAddress();
            try {
                Socket socket = new Socket(serverAddress, port);
                Client.this.connection = new Connection(socket);
                clientHandShake();
                clientMainLoop();
            } catch (IOException e) {
                notifyConnectionStatusChanged(false);
            } catch (ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }
        }

        protected void processIncomingMessage(String message){
            ConsoleHelper.writeMessage(message);
        }
        protected void informAboutAddingNewUser(String userName){
            ConsoleHelper.writeMessage("User " + userName + " was added");

        }
        protected void informAboutDeletingNewUser(String userName){
            ConsoleHelper.writeMessage("User " + userName + " left tha chat");
        }
        protected void notifyConnectionStatusChanged(boolean clientConnected){
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this){
                Client.this.notify();
            }
        }
        protected void clientHandShake() throws IOException, ClassNotFoundException {
            while(true){
                Message msg = connection.receive();
                switch(msg.getType()){
                    case NAME_REQUEST: connection.send(new Message(MessageType.USER_NAME, getUserName())); break;
                    case NAME_ACCEPTED: notifyConnectionStatusChanged(true); return;
                    default: throw new IOException("Unexpected MessageType");
                }
            }
        }
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            while (!Thread.currentThread().isInterrupted()){
                Message msg = connection.receive();
                switch(msg.getType()){
                    case TEXT: processIncomingMessage(msg.getData()); break;
                    case USER_ADDED: informAboutAddingNewUser(msg.getData()); break;
                    case USER_REMOVED: informAboutDeletingNewUser(msg.getData()); break;
                    default: throw new IOException("Unexpected MessageType");
                }
            }
        }
    }
}
