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

import java.net.InetAddress;

import java.io.IOException;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;


public class ChatWindow extends Application {
    public int port;
    public String ip;
    public Stage stage;
    public Scene scene;
    private BorderPane base;
    public ScrollPane scroll;
    public VBox scrollBox;
    TextField msgField;
    public boolean isHost;
    public static ChatServerThread serverThread;
    public static ChatConnectionThread connectionThread;
    public static ChatWindow thisChat;

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

    private void startChat() throws IOException {
        if (isHost) {
            serverThread = new ChatServerThread(port);
            serverThread.start();;

        }
        else {
            connectionThread = new ChatConnectionThread(ip, port);
            connectionThread.start();
        }
        base = new BorderPane();
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
    }

    public void send(String msg) {
        //add stuff here to send msg string to other client (sending it to their "recieve" method !!!!!!!!!!!!!!!!!!!!!!!
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
    //msg will need to be input from some socket/thread elsewhere (msg will come from other client's "send" method
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
    public void gameEvent(String msg) {
        Text message = new Text(msg);
        message.setFill(Color.BLUE);
        scrollBox.getChildren().add(message);
    }
}