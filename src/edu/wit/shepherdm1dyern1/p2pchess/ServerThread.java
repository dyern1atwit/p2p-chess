package edu.wit.shepherdm1dyern1.p2pchess;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread extends Thread {
    int serverPort;
    private ConnectionThread connectionThread;

    public ServerThread(int port) {
        serverPort = port;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(serverPort);
            while (true) {
                Socket connection = serverSocket.accept();
                connectionThread = new ConnectionThread(connection);
                connectionThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ConnectionThread getConnectionThread() {
        return connectionThread;
    }

}
