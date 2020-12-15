package edu.wit.shepherdm1dyern1.p2pchess;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServerThread extends Thread{
    int serverPort; //port of the chat thread
    public ChatConnectionThread connectionThread; //connection thread used to communicate as a p2p network

    //constructor that defines the server port as the given port
    public ChatServerThread(int port) {
        serverPort = port;
    }

    //main run loop of thread
    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(serverPort); //create a server socket
            while (true) {
                Socket connection = serverSocket.accept(); //wait for incoming connection
                connectionThread = new ChatConnectionThread(connection); //create a chat client thread that is linked to the server socket
                connectionThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //returns the client thread that is created by this server thread
    public ChatConnectionThread getConnectionThread(){
        return connectionThread;
    }
}
