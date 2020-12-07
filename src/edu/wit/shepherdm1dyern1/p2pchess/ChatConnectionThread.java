package edu.wit.shepherdm1dyern1.p2pchess;

import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ChatConnectionThread extends Thread {
    public BufferedReader inFromPeer;
    public DataOutputStream outToPeer;

    public ChatConnectionThread(Socket socket) throws IOException {
        inFromPeer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        outToPeer = new DataOutputStream(socket.getOutputStream());
    }

    public ChatConnectionThread(String ip, int port) throws IOException {
        Socket connectionSocket = new Socket(ip, port);
        inFromPeer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        outToPeer = new DataOutputStream(connectionSocket.getOutputStream());
    }

    @Override
    public void run() {
        while (true){
            try {
                String msg = inFromPeer.readLine();
                sendToChat(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


    public void send(String s) throws IOException {
        outToPeer.writeBytes(s + "\r\n");
    }

    public void sendToChat(String msg) {
        Platform.runLater(new Runnable() {
            @Override public void run() {
                ChatWindow.thisChat.receive(msg, false);
            }
        });
    }
}
