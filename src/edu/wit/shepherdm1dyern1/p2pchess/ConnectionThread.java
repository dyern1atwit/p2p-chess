package edu.wit.shepherdm1dyern1.p2pchess;

import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Random;

public class ConnectionThread extends Thread {
    BufferedReader inFromPeer; //buffered reader from connection chat socket
    DataOutputStream outToPeer; //output stream from connection chat socket
    Socket connectionSocket; //connection socket
    int connectingID; //ID of the connection thread
    int thisID; //ID of this connection thread
    boolean isConnecting = false; //boolean that says if the thread is connecting or not
    boolean isLive; //boolean that says if the thread is live or not

    public ConnectionThread(Socket socket) throws IOException {
        isLive = true;
        connectionSocket = socket;
        inFromPeer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        outToPeer = new DataOutputStream(connectionSocket.getOutputStream());
    }

    public ConnectionThread(String ip, int port) throws IOException {
        isLive = true;
        connectionSocket = new Socket(ip, port);
        inFromPeer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        outToPeer = new DataOutputStream(connectionSocket.getOutputStream());
    }

    //main run loop for thread
    @Override
    public void run() {
        try {
            Random gen = new Random();
            thisID = gen.nextInt(999999999-100000000+1)+100000000;
            if (isConnecting) {
                String connectString = "CNTRQ: " + thisID + "\r\n"; //starting message, connection request
                outToPeer.writeBytes(connectString);
            }
            while (isLive) {
                String input = inFromPeer.readLine();
                String output = inputProcessing(input);
                outToPeer.writeBytes(output);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //sets this thread as the connecting thread
    public void setAsConnector() {
        isConnecting = true;
    }

    //sends a message to the other player's network thread
    public void send(String s, boolean send) throws IOException {
        if (isLive) {
            outToPeer.writeBytes(s);
            if (send) outToPeer.writeBytes("\r\n");
        }
    }

    //method that closes the connection
    public void closeConnection() throws IOException {
        isLive = false;
        connectionSocket.close();
    }

    //method that takes in the message from the other player's network and gives a response to be sent back
    public String inputProcessing(String inputString) throws IOException {
        //System.out.println("in: " + inputString);
        String errorArgs = "ERROR: INCORRECT NUM OF ARGS\r\n";
        String errorNotFound = "ERROR: INPUT NOT REGISTERED\r\n";
        String[] input = inputString.split(" ");
        switch (input[0]) {
            case "CNTRQ:":
                if (input.length == 2) {
                    connectingID = Integer.parseInt(input[1]);
                    return "ACKNL: " + thisID + "\r\n";
                } else return errorArgs;
            case "MOVE:":
                if (input.length == 5) {
                    Platform.runLater(() -> {
                        Main.getGame().movePiece(Integer.parseInt(input[1]), Integer.parseInt(input[2]), Integer.parseInt(input[3]), Integer.parseInt(input[4]));
                        Main.getGame().switchTurn();
                    });
                    return "ACKNL: \"" + inputString + "\"\r\n";
                } else return errorArgs;
            case "FORF:":
                if (input.length == 2) {
                    Platform.runLater(() -> {
                        Main.chatWindow.gameEvent(input[1] + " forfeits!");
                        Main.getGame().endGame("win");
                    });
                    return "ACKNL: \"" + inputString + "\"\r\n";
                } else return errorArgs;
            case "TAKE:":
                if (input.length == 2) {
                    Platform.runLater(() -> Main.getGame().takeRemote(input[1].replace("_", " ")));
                    return "ACKNL: \"" + inputString + "\"\r\n";
                } else return errorArgs;
            case "QUIT:":
                if (input.length == 2) {
                    Platform.runLater(() -> Main.chatWindow.closeConnection());
                    closeConnection();
                    return "ACKNL: \"" + inputString + "\"\r\n";
                } else return errorArgs;
            case "ACKNL:":
            case "ERROR:":
                return "";
            default:
                return errorNotFound;
        }
    }
}
