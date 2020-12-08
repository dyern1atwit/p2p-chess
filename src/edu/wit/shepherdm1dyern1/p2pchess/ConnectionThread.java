package edu.wit.shepherdm1dyern1.p2pchess;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Random;

public class ConnectionThread extends Thread {
    BufferedReader inFromPeer;
    DataOutputStream outToPeer;
    Socket connectionSocket;
    int connectingID;
    int thisID;
    boolean isConnecting = false;
    boolean isLive;

    public ConnectionThread(Socket socket) throws IOException {
        isLive = true;
        connectionSocket = socket;
        inFromPeer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        outToPeer = new DataOutputStream(connectionSocket.getOutputStream());
    }

    public ConnectionThread(String ip, int port) throws IOException { ;
        isLive = true;
        connectionSocket = new Socket(ip, port);
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
                //System.out.println("out: " + connectString);
                outToPeer.writeBytes(connectString);
            }
            while (isLive) {
                //System.out.println("Waiting for signal...");
                String input = inFromPeer.readLine();
                String output = inputProccessing(input);
                //System.out.println("out: " + output);
                outToPeer.writeBytes(output);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setAsConnector() {
        isConnecting = true;
    }

    public void send(String s, boolean send) throws IOException {
        if (isLive) {
            outToPeer.writeBytes(s);
            if (send) outToPeer.writeBytes("\r\n");
        }
    }

    public void closeConnection() throws IOException {
        isLive = false;
        connectionSocket.close();
    }

    public String inputProccessing(String inputString) throws IOException {
        //System.out.println("in: " + inputString);
        String errorArgs = "ERROR: INCORRECT NUM OF ARGS\r\n";
        String errorNotFound = "ERROR: INPUT NOT REGISTERED\r\n";
        String[] input = inputString.split(" ");
        if (input[0].equals("CNTRQ:")) {
            if (input.length == 2) {
                connectingID = Integer.parseInt(input[1]);
                return "ACKNL: " + thisID + "\r\n";
            } else return errorArgs;
        }
        else if (input[0].equals("MOVE:")) {
            if (input.length == 5) {
                Platform.runLater(new Runnable() {
                    @Override public void run() {
                        Main.getGame().movePiece(Integer.parseInt(input[1]), Integer.parseInt(input[2]), Integer.parseInt(input[3]), Integer.parseInt(input[4]));
                        Main.getGame().switchTurn();
                    }
                });
                return "ACKNL: \"" + inputString + "\"\r\n";
            } else return errorArgs;
        }
        else if (input[0].equals("FORF:")) {
            if (input.length == 2) {
                Platform.runLater(new Runnable() {
                    @Override public void run() {
                        Main.chatWindow.gameEvent(input[1] + " forfeits!");
                        Main.getGame().endGame("win");
                    }
                });
                return "ACKNL: \"" + inputString + "\"\r\n";
            } else return errorArgs;
        }
        else if (input[0].equals("TAKE:")){
            if (input.length == 2) {
                Platform.runLater(new Runnable() {
                    @Override public void run() {
                        Main.getGame().takeRemote(input[1].replace("_", " "));
                    }
                });
                return "ACKNL: \"" + inputString + "\"\r\n";
            } else return errorArgs;
        }
        else if (input[0].equals("QUIT:")){
            if (input.length == 2) {
                Platform.runLater(new Runnable() {
                    @Override public void run() {
                        Main.chatWindow.closeConnection();
                    }
                });
                closeConnection();
                return "ACKNL: \"" + inputString + "\"\r\n";
            } else return errorArgs;
        }
        else if (input[0].equals("ACKNL:")) return "";
        else if (input[0].equals("ERROR:")) return "";
        else return errorNotFound;
    }
}
