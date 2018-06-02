package process.main.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ThreadLocalRandom;

public class ClientGuiView {
    private final ClientGuiController controller;

    private JFrame frame = new JFrame("Chat room");
    private JTextField textField = new JTextField(50);
    private JTextArea messages = new JTextArea(10, 50);
    private JTextArea users = new JTextArea(10, 10);

    public ClientGuiView(ClientGuiController controller) {
        this.controller = controller;
        initView();
    }

    private void initView() {
        ThreadLocalRandom.current().nextBoolean();
        textField.setEditable(false);
        messages.setEditable(false);
        users.setEditable(false);

        frame.getContentPane().add(textField, BorderLayout.NORTH);
        frame.getContentPane().add(new JScrollPane(messages), BorderLayout.WEST);
        frame.getContentPane().add(new JScrollPane(users), BorderLayout.EAST);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.sendTextMessage(textField.getText());
                textField.setText("");
            }
        });
    }

    public String getServerAddress() {
        return JOptionPane.showInputDialog(
                frame,
                "Enter the server address:",
                "Client configuration",
                JOptionPane.QUESTION_MESSAGE);
    }

    public int getServerPort() {
        while (true) {
            String port = JOptionPane.showInputDialog(
                    frame,
                    "Enter the server port:",
                    "Client configuration",
                    JOptionPane.QUESTION_MESSAGE);
            try {
                return Integer.parseInt(port.trim());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        frame,
                        "Server port is incorrect. Try again.",
                        "Client configuration",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public String getUserName() {
        return JOptionPane.showInputDialog(
                frame,
                "Enter your name:",
                "Client configuration",
                JOptionPane.QUESTION_MESSAGE);
    }

    public void notifyConnectionStatusChanged(boolean clientConnected) {
        textField.setEditable(clientConnected);
        if (clientConnected) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Connection with server was settled",
                    "Chat",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(
                    frame,
                    "Client doesn't connect to the server",
                    "Chat",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshMessage() {
        messages.append(controller.getModel().getNewMessage() + "\n");
    }

    public void refreshUsers() {
        ClientGuiModel model = controller.getModel();
        StringBuilder builder = new StringBuilder();
        for (String userName : model.getAllUserNames()) {
            builder.append(userName).append("\n");
        }
        users.setText(builder.toString());
    }
}
