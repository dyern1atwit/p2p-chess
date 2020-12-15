package edu.wit.shepherdm1dyern1.p2pchess;

import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ChatConnectionThread extends Thread {
    public BufferedReader inFromPeer; //buffered reader from the chat socket
    public DataOutputStream outToPeer; //output stream from the chat socket
    public Socket connectionSocket; //chat socket

    //constructor to make a chat connection thread given a server socket
    public ChatConnectionThread(Socket socket) throws IOException {
        connectionSocket = socket;
        inFromPeer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        outToPeer = new DataOutputStream(connectionSocket.getOutputStream());
    }

    //overloaded constructor given an IP and a port rather than a socket, creates a socket instead
    public ChatConnectionThread(String ip, int port) throws IOException {
        connectionSocket = new Socket(ip, port);
        inFromPeer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        outToPeer = new DataOutputStream(connectionSocket.getOutputStream());
    }

    //main run method of thread
    @SuppressWarnings("InfiniteLoopStatement")
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

    //closes the connection socket
    public void closeConnection() throws IOException {
        connectionSocket.close();
    }

    //sends a given string over the connection socket
    public void send(String s) throws IOException {
        outToPeer.writeBytes(s + "\r\n");
    }

    //sends a given string to the chat window
    public void sendToChat(String msg) {
        Platform.runLater(() -> ChatWindow.thisChat.receive(msg, false));
    }
}
