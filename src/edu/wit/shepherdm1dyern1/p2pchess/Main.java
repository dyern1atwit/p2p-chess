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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Stack;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.*;


public class Main extends Application {
    private static Main mainGame;
    //public variables for easier helper access
    public static GridPane boardGrid = new GridPane();
    public Stage stage;
    public Scene chessBoard;
    public Scene menu;
    public Scene gameOverScene;
    public Node selectedNode;
    public int port;
    public static BidiMap<String, Node> blackSprites;
    public static BidiMap<String, Node> whiteSprites;
    public BidiMap<String, Node> blackTaken;
    public BidiMap<String, Node> whiteTaken;
    public String playerColor;
    public static String turn = "white";
    public ArrayList<int[]> currentValid;
    private ChatWindow chat;
    public ConnectionThread clientConnection;
    public ServerThread serverConnection;
    public boolean isHost;

    /*
    - do pieces
    - change what buttons do
    - implement networking
    - player control
     */

    @Override
    public void start(Stage primaryStage){
        this.stage = primaryStage;
        startGame();
    }

    //cleans up public variables and resets game
    public void startGame() {
        this.menu = createMenu();
        this.chessBoard = createChessBoard();
        this.stage.setTitle("Chess");
        this.stage.setScene(menu);
        this.stage.setResizable(false);
        this.stage.show();
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
        blackTaken=null;
        whiteTaken=null;
        playerColor=null;
        turn = "white";
        currentValid=null;
        startGame();
    }

    //takes port and game scene, will be used later to handle creating new game on given port
    public void createGame (TextField port, Scene game) throws IOException {
        this.playerColor = "white";
        this.isHost = true;
        this.stage.setScene(game);
        //this.chat = new ChatWindow();
        this.port = Integer.parseInt(port.getText());
        serverConnection = new ServerThread(this.port);
        serverConnection.start();
        System.out.println("white, host");
        System.out.println(port.getText());
        port.clear();
    }

    //takes ip and game scene, will be used later to handle connecting to existing game
    public void joinGame (TextField ip, Scene game) throws IOException {
        this.playerColor = "black";
        this.isHost = false;
        boardGrid.setRotate(180);
        for (String key : blackSprites.keySet()) {
            blackSprites.get(key).setRotate(180);
        }
        for (String key : whiteSprites.keySet()) {
            whiteSprites.get(key).setRotate(180);
        }
        this.stage.setScene(game);
        //this.chat = new ChatWindow();
        this.clientConnection = new ConnectionThread(ip.getText().split(":")[0], Integer.parseInt(ip.getText().split(":")[1]));
        this.clientConnection.setAsConnector();
        this.clientConnection.start();
        System.out.println("black, client");
        System.out.println(ip.getText());
        ip.clear();
    }

    //takes menu scene, returns to menu, will be used later to disconnect - not used
    /*
    public void toMenu(Scene menu){
        this.stage.setScene(menu);
    }
    */

    //removed due to event handler
    /*
    gets position of a node given a mouseclick event
    public int[] getGridPos (MouseEvent event, GridPane grid) {
        int[] pos = new int[2];
        Node clickedNode = event.getPickResult().getIntersectedNode();
        if (clickedNode != grid) {
            pos[0] = GridPane.getColumnIndex(clickedNode);
            pos[1] = GridPane.getRowIndex(clickedNode);
        }
        return pos;
    }
    unnecesary due to mouse handler
    */


    /*
    ^^^^^^^^^^^^^^^^^^^^^^^^
    Stage creation and stuff
    Getter/helper methods
    VVVVVVVVVVVVVVVVVVVVVV
     */

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
        int i = 0;
        for (Node node : gridNodes) {
            if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == column) {
                return node;
            }
            System.out.println(i);
            i++;
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
                getMoves(this.selectedNode.getParent(), boardGrid, "black");
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
                getMoves(this.selectedNode.getParent(), boardGrid, "white");
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

    /*
    ^^^^^^^^^^^^^^^^^^^^^
    Getter/helper methods
    Actual chess stuff (piece movement, taking, move checking, etc)
    VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV
    */

    //moves a node given piece (node being moved) and destination node (space/node clicked on) - switches turn after move
    public void movePiece(Node piece, Node dest){           //WILL NEED CONTINUOUS REWORK
        String s = "MOVE: ";
        if (turn.equals(playerColor)){
            for (int i : getGridPos(piece, this.boardGrid)) s += Integer.toString(i) + " ";
            for (int i : getGridPos(dest, this.boardGrid)) s += Integer.toString(i) + " ";
            System.out.println(s);
        }
        piece = piece.getParent();

        StackPane destStack = (StackPane) dest.getParent();
        StackPane finalDest = destStack;

        if((!(blackSprites.getKey(destStack)==null)&&playerColor.equals("white"))||(!(whiteSprites.getKey(destStack)==null)&&playerColor.equals("black"))){
            finalDest = (StackPane) destStack.getParent();
            takePiece(destStack);
        }

        StackPane sourceStack = (StackPane) piece.getParent();

        sourceStack.getChildren().remove(piece);
        finalDest.getChildren().add(piece);

        this.selectedNode = null;

        if (turn.equals(playerColor)) {
            if (isHost) {
                try {
                    this.serverConnection.getConnectionThread().send(s, true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                try {
                    this.clientConnection.send(s, true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        switchTurn();

    }

    public void movePiece(int x1, int y1, int x2, int y2) {
        StackPane sourceStack = (StackPane) getNode(x1, y1, boardGrid);
        StackPane destStack = (StackPane) getNode(x2, y2, boardGrid);
        if(destStack.getChildren().size()>1){
            takePiece(destStack.getChildren().get(1));
        }
        StackPane piece = (StackPane) sourceStack.getChildren().get(1);
        sourceStack.getChildren().remove(sourceStack.getChildren().get(1));
        destStack.getChildren().add(piece);
    }

    //method to take a piece, takes piece off the board, removes from "sprites" bidimap and adds to "taken" bidimap
    public void takePiece(Node taken){
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
    }

    //IMPORTANT method that determines where a clicked piece can move and highlights those spaces NOT FINISHED
    public ArrayList<int[]> getMoves(Node piece, GridPane grid, String player) {

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
                currentValid = rookMoves(piece, player);
                break;
            case "Qu":
                currentValid = queenMoves(piece, player);
                break;
            case "Ki":
                currentValid = kingMoves(piece, player);
                break;
            case "Bi":
                currentValid = bishopMoves(piece, player);
                break;
            case "Kn":
                currentValid = knightMoves(piece, player);
                break;
        }
        return new ArrayList<>();
    }

    //helper method to find if a piece can take another piece
    public boolean canTake(Node taker, Node taken){
        return (whiteSprites.containsValue(taker) && blackSprites.containsValue(taken)) || (blackSprites.containsValue(taker) && whiteSprites.containsValue(taken));
    }

    /*STUB METHOD FOR PAWN PROMOTION - unfinished
    public void promotePawn(Node pawn){
    }
     */

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

    //HOW PIECES MOVE BELOW

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
                    if((pos[0]-1)>=0){
                        stack = (StackPane) getNode(pos[0]-1, pos[1], boardGrid);
                    }
                    if(stack.getChildren().size()>1&&canTake(pawn, stack.getChildren().get(1))&&i==0){
                        valid.add(new int[]{pos[0]-1, pos[1]});
                        StackPane taking = (StackPane) stack.getChildren().get(1);
                        redBorder(taking.getChildren().get(1));
                    }
                    if((pos[0]+1)<=7){
                        stack = (StackPane) getNode(pos[0]+1, pos[1], boardGrid);
                    }
                    if(stack.getChildren().size()>1&&canTake(pawn, stack.getChildren().get(1))&&i==0){
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
    public ArrayList<int[]> bishopMoves(Node bishop, String player){
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
    public ArrayList<int[]> rookMoves(Node rook, String player){
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
    public ArrayList<int[]> queenMoves(Node queen, String player){
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
    public ArrayList<int[]> knightMoves(Node knight, String player){
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
    public ArrayList<int[]> kingMoves(Node king, String player){
        int[] pos = new int[]{getGridPos(king, boardGrid)[0], getGridPos(king, boardGrid)[1]};
        int[] tmpPos = new int[]{getGridPos(king, boardGrid)[0], getGridPos(king, boardGrid)[1]};
        ArrayList<int[]> valid = new ArrayList<>();
        tmpPos[0]=pos[0]+1; tmpPos[1]=pos[1];
        kingHelper(king, tmpPos, valid, player);
        tmpPos[0]=pos[0]+1; tmpPos[1]=pos[1]-1;
        kingHelper(king, tmpPos, valid, player);
        tmpPos[0]=pos[0]+1; tmpPos[1]=pos[1]+1;
        kingHelper(king, tmpPos, valid, player);
        tmpPos[0]=pos[0]; tmpPos[1]=pos[1]+1;
        kingHelper(king, tmpPos, valid, player);
        tmpPos[0]=pos[0]; tmpPos[1]=pos[1]-1;
        kingHelper(king, tmpPos, valid, player);
        tmpPos[0]=pos[0]-1; tmpPos[1]=pos[1];
        kingHelper(king, tmpPos, valid, player);
        tmpPos[0]=pos[0]-1; tmpPos[1]=pos[1]-1;
        kingHelper(king, tmpPos, valid, player);
        tmpPos[0]=pos[0]-1; tmpPos[1]=pos[1]+1;
        kingHelper(king, tmpPos, valid, player);
        return valid;
    }

    //helper method for circling the king
    private void kingHelper(Node king, int[] pos, ArrayList<int[]> valid, String player) {
        StackPane stack;
        if(pos[0]>=0&&pos[0]<=7&&pos[1]>=0&&pos[1]<=7&&!checkCheck(new int[]{pos[0], pos[1]}, player)){
            stack = (StackPane) getNode(pos[0], pos[1], boardGrid);
            if(stack.getChildren().size()>1) {
                if (canTake(king, stack.getChildren().get(1))) {
                    StackPane taking = (StackPane) stack.getChildren().get(1);
                    redBorder(taking.getChildren().get(1));
                    valid.add(new int[]{pos[0], pos[1]});
                }
            }
            else{
                System.out.println("why");
                greenBorder(stack.getChildren().get(0));
                valid.add(new int[]{pos[0], pos[1]});
            }
        }
    }

    //returns true if given king is in check (for when king is moving and after each move, int[] used for ease of king movement, can overload later if needed)
    public boolean checkCheck(int[] kingPos, String player){
        //stub
        return false;
    }

    /*
    ^^^^^^^^^^^^^^^^^^^^^
    Actual chess stuff (piece movement, taking, move checking, etc)
    preload/creation stuff
    VVVVVVVVVVVVVVVVVVVVV
    */

    //ends game
    public void endGame(String result){
        gameOver(result);
        this.stage.setScene(gameOverScene);
    }

    //loads images into imageview objects, overlays them with transparent rectangle inside a stackpane for easier border support
    public void loadNodes() {
        ArrayList<ImageView> images = new ArrayList<>();
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
                            this.blackSprites.put(img, piece);
                        }
                        else if(img.startsWith("Qu", 6)){
                            ImageView image = new ImageView(new Image(new FileInputStream(basePath + file.getName())));
                            Rectangle surfaceRect = new Rectangle(75,75,Color.rgb(0, 0, 0, 0));
                            surfaceRect.setStroke(Color.rgb(20, 20, 20, 1));
                            surfaceRect.setStrokeWidth(2);
                            StackPane piece = new StackPane();
                            piece.getChildren().addAll(image, surfaceRect);
                            this.blackSprites.put(img, piece);

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
                            this.whiteSprites.put(img, piece);
                        }
                        else if(img.startsWith("Qu", 6)){
                            ImageView image = new ImageView(new Image(new FileInputStream(basePath + file.getName())));
                            Rectangle surfaceRect = new Rectangle(75,75,Color.rgb(0, 0, 0, 0));
                            surfaceRect.setStroke(Color.rgb(20, 20, 20, 1));
                            surfaceRect.setStrokeWidth(2);
                            StackPane piece = new StackPane();
                            piece.getChildren().addAll(image, surfaceRect);
                            this.whiteSprites.put(img, piece);
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
            this.whiteSprites.put(img+" "+i, piece);
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
            this.blackSprites.put(img+" "+i, piece);
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

        gridStack = (StackPane) getNode(4, 0, boardGrid);
        gridStack.getChildren().add(blackSprites.get("Black Queen"));

        gridStack = (StackPane) getNode(3, 0, boardGrid);
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

        Button playAgain = new Button("Play Again");
        playAgain.setPrefSize(100, 20);

        gameOver.getChildren().addAll(quit, playAgain);
        gameOver.setPadding(new Insets(5, 5, 5, 5));
        gameOver.setSpacing(10);

        Text res = new Text();
        res.setText("You " + result);
        res.setFont(new Font(50));

        root.setCenter(res);
        root.setRight(gameOver);

        quit.setOnAction(e -> {
            try {
                System.exit(1);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }); //CHANGE ME
        playAgain.setOnAction(e -> {
            try {
                restart();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }); //CHANGE ME

        this.gameOverScene = new Scene(root, 725, 500);
    }

    //creates main chessboard/game scene and defines what buttons do, etc
    public Scene createChessBoard() {
        this.blackSprites = new DualHashBidiMap<>(){};
        this.whiteSprites = new DualHashBidiMap<>();
        this.blackTaken = new DualHashBidiMap<>(){};
        this.whiteTaken = new DualHashBidiMap<>();

        BorderPane root = new BorderPane();
        StackPane stack = new StackPane();
        HBox boardControls = new HBox();
        //Button menuButton = new Button("Menu");
        //menuButton.setPrefSize(100, 20);

        Button forfeit = new Button("Forfeit");
        forfeit.setPrefSize(100, 20);
        forfeit.setOnAction(e -> {
            try {
                endGame("lose");
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });

        //TESTING ONLY BUTTON TO SWITCH PLAYER - for turn testing etc before sockets implemented
        Button switchPlayer = new Button("Switch player");
        switchPlayer.setPrefSize(110, 20);
        switchPlayer.setOnAction(e -> switchTurn());
        //TESTING ONLY BUTTON TO SWITCH PLAYER - for turn testing etc before sockets implemented
        Button printPlayer = new Button("Print player");
        printPlayer.setPrefSize(100, 20);
        printPlayer.setOnAction(e -> System.out.println(turn));


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
        testbuttons.getChildren().addAll(switchPlayer, printPlayer, forfeit);
        root.setRight(testbuttons);
        //menuButton.setOnAction(e -> toMenu(menu)); //CHANGE ME
        return new Scene(root, 726, 616);
    }

    public static void main(String[] args) {
        mainGame = new Main();
        mainGame.launch(args);
    }

    public static Main getGame(){
        return mainGame;
    }
}