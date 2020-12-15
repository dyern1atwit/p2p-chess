package edu.wit.shepherdm1dyern1.p2pchess;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread extends Thread {
    int serverPort; //port of the thread
    private ConnectionThread connectionThread; //connection thread used to communicate as a p2p network

    //constructor that defines the server port as the given port
    public ServerThread(int port) {
        serverPort = port;
    }

    //main run loop for thread
    @SuppressWarnings("InfiniteLoopStatement")
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

    //returns the client thread that is created by this server thread
    public ConnectionThread getConnectionThread() {
        return connectionThread;
    }

}
