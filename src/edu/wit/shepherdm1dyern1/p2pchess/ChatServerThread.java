package edu.wit.shepherdm1dyern1.p2pchess;

import edu.wit.shepherdm1dyern1.p2pchess.ChatNetwork;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServerThread extends Thread{
    int serverPort;
    public ChatConnectionThread connectionThread;

    public ChatServerThread(int port) {
        serverPort = port;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(serverPort);
            while (true) {
                Socket connection = serverSocket.accept();
                connectionThread = new ChatConnectionThread(connection);
                connectionThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public ChatConnectionThread getConnectionThread(){
        return connectionThread;
    }
}
