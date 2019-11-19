package broker;

import javax.swing.*;
import java.io.*;
import java.net.Socket;

import static java.lang.Integer.parseInt;

public class Main {

    private static final int    PORT = 5000;
    private static final String ADDRESS = "127.0.0.1";

    String          id;
    BufferedReader  in;
    PrintWriter     out;
    BrokerMessage   brokerMessage = new BrokerMessage();

    JFrame frame = new JFrame("Broker");
    JTextField tfItem   = new JTextField(40);
    JTextField tfAmount = new JTextField(40);
    JTextArea messageArea = new JTextArea(8, 40);
    JButton bBuy = new JButton("Buy");
    JButton bSell = new JButton("Sell");

    public static void main(String[] args) throws Exception {
        Main client = new Main();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();
    }

    String addCheckSum(String s) {
        return s + "10=" + s.length();
    }

    public Main() {

        tfItem.setEditable(true);
        messageArea.setEditable(false);
        frame.getContentPane().add(tfItem, "North");
        frame.getContentPane().add(tfAmount, "Center");
        frame.getContentPane().add(new JScrollPane(messageArea), "South");
        frame.getContentPane().add(bBuy, "East");
        frame.getContentPane().add(bSell, "West");
        frame.pack();

        bBuy.addActionListener( e -> {
            String message;

            message = "49=" + this.id + "|";
            message += "COMMAND=BUY|";
            message += "ITEM=" + tfItem.getText() + "|";
            message += "AMOUNT=" + tfAmount.getText() + "|";
            message = addCheckSum(message);
            out.println(message);
            messageArea.append(message + "\n");
        });

        bSell.addActionListener( e -> {
            String message;

            message = "49=" + this.id + "|";
            message += "COMMAND=SELL|";
            message += "ITEM=" + tfItem.getText() + "|";
            message += "AMOUNT=" + tfAmount.getText() + "|";
            message = addCheckSum(message);
            out.println(message);
            messageArea.append(message + "\n");
            tfItem.setText("");
            tfAmount.setText("");
        });


    }

    private void run() throws IOException {
        Socket socket = new Socket(ADDRESS, PORT);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        while (true) {
            String line = in.readLine();
            System.out.println("Recv: " + line);
            if (line.startsWith("SETID")) {
                this.id = line.substring(6);
                messageArea.append("ID: " + this.id + '\n');
            } else {
                brokerMessage.clear();
                parseBrokerMessage(line);
                messageArea.append(brokerMessage.status + "\n");
                messageArea.append(line + "\n");
            }
        }
    }

    public static class BrokerMessage {
        public String   id = null;
        public String   status = null;
        public String   checksum = null;

        boolean isValid() {
            return ((id != null) && (status != null) && (checksum != null));
        }

        void clear() {
            id = null;
            status = null;
            checksum = null;
        }
    }

    private void parseBrokerMessage(String input) {
        String[] fields = input.split("\\|");

        for (String field : fields) {
            String[] value = field.split("=");
            if (value.length != 2) {
                throw new IllegalArgumentException("Invalid message\nArgument length");
            }
            parseBrokerValue(value);
        }
        int length = input.length() - ("10=" + brokerMessage.checksum).length();
        if (length != parseInt(brokerMessage.checksum)) {
            throw new IllegalArgumentException("Invalid message\nMessage doesnt match checksum length\n" + " " + input.length() + " " + brokerMessage.checksum );
        }
    }

    private void parseBrokerValue(String[] value) {
        switch (value[0]) {
            case ("49"): {
                brokerMessage.id = value[1];
                break;
            }
            case ("COMMAND"): {
                brokerMessage.status = value[1];
                break;
            }
            case ("STATUS"): {
                brokerMessage.status = value[1];
                break;
            }
            case ("10"): {
                brokerMessage.checksum = value[1];
                break;
            }
            default:
                throw new IllegalArgumentException("WHAT DID YOU ENTER!?!?\n" + value[0]);
        }
    }

}