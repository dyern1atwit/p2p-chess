package edu.wit.shepherdm1dyern1.p2pchess;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class Connection {
    public static class ConnectionThread extends Thread {
        BufferedReader inFromPeer;
        DataOutputStream outToPeer;
        int connectingID;
        int thisID;
        boolean isConnecting = false;

        public ConnectionThread(Socket socket) throws IOException {
            inFromPeer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outToPeer = new DataOutputStream(socket.getOutputStream());
        }

        public ConnectionThread(String ip, int port) throws IOException { ;
            Socket connectionSocket = new Socket(ip, port);
            inFromPeer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            outToPeer = new DataOutputStream(connectionSocket.getOutputStream());
        }

        @Override
        public void run() {
            try {
                Random gen = new Random();
                thisID = gen.nextInt(999999999-100000000+1)+100000000;
                if (isConnecting) {
                    String connectString = "CNTRQ: " + thisID + "\r\n";
                    System.out.println("out: " + connectString);
                    outToPeer.writeBytes(connectString);
                }
                while (true) {
                    String input = inFromPeer.readLine();
                    String output = inputProccessing(input);
                    System.out.println("out: " + output);
                    outToPeer.writeBytes(output);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void setAsConnector() {
            isConnecting = true;
        }

        public String inputProccessing(String inputString) {
            System.out.println("in: " + inputString);
            String errorArgs = "ERROR: INCORRECT NUM OF ARGS\r\n";
            String errorNotFound = "ERROR: INPUT NOT REGISTERED\r\n";
            String[] input = inputString.split(" ");
            if (input[0].equals("CNTRQ:")) {
                if (input.length == 2) {
                    connectingID = Integer.parseInt(input[1]);
                    return "ACKNL: " + thisID + "\r\n";
                } else return errorArgs;
            }
            else if (input[0].equals("ACKNL:")) return "";
            else if (input[0].equals("ERROR:")) return "";
            else return errorNotFound;
        }
    }

    public static class ServerThread extends Thread {
        int serverPort;

        public ServerThread(int port) {
            serverPort = port;
        }

        @Override
        public void run() {
            try {
                ServerSocket serverSocket = new ServerSocket(serverPort);
                while (true) {
                    Socket connection = serverSocket.accept();
                    ConnectionThread connectionThread = new ConnectionThread(connection);
                    connectionThread.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}