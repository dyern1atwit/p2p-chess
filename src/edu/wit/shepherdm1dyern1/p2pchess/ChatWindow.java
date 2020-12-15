package edu.wit.shepherdm1dyern1.p2pchess;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;

import java.io.IOException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;


public class ChatWindow extends Application {
    public int port; //port that the chat window is running ojn
    public String ip; //ip that the chat window is running on
    public Stage stage; //main running stage
    public Scene scene; //main running scene
    public ScrollPane scroll; //pane where text is displayed
    public VBox scrollBox; //scrollbar
    TextField msgField; //text input field
    public boolean isHost; //boolean to show if client is host or not
    public static ChatServerThread serverThread; //chat server thread
    public static ChatConnectionThread connectionThread; //chat connection thread
    public static ChatWindow thisChat; //instance of this class that is used for interfacing

    //constructor that creates the chat window and sets local variables to given arguments
    public ChatWindow(String ip, int port, boolean isHost) throws IOException {
        this.ip = ip;
        this.port = port+1;
        this.isHost = isHost;
        this.stage = new Stage();
        thisChat = this;
        startChat();
    }

    @Override
    public void start(Stage stage) {
    }

    //creates the chat window
    private void startChat() throws IOException {
        if (isHost) {
            serverThread = new ChatServerThread(port);
            serverThread.start();

        }
        else {
            connectionThread = new ChatConnectionThread(ip, port);
            connectionThread.start();
        }
        BorderPane base = new BorderPane();
        scroll = new ScrollPane();
        HBox entry = new HBox();
        scrollBox = new VBox();
        scrollBox.setPrefWidth(550);

        Button sendMsg = new Button("Send");
        sendMsg.setPrefSize(70, 20);

        msgField = new TextField();
        msgField.setPrefWidth(480);
        sendMsg.setOnAction(e -> {
            try {
                send(msgField.getText());
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });

        entry.getChildren().addAll(msgField, sendMsg);
        scroll.setContent(scrollBox);
        base.setCenter(scroll);
        base.setBottom(entry);

        this.stage.setTitle("Chat");
        this.scene = new Scene(base, 300, 400);
        scene.setOnKeyPressed(ke -> {
            KeyCode keyCode = ke.getCode();
            if (keyCode.equals(KeyCode.ENTER)) {
                send(msgField.getText());
            }
        });
        this.stage.setScene(scene);
        this.stage.setResizable(false);
        this.stage.show();
        if (isHost) {
            gameEvent("Your local IP address is the following: ");
            for (String s : getLocalIPs()) {
                gameEvent(s);
            }
            gameEvent("If you have port forwarding enabled,");
            gameEvent("your external IP is the following:");
            gameEvent(getPublicIP());
        }
    }

    //sends a given message to the other player and adds message to current player's chat
    public void send(String msg) {
        if (isHost) {
            try {
                serverThread.getConnectionThread().send(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                connectionThread.send(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        receive(msg, true);
        msgField.clear();
    }

    //adds a received message from the other player
    public void receive(String msg, boolean isMe){
        if (isHost) {
            if (isMe) {
                Text message = new Text("White: " + msg);
                scrollBox.getChildren().add(message);
            } else {
                Label message = new Label("Black: " + msg);
                message.setPrefWidth(550);
                message.setWrapText(true);
                scrollBox.getChildren().add(message);
            }
        }
        else {
            if (isMe) {
                Text message = new Text("Black: " + msg);
                scrollBox.getChildren().add(message);
            } else {
                Label message = new Label("White: " + msg);
                message.setPrefWidth(550);
                message.setWrapText(true);
                scrollBox.getChildren().add(message);
            }
        }
    }

    //adds a game event to the chat window in blue
    public void gameEvent(String msg) {
        Text message = new Text(msg);
        message.setFill(Color.BLUE);
        scrollBox.getChildren().add(message);
    }

    //closes the chat connection
    public void closeConnection() {
        if (isHost) {
            try {
                serverThread.getConnectionThread().closeConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                connectionThread.closeConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //gets the local ip of the host
    //solution from "roylaurie" on StackOverflow
    //https://stackoverflow.com/questions/8083479/java-getting-my-ip-address
    public ArrayList<String> getLocalIPs() throws SocketException {
        ArrayList<String> localIPs = new ArrayList<>();
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface iface = interfaces.nextElement();
            // filters out 127.0.0.1 and inactive interfaces
            if (iface.isLoopback() || !iface.isUp())
                continue;

            Enumeration<InetAddress> addresses = iface.getInetAddresses();
            while(addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                String localIP = addr.getHostAddress();
                for (char c : localIP.toCharArray()) {
                    if (c == ':') {
                        localIP = null;
                        break;
                    }
                }
                if (localIP != null) localIPs.add(localIP);
            }
        }
        return localIPs;
    }

    //gets the public ip of the host
    //solution from "bakkal" on StackOverflow
    //https://stackoverflow.com/questions/2939218/getting-the-external-ip-address-in-java
    public String getPublicIP () throws IOException {
        URL whatismyip = new URL("http://checkip.amazonaws.com");
        BufferedReader in = new BufferedReader(new InputStreamReader(
                whatismyip.openStream()));
        return in.readLine();
    }
}