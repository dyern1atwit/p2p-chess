package edu.wit.shepherdm1dyern1.p2pchess;

import javafx.scene.image.*;
import javafx.application.*;
import javafx.collections.*;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.*;

import java.io.*;
import java.util.ArrayList;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.bidimap.*;


public class Main extends Application {
    private static Main mainGame; //instance of main that is used for interfacing between the network and the game
    public static GridPane boardGrid = new GridPane(); //main chess board
    public static Stage stage; //current stage of the window
    public Scene chessBoard; //scene that boardGrid sits on top of
    public Scene menu; //main menu scene
    public Scene gameOverScene; //game over scene
    public Node selectedNode; //selected node from handleClickEvent
    public int port; //port used to host the game
    public static BidiMap<String, Node> blackSprites; //map of all of the black sprites
    public static BidiMap<String, Node> whiteSprites; //map of all of the white sprites
    public static String playerColor; //current player color
    public static String turn = "white"; //current player's turn (always white at the beginning of a game)
    public ArrayList<int[]> currentValid; //array of the current valid moves
    public static ConnectionThread clientConnection; //connection thread of the client connection
    public ServerThread serverConnection; //connection thread of the server connection
    public boolean isHost; //boolean to show if client is host or not
    public static ChatWindow chatWindow; //javafx application window for chat

    @Override
    public void start(Stage primaryStage){
        stage = primaryStage;
        startGame();
    }

    //The following section defines methods to start the game and ccreate winodws

    //cleans up public variables and resets game
    public void startGame() {
        this.menu = createMenu();
        this.chessBoard = createChessBoard();
        stage.setTitle("Chess");
        stage.setScene(menu);
        stage.setResizable(false);
        stage.show();
    }

    //cleans up before restart
    public void restart(){
        boardGrid = new GridPane();
        chessBoard=null;
        menu=null;
        gameOverScene=null;
        selectedNode=null;
        blackSprites=null;
        whiteSprites=null;
        playerColor=null;
        turn = "white";
        currentValid=null;
        startGame();
    }

    //takes port and game scene, will be used later to handle creating new game on given port
    public void createGame (TextField port, Scene game) throws IOException {
        playerColor = "white";
        this.isHost = true;
        stage.setScene(game);
        this.port = Integer.parseInt(port.getText());
        chatWindow = new ChatWindow("localhost", this.port, true);
        serverConnection = new ServerThread(this.port);
        serverConnection.start();
        System.out.println("white, host");
        System.out.println(port.getText());
        port.clear();
    }

    //takes ip and game scene, will be used later to handle connecting to existing game
    public void joinGame (TextField ip, Scene game) throws IOException {
        playerColor = "black";
        this.isHost = false;
        boardGrid.setRotate(180);
        for (String key : blackSprites.keySet()) {
            blackSprites.get(key).setRotate(180);
        }
        for (String key : whiteSprites.keySet()) {
            whiteSprites.get(key).setRotate(180);
        }
        stage.setScene(game);
        String connectingIP = ip.getText().split(":")[0];
        int connectingPort = Integer.parseInt(ip.getText().split(":")[1]);
        chatWindow = new ChatWindow(connectingIP, connectingPort, false);
        clientConnection = new ConnectionThread(connectingIP, connectingPort);
        clientConnection.setAsConnector();
        clientConnection.start();
        System.out.println("black, client");
        System.out.println(ip.getText());
        ip.clear();
    }

    //The following section defines helper methods to help with the game logic overall

    //gets position of node given node
    public int[] getGridPos (Node node, GridPane grid) {
        int[] pos = new int[2];
        if (node != grid) {
            if(GridPane.getRowIndex(node.getParent())!=null){
                pos[1] = GridPane.getRowIndex(node.getParent());
                pos[0] = GridPane.getColumnIndex(node.getParent());
            }
            else{
                pos[1] = GridPane.getRowIndex(node.getParent().getParent());
                pos[0] = GridPane.getColumnIndex(node.getParent().getParent());
            }
        }
        return pos;
    }

    //gets node given row and column values
    public Node getNode (int column, int row, GridPane grid) {
        ObservableList<Node> gridNodes = grid.getChildren();
        for (Node node : gridNodes) {
            if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == column) {
                return node;
            }
        }
        return null;
    }

    //gets node given mouseclick event
    public Node getNode (MouseEvent event) {
        return event.getPickResult().getIntersectedNode();
    }

    //helper method to handle click events on nodes
    public void handleClickEvent(MouseEvent event) { //WILL NEED REWORK FOR PIECE OBJECTS - ONLY TESTING SELECTING/MOVING NODES
        Node clickedNode = getNode(event);

        if (playerColor.equals("black") && blackSprites.containsValue(clickedNode.getParent()) && turn.equals("black")){
            if (this.selectedNode==null){
                greenBorder(clickedNode);
                this.selectedNode = clickedNode;
                getMoves(this.selectedNode.getParent(), "black");
            }
            else if (clickedNode == this.selectedNode) {
                blackBorders();
                this.selectedNode = null;
            }
        }
        else if(playerColor.equals("white") && whiteSprites.containsValue(clickedNode.getParent()) && turn.equals("white")) {
            if (this.selectedNode==null){
                greenBorder(clickedNode);
                this.selectedNode = clickedNode;
                getMoves(this.selectedNode.getParent(), "white");
            }
            else if (clickedNode == this.selectedNode) {
                blackBorders();
                this.selectedNode = null;
            }
        }
        else if (this.selectedNode!=null&&(checkFor(getGridPos(clickedNode,boardGrid)))){
            blackBorders();
            movePiece(this.selectedNode, clickedNode);
        }
        else{
            if ((playerColor.equals("black") && whiteSprites.containsValue(clickedNode.getParent()))||(playerColor.equals("white") && blackSprites.containsValue(clickedNode.getParent()))){
                System.out.println("Not your Piece");
            }
        }
    }

    //checks current valid for coords
    public boolean checkFor(int[] coords){
        for(int[] i:currentValid){
            if(i[0]==coords[0]&&i[1]==coords[1]){
                return true;
            }
        }
        return false;
    }

    //sets border of given node to green if not already and black if green already
    public void greenBorder(Node node){
        Shape shape = (Shape) node;
        if(shape.getStroke()==Color.GREEN){
                shape.setStroke(Color.rgb(20, 20, 20, 1));
        }
        else {
            shape.setStroke(Color.GREEN);
        }
    }

    //sets border of given node to red if not already and black if red already
    public void redBorder(Node node){
        Shape shape = (Shape) node;
        if(shape.getStroke()==Color.RED){
            shape.setStroke(Color.rgb(20, 20, 20, 1));
        }
        else {
            shape.setStroke(Color.RED);
        }
    }

    //resets all borders to black
    public void blackBorders(){
        for(int i=0;i<8;i++){
            for(int j=0;j<8;j++){
                StackPane stack = (StackPane) getNode(i,j,boardGrid);
                Shape shape = (Shape) stack.getChildren().get(0);
                shape.setStroke(Color.rgb(20, 20, 20, 1));
                if(stack.getChildren().size()>1){
                    stack = (StackPane) stack.getChildren().get(1);
                    shape = (Shape) stack.getChildren().get(1);
                    shape.setStroke(Color.rgb(20, 20, 20, 1));
                }

            }
        }
    }

    //The following section defines piece movement and logic

    //moves a node given piece (node being moved) and destination node (space/node clicked on) - switches turn after move
    public void movePiece(Node piece, Node dest){           //WILL NEED CONTINUOUS REWORK
        //chatWindow.gameEvent(toChessMoves(piece.getParent(), dest));
        String chessMove = toChessMoves(piece.getParent(), dest);
        StringBuilder s = new StringBuilder("MOVE: ");
        if (turn.equals(playerColor)){
            for (int i : getGridPos(piece, boardGrid)) s.append(i).append(" ");
            for (int i : getGridPos(dest, boardGrid)) s.append(i).append(" ");
            System.out.println(s);
        }
        piece = piece.getParent();

        StackPane destStack = (StackPane) dest.getParent();
        StackPane finalDest = destStack;

        boolean taken = false;
        String takeName = null;
        if((!(blackSprites.getKey(destStack)==null)&&playerColor.equals("white"))||(!(whiteSprites.getKey(destStack)==null)&&playerColor.equals("black"))){
            finalDest = (StackPane) destStack.getParent();
            takeName = takePiece(destStack);
            taken = true;
        }

        StackPane sourceStack = (StackPane) piece.getParent();

        sourceStack.getChildren().remove(piece);
        finalDest.getChildren().add(piece);

        StackPane whiteKing = (StackPane) whiteSprites.get("White King");
        StackPane blackKing = (StackPane) blackSprites.get("Black King");
        System.out.println(whiteKing + " " + blackKing);

        boolean check;
        if(playerColor.equals("white")){
            check = checkCheck(getGridPos(whiteKing.getChildren().get(0), boardGrid), "white");
        }
        else{
            check = checkCheck(getGridPos(blackKing.getChildren().get(0), boardGrid), "black");
        }

        if(check){
            chatWindow.gameEvent("Cannot move into check or leave king in check");
            sourceStack.getChildren().add(piece);
            finalDest.getChildren().remove(piece);
            if(taken){
                finalDest.getChildren().add(destStack);
                if(takeName.startsWith("Black")){
                    blackSprites.put(takeName, destStack);
                }
                else{
                    whiteSprites.put(takeName, destStack);
                }
            }
            this.selectedNode = null;
        }
        else{
            chatWindow.gameEvent(chessMove);
            if(taken){
                chatWindow.gameEvent("Took "+ takeName.split("_")[0] + " " + takeName.split("_")[1]);
            }
            this.selectedNode = null;
            if (turn.equals(playerColor)) {
                if (isHost) {
                    try {
                        this.serverConnection.getConnectionThread().send(s.toString(), true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    try {
                        clientConnection.send(s.toString(), true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            switchTurn();
        }
    }

    //moves a node given starting coordinates and ending coordinates
    public void movePiece(int x1, int y1, int x2, int y2) {

        StackPane sourceStack = (StackPane) getNode(x1, y1, boardGrid);
        StackPane destStack = (StackPane) getNode(x2, y2, boardGrid);
        StackPane piece = (StackPane) sourceStack.getChildren().get(1);
        chatWindow.gameEvent(toChessMoves(piece, new int[] {x1, y1}, new int[] {x2, y2}));
        if(destStack.getChildren().size()>1){
            chatWindow.gameEvent("Took "+getPieceName(destStack.getChildren().get(1)));
            takePiece(destStack.getChildren().get(1));
        }
        sourceStack.getChildren().remove(sourceStack.getChildren().get(1));
        destStack.getChildren().add(piece);
    }

    //method to take a piece, takes piece off the board, removes from "sprites" bidimap and adds to "taken" bidimap
    public String takePiece(Node taken){
        System.out.println(taken.getParent().getParent().getClass().getSimpleName());
        /*
        if (whiteSprites.containsValue(taken)) {
            this.whiteTaken.put(whiteSprites.getKey(taken), taken);
        }
        if (blackSprites.containsValue(taken)) {
            this.blackTaken.put(blackSprites.getKey(taken), taken);
        }
        */
        StackPane takenPane = (StackPane) taken.getParent();
        takenPane.getChildren().remove(taken);
        String s = null;
        if(blackSprites.containsValue(taken)){
            s = blackSprites.getKey(taken).replace(" ", "_");
            blackSprites.remove(blackSprites.getKey(taken), taken);
        }
        else if(whiteSprites.containsValue(taken)){
            s = whiteSprites.getKey(taken).replace(" ", "_");
            whiteSprites.remove(whiteSprites.getKey(taken), taken);
        }
        return s;
    }

    //method used to take a piece when called by the network rather than the client
    @SuppressWarnings("SuspiciousMethodCalls")
    public void takeRemote(String taken){
        if(blackSprites.containsValue(taken)){
            blackSprites.remove(blackSprites.getKey(taken), taken);
        }
        else if(whiteSprites.containsValue(taken)){
            whiteSprites.remove(whiteSprites.getKey(taken), taken);
        }
    }

    //outputs a string that represents where a piece moved to given the piece and the destination node (for example, "White Pawn c2 to c4")
    public String toChessMoves(Node piece, Node dest){
        String[] letters = new String[] {"a","b","c","d","e","f","g","h"};
        String s = "";
        s += getPieceName(piece) + " ";

        int[] piecePos = getGridPos(piece, boardGrid);
        int[] destPos = getGridPos(dest, boardGrid);
        //String piecePos = "";
        s += letters[piecePos[0]];
        s += (8-piecePos[1])+" to ";
        s += letters[destPos[0]];
        s += (8-destPos[1]);

        return s;
    }

    //overloaded method, uses piece and the starting position and ending position rather than the destination node
    public String toChessMoves(Node piece, int[] piecePos, int[] destPos){
        String[] letters = new String[] {"a","b","c","d","e","f","g","h"};
        String s = "";
        s += getPieceName(piece) + " ";

        //String piecePos = "";
        s += letters[piecePos[0]]+(8-piecePos[1])+" to ";
        s += letters[destPos[0]]+(8-destPos[1]);
        return s;
    }

    //gets the name of a piece given a node (e.g. "White Pawn")
    public String getPieceName(Node piece){
        String s = "";
        String pieceName;
        if (whiteSprites.getKey(piece) != null) {
            pieceName = whiteSprites.getKey(piece);
        } else {
            pieceName = blackSprites.getKey(piece);
        }
        s += pieceName.split(" ")[0]+" ";
        s += pieceName.split(" ")[1];
        return s;

    }

    //method that determines where a clicked piece can move and highlights those spaces
    public void getMoves(Node piece, String player) {

        String pieceName;

        if (whiteSprites.getKey(piece) != null) {
            pieceName = whiteSprites.getKey(piece);
        } else {
            pieceName = blackSprites.getKey(piece);
        }
        String nameStart = pieceName.substring(6, 8);
        switch (nameStart) {
            case "Pa":
                currentValid = pawnMoves(piece, player);
                break;
            case "Ro":
                currentValid = rookMoves(piece);
                break;
            case "Qu":
                currentValid = queenMoves(piece);
                break;
            case "Ki":
                currentValid = kingMoves(piece);
                break;
            case "Bi":
                currentValid = bishopMoves(piece);
                break;
            case "Kn":
                currentValid = knightMoves(piece);
                break;
        }
    }

    //gets the moves for a given piece and checks each move to see if it is valid or not
    public ArrayList<int[]> getMovesCheck(Node piece, String player) {

        String pieceName;

        ArrayList<int[]> valid = new ArrayList<>();


        if (whiteSprites.getKey(piece) != null) {
            pieceName = whiteSprites.getKey(piece);
        } else {
            pieceName = blackSprites.getKey(piece);
        }
        String nameStart = pieceName.substring(6, 8);
        switch (nameStart) {
            case "Pa":
                valid = pawnMoves(piece, player);
                break;
            case "Ro":
                valid = rookMoves(piece);
                break;
            case "Qu":
                valid = queenMoves(piece);
                break;
            case "Ki":
                valid = kingMoves(piece);
                break;
            case "Bi":
                valid = bishopMoves(piece);
                break;
            case "Kn":
                valid = knightMoves(piece);
                break;
        }
        return valid;
    }

    //helper method to find if a piece can take another piece
    public boolean canTake(Node taker, Node taken){
        return (whiteSprites.containsValue(taker) && blackSprites.containsValue(taken)) || (blackSprites.containsValue(taker) && whiteSprites.containsValue(taken));
    }

    //helper method to swap which player's turn it is
    public void switchTurn(){
        if(turn.equals("white")){
            turn="black";
        }
        else{
            turn="white";
        }
        System.out.println("switched turn to "+turn);
    }

    //The following section defines piece movement

    //pawn movement method
    public ArrayList<int[]> pawnMoves(Node pawn, String player){
        int[] pos = new int[]{getGridPos(pawn, boardGrid)[0], getGridPos(pawn, boardGrid)[1]};

        ArrayList<int[]> valid = new ArrayList<>();

        if(player.equals("white")){
            int spaces;
            if(pos[1]==6){
                spaces = 2;
            }
            else{
                spaces = 1;
            }
            for(int i=0; i<spaces;i++){
                pos[1]--;
                if(pos[1]>=0){
                    StackPane stack = (StackPane) getNode(pos[0], pos[1], boardGrid);
                    if(!(stack.getChildren().size()>1)){
                        valid.add(new int[]{pos[0], pos[1]});
                        greenBorder(stack.getChildren().get(0));
                    }
                    else{
                        i++;
                    }
                    if((pos[0]-1)>=0){
                        stack = (StackPane) getNode(pos[0]-1, pos[1], boardGrid);
                    }
                    if(stack.getChildren().size()>1&&canTake(pawn, stack.getChildren().get(1))){
                        valid.add(new int[]{pos[0]-1, pos[1]});
                        StackPane taking = (StackPane) stack.getChildren().get(1);
                        redBorder(taking.getChildren().get(1));
                    }
                    if((pos[0]+1)<=7){
                        stack = (StackPane) getNode(pos[0]+1, pos[1], boardGrid);
                    }
                    if(stack.getChildren().size()>1&&canTake(pawn, stack.getChildren().get(1))){
                        valid.add(new int[]{pos[0]+1, pos[1]});
                        StackPane taking = (StackPane) stack.getChildren().get(1);
                        redBorder(taking.getChildren().get(1));
                    }
                }
                else {
                    break;
                }
            }
        }
        else{
            int spaces;
            if(pos[1]==1){
                spaces = 2;
            }
            else{
                spaces = 1;
            }
            for(int i=0; i<spaces;i++){
                pos[1]++;
                if(pos[1]<=7){
                    StackPane stack = (StackPane) getNode(pos[0], pos[1], boardGrid);
                    if(!(stack.getChildren().size()>1)){
                        valid.add(new int[]{pos[0], pos[1]});
                        greenBorder(stack.getChildren().get(0));
                    }
                    else{
                        i++;
                    }
                    if((pos[0]-1)>=0){
                        stack = (StackPane) getNode(pos[0]-1, pos[1], boardGrid);
                    }
                    if(stack.getChildren().size()>1&&canTake(pawn, stack.getChildren().get(1))){
                        valid.add(new int[]{pos[0]-1, pos[1]});
                        StackPane taking = (StackPane) stack.getChildren().get(1);
                        redBorder(taking.getChildren().get(1));
                    }
                    if((pos[0]+1)<=7){
                        stack = (StackPane) getNode(pos[0]+1, pos[1], boardGrid);
                    }
                    if(stack.getChildren().size()>1&&canTake(pawn, stack.getChildren().get(1))){
                        valid.add(new int[]{pos[0]+1, pos[1]});
                        StackPane taking = (StackPane) stack.getChildren().get(1);
                        redBorder(taking.getChildren().get(1));
                    }
                }
                else {
                    break;
                }
            }
        }
        return valid;
    }

    //bishop movement method
    public ArrayList<int[]> bishopMoves(Node bishop){
        ArrayList<int[]> valid = new ArrayList<>();
        int[] pos = new int[]{getGridPos(bishop, boardGrid)[0], getGridPos(bishop, boardGrid)[1]};
        int[] tmpPos = new int[]{getGridPos(bishop, boardGrid)[0], getGridPos(bishop, boardGrid)[1]};
        StackPane stack;
        StackPane taking;

        while(tmpPos[0]+1>=0&&tmpPos[0]+1<=7&&tmpPos[1]+1>=0&&tmpPos[1]+1<=7){
            tmpPos[0]++; tmpPos[1]++;
            stack = (StackPane) getNode(tmpPos[0], tmpPos[1], boardGrid);
            if(stack.getChildren().size()>1){
                taking = (StackPane) stack.getChildren().get(1);
                if(canTake(bishop, taking)){
                    redBorder(taking.getChildren().get(1));
                    valid.add(new int[]{tmpPos[0], tmpPos[1]});
                }
                break;
            }
            greenBorder(stack.getChildren().get(0));
            valid.add(new int[]{tmpPos[0], tmpPos[1]});
        }
        tmpPos[0]=pos[0];tmpPos[1]=pos[1];
        while(tmpPos[0]+1>=0&&tmpPos[0]+1<=7&&tmpPos[1]-1>=0&&tmpPos[1]-1<=7){
            tmpPos[0]++; tmpPos[1]--;
            stack = (StackPane) getNode(tmpPos[0], tmpPos[1], boardGrid);
            if(stack.getChildren().size()>1){
                taking = (StackPane) stack.getChildren().get(1);
                if(canTake(bishop, taking)){
                    redBorder(taking.getChildren().get(1));
                    valid.add(new int[]{tmpPos[0], tmpPos[1]});
                }
                break;
            }
            greenBorder(stack.getChildren().get(0));
            valid.add(new int[]{tmpPos[0], tmpPos[1]});
        }
        tmpPos[0]=pos[0];tmpPos[1]=pos[1];
        while(tmpPos[0]-1>=0&&tmpPos[0]-1<=7&&tmpPos[1]-1>=0&&tmpPos[1]-1<=7){
            tmpPos[0]--; tmpPos[1]--;
            stack = (StackPane) getNode(tmpPos[0], tmpPos[1], boardGrid);
            if(stack.getChildren().size()>1){
                taking = (StackPane) stack.getChildren().get(1);
                if(canTake(bishop, taking)){
                    redBorder(taking.getChildren().get(1));
                    valid.add(new int[]{tmpPos[0], tmpPos[1]});
                }
                break;
            }
            greenBorder(stack.getChildren().get(0));
            valid.add(new int[]{tmpPos[0], tmpPos[1]});
        }
        tmpPos[0]=pos[0];tmpPos[1]=pos[1];
        while(tmpPos[0]-1>=0&&tmpPos[0]-1<=7&&tmpPos[1]+1>=0&&tmpPos[1]+1<=7){
            tmpPos[0]--; tmpPos[1]++;
            stack = (StackPane) getNode(tmpPos[0], tmpPos[1], boardGrid);
            if(stack.getChildren().size()>1){
                taking = (StackPane) stack.getChildren().get(1);
                if(canTake(bishop, taking)){
                    redBorder(taking.getChildren().get(1));
                    valid.add(new int[]{tmpPos[0], tmpPos[1]});
                }
                break;
            }
            greenBorder(stack.getChildren().get(0));
            valid.add(new int[]{tmpPos[0], tmpPos[1]});
        }
        return valid;
    }

    //rook movement method
    public ArrayList<int[]> rookMoves(Node rook){
        ArrayList<int[]> valid = new ArrayList<>();
        int[] pos = new int[]{getGridPos(rook, boardGrid)[0], getGridPos(rook, boardGrid)[1]};
        int[] tmpPos = new int[]{getGridPos(rook, boardGrid)[0], getGridPos(rook, boardGrid)[1]};
        StackPane stack;
        StackPane taking;

        tmpPos[0]=pos[0];tmpPos[1]=pos[1];
        while(tmpPos[0]+1>=0&&tmpPos[0]+1<=7){
            tmpPos[0]++;
            stack = (StackPane) getNode(tmpPos[0], tmpPos[1], boardGrid);
            if(stack.getChildren().size()>1){
                taking = (StackPane) stack.getChildren().get(1);
                if(canTake(rook, taking)){
                    redBorder(taking.getChildren().get(1));
                    valid.add(new int[]{tmpPos[0], tmpPos[1]});
                }
                break;
            }
            greenBorder(stack.getChildren().get(0));
            valid.add(new int[]{tmpPos[0], tmpPos[1]});
        }
        tmpPos[0]=pos[0];tmpPos[1]=pos[1];
        while(tmpPos[0]-1>=0&&tmpPos[0]-1<=7){
            tmpPos[0]--;
            stack = (StackPane) getNode(tmpPos[0], tmpPos[1], boardGrid);
            if(stack.getChildren().size()>1){
                taking = (StackPane) stack.getChildren().get(1);
                if(canTake(rook, taking)){
                    redBorder(taking.getChildren().get(1));
                    valid.add(new int[]{tmpPos[0], tmpPos[1]});
                }
                break;
            }
            greenBorder(stack.getChildren().get(0));
            valid.add(new int[]{tmpPos[0], tmpPos[1]});
        }
        tmpPos[0]=pos[0];tmpPos[1]=pos[1];
        while(tmpPos[1]+1>=0&&tmpPos[1]+1<=7){
            tmpPos[1]++;
            stack = (StackPane) getNode(tmpPos[0], tmpPos[1], boardGrid);
            if(stack.getChildren().size()>1){
                taking = (StackPane) stack.getChildren().get(1);
                if(canTake(rook, taking)){
                    redBorder(taking.getChildren().get(1));
                    valid.add(new int[]{tmpPos[0], tmpPos[1]});
                }
                break;
            }
            greenBorder(stack.getChildren().get(0));
            valid.add(new int[]{tmpPos[0], tmpPos[1]});
        }
        tmpPos[0]=pos[0];tmpPos[1]=pos[1];
        while(tmpPos[1]-1>=0&&tmpPos[1]-1<=7){
            tmpPos[1]--;
            stack = (StackPane) getNode(tmpPos[0], tmpPos[1], boardGrid);
            if(stack.getChildren().size()>1){
                taking = (StackPane) stack.getChildren().get(1);
                if(canTake(rook, taking)){
                    redBorder(taking.getChildren().get(1));
                    valid.add(new int[]{tmpPos[0], tmpPos[1]});
                }
                break;
            }
            greenBorder(stack.getChildren().get(0));
            valid.add(new int[]{tmpPos[0], tmpPos[1]});
        }

        return valid;
    }

    //queen movement method
    public ArrayList<int[]> queenMoves(Node queen){
        ArrayList<int[]> valid = new ArrayList<>();
        int[] pos = new int[]{getGridPos(queen, boardGrid)[0], getGridPos(queen, boardGrid)[1]};
        int[] tmpPos = new int[]{getGridPos(queen, boardGrid)[0], getGridPos(queen, boardGrid)[1]};
        StackPane stack;
        StackPane taking;
        //diagonals (stolen from bishop)
        while(tmpPos[0]+1>=0&&tmpPos[0]+1<=7&&tmpPos[1]+1>=0&&tmpPos[1]+1<=7){
            tmpPos[0]++; tmpPos[1]++;
            stack = (StackPane) getNode(tmpPos[0], tmpPos[1], boardGrid);
            if(stack.getChildren().size()>1){
                taking = (StackPane) stack.getChildren().get(1);
                if(canTake(queen, taking)){
                    redBorder(taking.getChildren().get(1));
                    valid.add(new int[]{tmpPos[0], tmpPos[1]});
                }
                break;
            }
            greenBorder(stack.getChildren().get(0));
            valid.add(new int[]{tmpPos[0], tmpPos[1]});
        }
        tmpPos[0]=pos[0];tmpPos[1]=pos[1];
        while(tmpPos[0]+1>=0&&tmpPos[0]+1<=7&&tmpPos[1]-1>=0&&tmpPos[1]-1<=7){
            tmpPos[0]++; tmpPos[1]--;
            stack = (StackPane) getNode(tmpPos[0], tmpPos[1], boardGrid);
            if(stack.getChildren().size()>1){
                taking = (StackPane) stack.getChildren().get(1);
                if(canTake(queen, taking)){
                    redBorder(taking.getChildren().get(1));
                    valid.add(new int[]{tmpPos[0], tmpPos[1]});
                }
                break;
            }
            greenBorder(stack.getChildren().get(0));
            valid.add(new int[]{tmpPos[0], tmpPos[1]});
        }
        tmpPos[0]=pos[0];tmpPos[1]=pos[1];
        while(tmpPos[0]-1>=0&&tmpPos[0]-1<=7&&tmpPos[1]-1>=0&&tmpPos[1]-1<=7){
            tmpPos[0]--; tmpPos[1]--;
            stack = (StackPane) getNode(tmpPos[0], tmpPos[1], boardGrid);
            if(stack.getChildren().size()>1){
                taking = (StackPane) stack.getChildren().get(1);
                if(canTake(queen, taking)){
                    redBorder(taking.getChildren().get(1));
                    valid.add(new int[]{tmpPos[0], tmpPos[1]});
                }
                break;
            }
            greenBorder(stack.getChildren().get(0));
            valid.add(new int[]{tmpPos[0], tmpPos[1]});
        }
        tmpPos[0]=pos[0];tmpPos[1]=pos[1];
        while(tmpPos[0]-1>=0&&tmpPos[0]-1<=7&&tmpPos[1]+1>=0&&tmpPos[1]+1<=7){
            tmpPos[0]--; tmpPos[1]++;
            stack = (StackPane) getNode(tmpPos[0], tmpPos[1], boardGrid);
            if(stack.getChildren().size()>1){
                taking = (StackPane) stack.getChildren().get(1);
                if(canTake(queen, taking)){
                    redBorder(taking.getChildren().get(1));
                    valid.add(new int[]{tmpPos[0], tmpPos[1]});
                }
                break;
            }
            greenBorder(stack.getChildren().get(0));
            valid.add(new int[]{tmpPos[0], tmpPos[1]});
        }
        //straight (stolen from rook)
        tmpPos[0]=pos[0];tmpPos[1]=pos[1];
        while(tmpPos[0]+1>=0&&tmpPos[0]+1<=7){
            tmpPos[0]++;
            stack = (StackPane) getNode(tmpPos[0], tmpPos[1], boardGrid);
            if(stack.getChildren().size()>1){
                taking = (StackPane) stack.getChildren().get(1);
                if(canTake(queen, taking)){
                    redBorder(taking.getChildren().get(1));
                    valid.add(new int[]{tmpPos[0], tmpPos[1]});
                }
                break;
            }
            greenBorder(stack.getChildren().get(0));
            valid.add(new int[]{tmpPos[0], tmpPos[1]});
        }
        tmpPos[0]=pos[0];tmpPos[1]=pos[1];
        while(tmpPos[0]-1>=0&&tmpPos[0]-1<=7){
            tmpPos[0]--;
            stack = (StackPane) getNode(tmpPos[0], tmpPos[1], boardGrid);
            if(stack.getChildren().size()>1){
                taking = (StackPane) stack.getChildren().get(1);
                if(canTake(queen, taking)){
                    redBorder(taking.getChildren().get(1));
                    valid.add(new int[]{tmpPos[0], tmpPos[1]});
                }
                break;
            }
            greenBorder(stack.getChildren().get(0));
            valid.add(new int[]{tmpPos[0], tmpPos[1]});
        }
        tmpPos[0]=pos[0];tmpPos[1]=pos[1];
        while(tmpPos[1]+1>=0&&tmpPos[1]+1<=7){
            tmpPos[1]++;
            stack = (StackPane) getNode(tmpPos[0], tmpPos[1], boardGrid);
            if(stack.getChildren().size()>1){
                taking = (StackPane) stack.getChildren().get(1);
                if(canTake(queen, taking)){
                    redBorder(taking.getChildren().get(1));
                    valid.add(new int[]{tmpPos[0], tmpPos[1]});
                }
                break;
            }
            greenBorder(stack.getChildren().get(0));
            valid.add(new int[]{tmpPos[0], tmpPos[1]});
        }
        tmpPos[0]=pos[0];tmpPos[1]=pos[1];
        while(tmpPos[1]-1>=0&&tmpPos[1]-1<=7){
            tmpPos[1]--;
            stack = (StackPane) getNode(tmpPos[0], tmpPos[1], boardGrid);
            if(stack.getChildren().size()>1){
                taking = (StackPane) stack.getChildren().get(1);
                if(canTake(queen, taking)){
                    redBorder(taking.getChildren().get(1));
                    valid.add(new int[]{tmpPos[0], tmpPos[1]});
                }
                break;
            }
            greenBorder(stack.getChildren().get(0));
            valid.add(new int[]{tmpPos[0], tmpPos[1]});
        }

        return valid;
    }

    //knight movement method
    public ArrayList<int[]> knightMoves(Node knight){
        ArrayList<int[]> valid = new ArrayList<>();
        int[] pos = new int[]{getGridPos(knight, boardGrid)[0], getGridPos(knight, boardGrid)[1]};
        int[] tmpPos = new int[]{getGridPos(knight, boardGrid)[0], getGridPos(knight, boardGrid)[1]};
        StackPane stack;
        StackPane taking;

        if(tmpPos[0]+2<=7){
            tmpPos[0]+=2;
            if((tmpPos[1]+1)<=7){
                tmpPos[1]++;
                stack = (StackPane) getNode(tmpPos[0], tmpPos[1], boardGrid);
                if(stack.getChildren().size()>1){
                    taking = (StackPane) stack.getChildren().get(1);
                    if(canTake(knight, taking)){
                        redBorder(taking.getChildren().get(1));
                        valid.add(new int[]{tmpPos[0], tmpPos[1]});
                    }
                }
                greenBorder(stack.getChildren().get(0));
                valid.add(new int[]{tmpPos[0], tmpPos[1]});
            }
            tmpPos[1]=pos[1];
            if((tmpPos[1]-1)>=0){
                tmpPos[1]--;
                stack = (StackPane) getNode(tmpPos[0], tmpPos[1], boardGrid);
                if(stack.getChildren().size()>1){
                    taking = (StackPane) stack.getChildren().get(1);
                    if(canTake(knight, taking)){
                        redBorder(taking.getChildren().get(1));
                        valid.add(new int[]{tmpPos[0], tmpPos[1]});
                    }
                }
                greenBorder(stack.getChildren().get(0));
                valid.add(new int[]{tmpPos[0], tmpPos[1]});
            }
        }
        tmpPos[0]=pos[0]; tmpPos[1]=pos[1];
        if(tmpPos[0]-2>=0){
            tmpPos[0]-=2;
            if((tmpPos[1]+1)<=7){
                tmpPos[1]++;
                stack = (StackPane) getNode(tmpPos[0], tmpPos[1], boardGrid);
                if(stack.getChildren().size()>1){
                    taking = (StackPane) stack.getChildren().get(1);
                    if(canTake(knight, taking)){
                        redBorder(taking.getChildren().get(1));
                        valid.add(new int[]{tmpPos[0], tmpPos[1]});
                    }
                }
                greenBorder(stack.getChildren().get(0));
                valid.add(new int[]{tmpPos[0], tmpPos[1]});
            }
            tmpPos[1]=pos[1];
            if((tmpPos[1]-1)>=0){
                tmpPos[1]--;
                stack = (StackPane) getNode(tmpPos[0], tmpPos[1], boardGrid);
                if(stack.getChildren().size()>1){
                    taking = (StackPane) stack.getChildren().get(1);
                    if(canTake(knight, taking)){
                        redBorder(taking.getChildren().get(1));
                        valid.add(new int[]{tmpPos[0], tmpPos[1]});
                    }
                }
                greenBorder(stack.getChildren().get(0));
                valid.add(new int[]{tmpPos[0], tmpPos[1]});
            }
        }
        tmpPos[0]=pos[0]; tmpPos[1]=pos[1];
        if(tmpPos[1]+2<=7){
            tmpPos[1]+=2;
            if((tmpPos[0]+1)<=7){
                tmpPos[0]++;
                stack = (StackPane) getNode(tmpPos[0], tmpPos[1], boardGrid);
                if(stack.getChildren().size()>1){
                    taking = (StackPane) stack.getChildren().get(1);
                    if(canTake(knight, taking)){
                        redBorder(taking.getChildren().get(1));
                        valid.add(new int[]{tmpPos[0], tmpPos[1]});
                    }
                }
                greenBorder(stack.getChildren().get(0));
                valid.add(new int[]{tmpPos[0], tmpPos[1]});
            }
            tmpPos[0]=pos[0];
            if((tmpPos[0]-1)>=0){
                tmpPos[0]--;
                stack = (StackPane) getNode(tmpPos[0], tmpPos[1], boardGrid);
                if(stack.getChildren().size()>1){
                    taking = (StackPane) stack.getChildren().get(1);
                    if(canTake(knight, taking)){
                        redBorder(taking.getChildren().get(1));
                        valid.add(new int[]{tmpPos[0], tmpPos[1]});
                    }
                }
                greenBorder(stack.getChildren().get(0));
                valid.add(new int[]{tmpPos[0], tmpPos[1]});
            }
        }
        tmpPos[0]=pos[0]; tmpPos[1]=pos[1];
        if(tmpPos[1]-2>=0){
            tmpPos[1]-=2;
            if((tmpPos[0]+1)<=7){
                tmpPos[0]++;
                stack = (StackPane) getNode(tmpPos[0], tmpPos[1], boardGrid);
                if(stack.getChildren().size()>1){
                    taking = (StackPane) stack.getChildren().get(1);
                    if(canTake(knight, taking)){
                        redBorder(taking.getChildren().get(1));
                        valid.add(new int[]{tmpPos[0], tmpPos[1]});
                    }
                }
                greenBorder(stack.getChildren().get(0));
                valid.add(new int[]{tmpPos[0], tmpPos[1]});
            }
            tmpPos[0]=pos[0];
            if((tmpPos[0]-1)>=0){
                tmpPos[0]--;
                stack = (StackPane) getNode(tmpPos[0], tmpPos[1], boardGrid);
                if(stack.getChildren().size()>1){
                    taking = (StackPane) stack.getChildren().get(1);
                    if(canTake(knight, taking)){
                        redBorder(taking.getChildren().get(1));
                        valid.add(new int[]{tmpPos[0], tmpPos[1]});
                    }
                }
                greenBorder(stack.getChildren().get(0));
                valid.add(new int[]{tmpPos[0], tmpPos[1]});
            }
        }
        return valid;
    }

    //king movement method
    public ArrayList<int[]> kingMoves(Node king){
        int[] pos = new int[]{getGridPos(king, boardGrid)[0], getGridPos(king, boardGrid)[1]};
        int[] tmpPos = new int[]{getGridPos(king, boardGrid)[0], getGridPos(king, boardGrid)[1]};
        ArrayList<int[]> valid = new ArrayList<>();
        tmpPos[0]=pos[0]+1; tmpPos[1]=pos[1];
        kingHelper(king, tmpPos, valid);
        tmpPos[0]=pos[0]+1; tmpPos[1]=pos[1]-1;
        kingHelper(king, tmpPos, valid);
        tmpPos[0]=pos[0]+1; tmpPos[1]=pos[1]+1;
        kingHelper(king, tmpPos, valid);
        tmpPos[0]=pos[0]; tmpPos[1]=pos[1]+1;
        kingHelper(king, tmpPos, valid);
        tmpPos[0]=pos[0]; tmpPos[1]=pos[1]-1;
        kingHelper(king, tmpPos, valid);
        tmpPos[0]=pos[0]-1; tmpPos[1]=pos[1];
        kingHelper(king, tmpPos, valid);
        tmpPos[0]=pos[0]-1; tmpPos[1]=pos[1]-1;
        kingHelper(king, tmpPos, valid);
        tmpPos[0]=pos[0]-1; tmpPos[1]=pos[1]+1;
        kingHelper(king, tmpPos, valid);
        return valid;
    }

    //helper method for circling the king
    private void kingHelper(Node king, int[] pos, ArrayList<int[]> valid) {
        StackPane stack;
        if(pos[0]>=0&&pos[0]<=7&&pos[1]>=0&&pos[1]<=7){
            stack = (StackPane) getNode(pos[0], pos[1], boardGrid);
            if(stack.getChildren().size()>1) {
                if (canTake(king, stack.getChildren().get(1))) {
                    StackPane taking = (StackPane) stack.getChildren().get(1);
                    redBorder(taking.getChildren().get(1));
                    valid.add(new int[]{pos[0], pos[1]});
                }
            }
            else{
                greenBorder(stack.getChildren().get(0));
                valid.add(new int[]{pos[0], pos[1]});
            }
        }
    }

    //returns true if given king is in check (for when king is moving and after each move, int[] used for ease of king movement, can overload later if needed)
    public boolean checkCheck(int[] kingPos, String player){
        if (player.equals("white")){
            MapIterator<String, Node> iterator = blackSprites.mapIterator();
            while (iterator.hasNext()) {
                iterator.next();
                StackPane piece = (StackPane) iterator.getValue();
                ArrayList<int[]> validMoves = getMovesCheck(piece, "black");
                for (int[] validMove : validMoves) {
                    if (validMove[0] == kingPos[0] && validMove[1] == kingPos[1]) {
                        blackBorders();
                        return true;
                    }
                }
            }
        }
        else {
            MapIterator<String, Node> iterator = whiteSprites.mapIterator();
            while (iterator.hasNext()) {
                iterator.next();
                StackPane piece = (StackPane) iterator.getValue();
                ArrayList<int[]> validMoves = getMovesCheck(piece, "white");
                for (int[] validMove : validMoves) {
                    if (validMove[0] == kingPos[0] && validMove[1] == kingPos[1]) {
                        blackBorders();
                        return true;
                    }
                }
            }
        }
        blackBorders();
        return false;
    }

    //The following section defines methods to create the game and game menu

    //ends game
    public void endGame(String result){
        gameOver(result);
        stage.setScene(gameOverScene);
    }

    //loads images into imageview objects, overlays them with transparent rectangle inside a stackpane for easier border support
    public void loadNodes() {
        String basePath;
        if(System.getProperty("os.name").startsWith("Linux")){
            basePath = ((new File("").getAbsolutePath()) + "/src/edu/wit/shepherdm1dyern1/p2pchess/images/");
        }
        else{
            basePath = ((new File("").getAbsolutePath()) + "\\src\\edu\\wit\\shepherdm1dyern1\\p2pchess\\images\\");
        }

        File folder = new File(basePath);
        File[] listOfFiles = folder.listFiles();

        assert listOfFiles != null;
        try{
            for (File file : listOfFiles) {

                if (file.isFile()) {

                    String img = file.getName();
                    img = img.replace("_", " ");
                    img = img.replace(".png", "");



                    if(img.startsWith("Black")){
                        if(img.startsWith("Pa", 6)){
                            for(int i=1; i<=8; i++){
                                makeBlackNode(basePath, file, img, i);
                            }
                        }
                        else if(img.startsWith("Bi", 6)){
                            for(int i=1; i<=2; i++){
                                makeBlackNode(basePath, file, img, i);
                            }
                        }
                        else if(img.startsWith("Kn", 6)){
                            for(int i=1; i<=2; i++){
                                makeBlackNode(basePath, file, img, i);
                            }
                        }
                        else if(img.startsWith("Ro", 6)){
                            for(int i=1; i<=2; i++){
                                makeBlackNode(basePath, file, img, i);
                            }
                        }
                        else if(img.startsWith("Ki", 6)){
                            ImageView image = new ImageView(new Image(new FileInputStream(basePath + file.getName())));
                            Rectangle surfaceRect = new Rectangle(75,75,Color.rgb(0, 0, 0, 0));
                            surfaceRect.setStroke(Color.rgb(20, 20, 20, 1));
                            surfaceRect.setStrokeWidth(2);
                            StackPane piece = new StackPane();
                            piece.getChildren().addAll(image, surfaceRect);
                            blackSprites.put(img, piece);
                        }
                        else if(img.startsWith("Qu", 6)){
                            ImageView image = new ImageView(new Image(new FileInputStream(basePath + file.getName())));
                            Rectangle surfaceRect = new Rectangle(75,75,Color.rgb(0, 0, 0, 0));
                            surfaceRect.setStroke(Color.rgb(20, 20, 20, 1));
                            surfaceRect.setStrokeWidth(2);
                            StackPane piece = new StackPane();
                            piece.getChildren().addAll(image, surfaceRect);
                            blackSprites.put(img, piece);

                        }
                    }
                    else{
                        if(img.startsWith("Pa", 6)){
                            for(int i=1; i<=8; i++){
                                makeWhiteNode(basePath, file, img, i);
                            }
                        }
                        else if(img.startsWith("Bi", 6)){
                            for(int i=1; i<=2; i++){
                                makeWhiteNode(basePath, file, img, i);
                            }
                        }
                        else if(img.startsWith("Kn", 6)){
                            for(int i=1; i<=2; i++){
                                makeWhiteNode(basePath, file, img, i);
                            }
                        }
                        else if(img.startsWith("Ro", 6)){
                            for(int i=1; i<=2; i++){
                                makeWhiteNode(basePath, file, img, i);
                            }
                        }
                        else if(img.startsWith("Ki", 6)){
                            ImageView image = new ImageView(new Image(new FileInputStream(basePath + file.getName())));
                            Rectangle surfaceRect = new Rectangle(75,75,Color.rgb(0, 0, 0, 0));
                            surfaceRect.setStroke(Color.rgb(20, 20, 20, 1));
                            surfaceRect.setStrokeWidth(2);
                            StackPane piece = new StackPane();
                            piece.getChildren().addAll(image, surfaceRect);
                            whiteSprites.put(img, piece);
                        }
                        else if(img.startsWith("Qu", 6)){
                            ImageView image = new ImageView(new Image(new FileInputStream(basePath + file.getName())));
                            Rectangle surfaceRect = new Rectangle(75,75,Color.rgb(0, 0, 0, 0));
                            surfaceRect.setStroke(Color.rgb(20, 20, 20, 1));
                            surfaceRect.setStrokeWidth(2);
                            StackPane piece = new StackPane();
                            piece.getChildren().addAll(image, surfaceRect);
                            whiteSprites.put(img, piece);
                        }
                    }
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    //helper method to generate a white piece node with image etc
    private void makeWhiteNode(String basePath, File file, String img, int i) {
        try{
            ImageView image = new ImageView(new Image(new FileInputStream(basePath + file.getName())));
            Rectangle surfaceRect = new Rectangle(75,75, Color.rgb(0, 0, 0, 0));
            surfaceRect.setStroke(Color.rgb(20, 20, 20, 1));
            surfaceRect.setStrokeWidth(2);
            StackPane piece = new StackPane();
            piece.getChildren().addAll(image, surfaceRect);
            whiteSprites.put(img+" "+i, piece);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    //helper method to generate a black piece node with image etc
    private void makeBlackNode(String basePath, File file, String img, int i) {
        try{
            ImageView image = new ImageView(new Image(new FileInputStream(basePath + file.getName())));
            Rectangle surfaceRect = new Rectangle(75,75, Color.rgb(0, 0, 0, 0));
            surfaceRect.setStroke(Color.rgb(20, 20, 20, 1));
            surfaceRect.setStrokeWidth(2);
            StackPane piece = new StackPane();
            piece.getChildren().addAll(image, surfaceRect);
            blackSprites.put(img+" "+i, piece);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    //places generated pieces in correct places on board
    public void placePieces(){
        StackPane gridStack = (StackPane) getNode(0, 0, boardGrid);
        gridStack.getChildren().add(blackSprites.get("Black Rook 1"));

        gridStack = (StackPane) getNode(7, 0, boardGrid);
        gridStack.getChildren().add(blackSprites.get("Black Rook 2"));


        gridStack = (StackPane) getNode(1, 0, boardGrid);
        gridStack.getChildren().add(blackSprites.get("Black Knight 1"));

        gridStack = (StackPane) getNode(6, 0, boardGrid);
        gridStack.getChildren().add(blackSprites.get("Black Knight 2"));

        gridStack = (StackPane) getNode(2, 0, boardGrid);
        gridStack.getChildren().add(blackSprites.get("Black Bishop 1"));

        gridStack = (StackPane) getNode(5, 0, boardGrid);
        gridStack.getChildren().add(blackSprites.get("Black Bishop 2"));

        gridStack = (StackPane) getNode(3, 0, boardGrid);
        gridStack.getChildren().add(blackSprites.get("Black Queen"));

        gridStack = (StackPane) getNode(4, 0, boardGrid);
        gridStack.getChildren().add(blackSprites.get("Black King"));


        for(int i = 0; i<8; i++){
            gridStack = (StackPane) getNode(i, 1, boardGrid);
            gridStack.getChildren().add(blackSprites.get("Black Pawn " + (i + 1)));
        }

        gridStack = (StackPane) getNode(0, 7, boardGrid);
        gridStack.getChildren().add(whiteSprites.get("White Rook 1"));

        gridStack = (StackPane) getNode(7, 7, boardGrid);
        gridStack.getChildren().add(whiteSprites.get("White Rook 2"));

        gridStack = (StackPane) getNode(1, 7, boardGrid);
        gridStack.getChildren().add(whiteSprites.get("White Knight 1"));

        gridStack = (StackPane) getNode(6, 7, boardGrid);
        gridStack.getChildren().add(whiteSprites.get("White Knight 2"));

        gridStack = (StackPane) getNode(2, 7, boardGrid);
        gridStack.getChildren().add(whiteSprites.get("White Bishop 1"));

        gridStack = (StackPane) getNode(5, 7, boardGrid);
        gridStack.getChildren().add(whiteSprites.get("White Bishop 2"));

        gridStack = (StackPane) getNode(3, 7, boardGrid);
        gridStack.getChildren().add(whiteSprites.get("White Queen"));

        gridStack = (StackPane) getNode(4, 7, boardGrid);
        gridStack.getChildren().add(whiteSprites.get("White King"));


        for(int i = 0; i<8; i++){
            gridStack = (StackPane) getNode(i, 6, boardGrid);
            gridStack.getChildren().add(whiteSprites.get("White Pawn " + (i + 1)));
        }





    }

    //creates menu scene and defines what buttons do, etc
    public Scene createMenu (){
        HBox startJoin = new HBox();

        Button create = new Button("Create Game");
        create.setPrefSize(100, 20);

        Label portLab = new Label("Local Port:");
        TextField portField = new TextField ();

        Button join = new Button("Join Game:");
        join.setPrefSize(100, 20);

        Label ipLab = new Label("IP Address and port:");
        TextField ipField = new TextField ();

        startJoin.getChildren().addAll(create, portLab, portField, join, ipLab, ipField);
        startJoin.setPadding(new Insets(5,5,5,5));
        startJoin.setSpacing(10);

        create.setOnAction(e -> {
            try {
                createGame(portField, chessBoard);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }); //CHANGE ME
        join.setOnAction(e -> {
            try {
                joinGame(ipField, chessBoard);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }); //CHANGE ME

        create.setOnAction(e -> {
            try {
                createGame(portField, chessBoard);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }); //CHANGE ME
        join.setOnAction(e -> {
            try {
                joinGame(ipField, chessBoard);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }); //CHANGE ME


        return new Scene(startJoin, 725, 500);
    }

    //create win/loss screen
    public void gameOver (String result){
        BorderPane root = new BorderPane();
        VBox gameOver = new VBox();

        Button quit = new Button("Quit Game");
        quit.setPrefSize(100, 20);

        //Button playAgain = new Button("Play Again");
        //playAgain.setPrefSize(100, 20);

        gameOver.getChildren().addAll(quit);
        gameOver.setPadding(new Insets(5, 5, 5, 5));
        gameOver.setSpacing(10);

        Text res = new Text();
        res.setText("You " + result);
        res.setFont(new Font(50));

        root.setCenter(res);
        root.setRight(gameOver);

        quit.setOnAction(e -> {
            try {
                if (isHost) {
                    try {
                        this.serverConnection.getConnectionThread().send("QUIT: "+playerColor, true);
                        this.serverConnection.getConnectionThread().closeConnection();
                        chatWindow.closeConnection();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                else {
                    try {
                        clientConnection.send("QUIT: "+playerColor, true);
                        clientConnection.closeConnection();
                        chatWindow.closeConnection();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                System.exit(1);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }); //CHANGE ME
        /*
        playAgain.setOnAction(e -> {
            try {
                restart();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }); //CHANGE ME
        */
        this.gameOverScene = new Scene(root, 725, 500);
    }

    //creates main chessboard/game scene and defines what buttons do, etc
    public Scene createChessBoard() {
        blackSprites = new DualHashBidiMap<>(){};
        whiteSprites = new DualHashBidiMap<>();


        BorderPane root = new BorderPane();
        StackPane stack = new StackPane();
        HBox boardControls = new HBox();
        //Button menuButton = new Button("Menu");
        //menuButton.setPrefSize(100, 20);

        Button forfeit = new Button("Forfeit");
        forfeit.setPrefSize(100, 20);
        forfeit.setOnAction(e -> {
            try {
                StringBuilder printColor = new StringBuilder();
                for (int i = 0; i < playerColor.toCharArray().length; i++){
                    if (i == 0) printColor.append(Character.toUpperCase(playerColor.toCharArray()[i]));
                    else printColor.append(playerColor.toCharArray()[i]);
                }
                if (isHost) {
                    try {
                        this.serverConnection.getConnectionThread().send("FORF: "+printColor, true);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                else {
                    try {
                        clientConnection.send("FORF: "+printColor, true);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                chatWindow.gameEvent(printColor + " forfeits!");
                endGame("lose");

            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });


        for(int i = 0; i<8; i++){
            for(int j = 0; j<8; j++){
                Rectangle rect = new Rectangle(75, 75, Color.WHITE);
                rect.setStroke(Color.rgb(20, 20, 20, 1));
                rect.setStrokeWidth(2);
                StackPane gridStack = new StackPane();
                if ((i % 2 != 0 || j % 2 != 0) && (i % 2 == 0 || j % 2 == 0)) {
                    rect.setFill(Color.rgb(20, 20, 20, 1));
                }
                gridStack.getChildren().add(rect);
                boardGrid.add(gridStack, i, j);
            }
        }

        this.loadNodes();

        boardGrid.setOnMouseClicked(this::handleClickEvent);

        boardControls.getChildren().addAll(boardGrid);
        stack.getChildren().addAll(boardControls);

        this.placePieces();

        root.setCenter(stack);


        VBox testbuttons = new VBox();
        testbuttons.getChildren().addAll(forfeit);
        root.setRight(testbuttons);
        return new Scene(root, 726, 616);
    }

    public static void main(String[] args) {
        mainGame = new Main();
        launch(args);
    }

    //returns the static game
    public static Main getGame(){
        return mainGame;
    }
}