package process.main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        ConsoleHelper.writeMessage("Enter the server port: ");
        int port = ConsoleHelper.readInt();
        try (ServerSocket server = new ServerSocket(port)){
            ConsoleHelper.writeMessage("Server was launched on the: " + port + " port");
            while(true){
                Socket socket = server.accept();
                new Handler(socket).start();
            }
        }catch (Exception e){
            ConsoleHelper.writeMessage(e.getMessage());
        }
    }

    public static void sendBroadcastMessage(Message message){
        for (String name : connectionMap.keySet()){
            try{
                connectionMap.get(name).send(message);
            } catch (IOException e){
                ConsoleHelper.writeMessage("Can't send a message to: " + name);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
    private static class Handler extends Thread{
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            ConsoleHelper.writeMessage("Connection was settled with  " + socket.getRemoteSocketAddress());
            String userName = "";
            try (Connection connection = new Connection(socket)){
                userName = serverHandShake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
                sendListOfUsers(connection, userName);
                serverMainLoop(connection, userName);
            } catch (IOException e) {
                ConsoleHelper.writeMessage("There is a mistake with remote address.");
            } catch (ClassNotFoundException e) {
                ConsoleHelper.writeMessage("There is a mistake with remote address.");
            }
            if (userName != null){
                connectionMap.remove(userName);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
            }
            ConsoleHelper.writeMessage("Connection with  " + socket.getRemoteSocketAddress() + " was closed");
        }

        public String serverHandShake(Connection connection) throws IOException, ClassNotFoundException {
            String name = "";
            boolean accepted = false;
            while (!accepted){
                connection.send(new Message(MessageType.NAME_REQUEST));
                Message receivedName = connection.receive();
                if (receivedName.getType() == MessageType.USER_NAME){
                    name = receivedName.getData();
                    if (!name.isEmpty() && connectionMap.get(name) == null){
                        connectionMap.put(name, connection);
                        connection.send(new Message(MessageType.NAME_ACCEPTED));
                        accepted = true;
                    }
                }
            }
            return name;
        }
        private void sendListOfUsers(Connection connection, String userName) throws IOException, ClassNotFoundException {
            for (String name: connectionMap.keySet()){
                if (!userName.equals(name))
                    connection.send(new Message(MessageType.USER_ADDED, name));
            }
        }
        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (!Thread.currentThread().isInterrupted()) {
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT) {
                    String messageText = userName + ": " + message.getData();
                    sendBroadcastMessage(new Message(MessageType.TEXT, messageText));
                } else ConsoleHelper.writeMessage(
                        String.format("Error! Not acceptable type of message (MessageType.%s) from client: %s",
                                message.getType().toString() ,userName)
                );
            }
        }
    }
}
