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
import javafx.stage.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Hashtable;

public class Main extends Application {
    //public variables for easier helper access
    public GridPane boardGrid = new GridPane();
    public Stage stage;
    public Scene chessBoard;
    public Scene menu;
    public Node selectedNode;
    public Hashtable<String, Node> blackSprites;
    public Hashtable<String, Node> whiteSprites;
    public String playerColor;

    /*
    - do pieces
    - change what buttons do
    - implement networking
    - player control
     */

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.stage = primaryStage;
        this.menu = createMenu();
        this.chessBoard = createChessBoard();
        this.stage.setTitle("Chess");
        this.stage.setScene(menu);
        this.stage.show();
    }

    //takes port and game scene, will be used later to handle creating new game on given port
    public void createGame (TextField port, Scene game) {
        this.playerColor = "white";
        this.stage.setScene(game);
        System.out.println(port.getText());
        port.clear();
    }

    //takes ip and game scene, will be used later to handle connecting to existing game
    public void joinGame (TextField ip, Scene game) {
        this.playerColor = "black";
        boardGrid.setRotate(180);
        for (String key : blackSprites.keySet()) {
            blackSprites.get(key).setRotate(180);
        }
        for (String key : whiteSprites.keySet()) {
            whiteSprites.get(key).setRotate(180);
        }
        this.stage.setScene(game);
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

    //gets position of node given node
    public int[] getGridPos (Node node, GridPane grid) {
        int[] pos = new int[2];
        if (node != grid) {
            pos[0] = GridPane.getColumnIndex(node.getParent());
            pos[1] = GridPane.getRowIndex(node.getParent());
        }
        return pos;
    }

    //gets node given row and column values
    public Node getNode (int row, int column, GridPane grid) {
        Node result = null;
        ObservableList<Node> gridNodes = grid.getChildren();
        for (Node node : gridNodes) {
            if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == column) {
                result = node;
                break;
            }
        }
        return result;
    }

    //gets node given mouseclick event
    public Node getNode (MouseEvent event) {
        return event.getPickResult().getIntersectedNode();
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

    //moves a node given piece (node being moved) and destination node (space/node clicked on)
    public void movePiece(Node piece, Node dest){           //WILL NEED REWORK FOR PIECE OBJECTS - ONLY TESTING MOVING NODES
        piece = piece.getParent();
        StackPane sourceStack = (StackPane) piece.getParent();
        sourceStack.getChildren().remove(piece);

        StackPane destStack = (StackPane) dest.getParent();
        destStack.getChildren().add(piece);
        this.selectedNode = null;
    }

    //helper method to handle click events on nodes
    public void handleClickEvent(MouseEvent event) { //WILL NEED REWORK FOR PIECE OBJECTS - ONLY TESTING SELECTING/MOVING NODES
        Node clickedNode = getNode(event);
        if (playerColor.equals("black") && blackSprites.contains(clickedNode.getParent())){
                if (this.selectedNode==null){
                    greenBorder(clickedNode);
                    this.selectedNode = clickedNode;
                }
                else if (clickedNode == this.selectedNode) {
                    greenBorder(clickedNode);
                    this.selectedNode = null;
                }
        }
        else if(playerColor.equals("white") && whiteSprites.contains(clickedNode.getParent())) {
            if (this.selectedNode==null){
                greenBorder(clickedNode);
                this.selectedNode = clickedNode;
            }
            else if (clickedNode == this.selectedNode) {
                greenBorder(clickedNode);
                this.selectedNode = null;
            }
        }
        else if (this.selectedNode!=null) {
            greenBorder(selectedNode);
            movePiece(this.selectedNode, clickedNode);
        }
        else{
            System.out.println("no piece selected");
            if (playerColor.equals("black") && whiteSprites.contains(clickedNode.getParent())){
                System.out.println("Not your Piece");
            }
        }
    }

    //loads images into imageview objects, overlays them with transparent rectangle inside a stackpane for easier border support
    public void loadImages() throws Exception {
        ArrayList<ImageView> images = new ArrayList<>();
        String basePath = ((new File("").getAbsolutePath()) + "\\src\\edu\\wit\\shepherdm1dyern1\\p2pchess\\images\\");

        File folder = new File(basePath);
        File[] listOfFiles = folder.listFiles();

        assert listOfFiles != null;
        for (File file : listOfFiles) {

            if (file.isFile()) {

                String img = file.getName();
                img = img.replace("_", " ");
                img = img.replace(".png", "");



                if(img.startsWith("Black")){
                    if(img.startsWith("Pa", 6)){
                        for(int i=1; i<=8; i++){
                            ImageView image = new ImageView(new Image(new FileInputStream(basePath + file.getName())));
                            Rectangle surfaceRect = new Rectangle(75,75,Color.rgb(0, 0, 0, 0));
                            surfaceRect.setStroke(Color.rgb(20, 20, 20, 1));
                            surfaceRect.setStrokeWidth(2);
                            StackPane piece = new StackPane();
                            piece.getChildren().addAll(image, surfaceRect);
                            this.blackSprites.put(img+" "+i, piece);
                        }
                    }
                    else if(img.startsWith("Bi", 6)){
                        for(int i=1; i<=2; i++){
                            ImageView image = new ImageView(new Image(new FileInputStream(basePath + file.getName())));
                            Rectangle surfaceRect = new Rectangle(75,75,Color.rgb(0, 0, 0, 0));
                            surfaceRect.setStroke(Color.rgb(20, 20, 20, 1));
                            surfaceRect.setStrokeWidth(2);
                            StackPane piece = new StackPane();
                            piece.getChildren().addAll(image, surfaceRect);
                            this.blackSprites.put(img+" "+i, piece);
                        }
                    }
                    else if(img.startsWith("Kn", 6)){
                        for(int i=1; i<=2; i++){
                            ImageView image = new ImageView(new Image(new FileInputStream(basePath + file.getName())));
                            Rectangle surfaceRect = new Rectangle(75,75,Color.rgb(0, 0, 0, 0));
                            surfaceRect.setStroke(Color.rgb(20, 20, 20, 1));
                            surfaceRect.setStrokeWidth(2);
                            StackPane piece = new StackPane();
                            piece.getChildren().addAll(image, surfaceRect);
                            this.blackSprites.put(img+" "+i, piece);
                        }
                    }
                    else if(img.startsWith("Ro", 6)){
                        for(int i=1; i<=2; i++){
                            ImageView image = new ImageView(new Image(new FileInputStream(basePath + file.getName())));
                            Rectangle surfaceRect = new Rectangle(75,75,Color.rgb(0, 0, 0, 0));
                            surfaceRect.setStroke(Color.rgb(20, 20, 20, 1));
                            surfaceRect.setStrokeWidth(2);
                            StackPane piece = new StackPane();
                            piece.getChildren().addAll(image, surfaceRect);
                            this.blackSprites.put(img+" "+i, piece);
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
                            ImageView image = new ImageView(new Image(new FileInputStream(basePath + file.getName())));
                            Rectangle surfaceRect = new Rectangle(75,75,Color.rgb(0, 0, 0, 0));
                            surfaceRect.setStroke(Color.rgb(20, 20, 20, 1));
                            surfaceRect.setStrokeWidth(2);
                            StackPane piece = new StackPane();
                            piece.getChildren().addAll(image, surfaceRect);
                            this.whiteSprites.put(img+" "+i, piece);
                        }
                    }
                    else if(img.startsWith("Bi", 6)){
                        for(int i=1; i<=2; i++){
                            ImageView image = new ImageView(new Image(new FileInputStream(basePath + file.getName())));
                            Rectangle surfaceRect = new Rectangle(75,75,Color.rgb(0, 0, 0, 0));
                            surfaceRect.setStroke(Color.rgb(20, 20, 20, 1));
                            surfaceRect.setStrokeWidth(2);
                            StackPane piece = new StackPane();
                            piece.getChildren().addAll(image, surfaceRect);
                            this.whiteSprites.put(img+" "+i, piece);
                        }
                    }
                    else if(img.startsWith("Kn", 6)){
                        for(int i=1; i<=2; i++){
                            ImageView image = new ImageView(new Image(new FileInputStream(basePath + file.getName())));
                            Rectangle surfaceRect = new Rectangle(75,75,Color.rgb(0, 0, 0, 0));
                            surfaceRect.setStroke(Color.rgb(20, 20, 20, 1));
                            surfaceRect.setStrokeWidth(2);
                            StackPane piece = new StackPane();
                            piece.getChildren().addAll(image, surfaceRect);
                            this.whiteSprites.put(img+" "+i, piece);
                        }
                    }
                    else if(img.startsWith("Ro", 6)){
                        for(int i=1; i<=2; i++){
                            ImageView image = new ImageView(new Image(new FileInputStream(basePath + file.getName())));
                            Rectangle surfaceRect = new Rectangle(75,75,Color.rgb(0, 0, 0, 0));
                            surfaceRect.setStroke(Color.rgb(20, 20, 20, 1));
                            surfaceRect.setStrokeWidth(2);
                            StackPane piece = new StackPane();
                            piece.getChildren().addAll(image, surfaceRect);
                            this.whiteSprites.put(img+" "+i, piece);
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

    public void placePieces(){
        StackPane gridStack = (StackPane) getNode(0, 0, boardGrid);
        gridStack.getChildren().add(blackSprites.get("Black Rook 1"));

        gridStack = (StackPane) getNode(0, 7, boardGrid);
        gridStack.getChildren().add(blackSprites.get("Black Rook 2"));


        gridStack = (StackPane) getNode(0, 1, boardGrid);
        gridStack.getChildren().add(blackSprites.get("Black Knight 1"));

        gridStack = (StackPane) getNode(0, 6, boardGrid);
        gridStack.getChildren().add(blackSprites.get("Black Knight 2"));

        gridStack = (StackPane) getNode(0, 2, boardGrid);
        gridStack.getChildren().add(blackSprites.get("Black Bishop 1"));

        gridStack = (StackPane) getNode(0, 5, boardGrid);
        gridStack.getChildren().add(blackSprites.get("Black Bishop 2"));

        gridStack = (StackPane) getNode(0, 3, boardGrid);
        gridStack.getChildren().add(blackSprites.get("Black Queen"));

        gridStack = (StackPane) getNode(0, 4, boardGrid);
        gridStack.getChildren().add(blackSprites.get("Black King"));


        for(int i = 0; i<8; i++){
            gridStack = (StackPane) getNode(1, i, boardGrid);
            gridStack.getChildren().add(blackSprites.get("Black Pawn " + (i + 1)));
        }

        gridStack = (StackPane) getNode(7, 0, boardGrid);
        gridStack.getChildren().add(whiteSprites.get("White Rook 1"));

        gridStack = (StackPane) getNode(7, 7, boardGrid);
        gridStack.getChildren().add(whiteSprites.get("White Rook 2"));

        gridStack = (StackPane) getNode(7, 1, boardGrid);
        gridStack.getChildren().add(whiteSprites.get("White Knight 1"));

        gridStack = (StackPane) getNode(7, 6, boardGrid);
        gridStack.getChildren().add(whiteSprites.get("White Knight 2"));

        gridStack = (StackPane) getNode(7, 2, boardGrid);
        gridStack.getChildren().add(whiteSprites.get("White Bishop 1"));

        gridStack = (StackPane) getNode(7, 5, boardGrid);
        gridStack.getChildren().add(whiteSprites.get("White Bishop 2"));

        gridStack = (StackPane) getNode(7, 4, boardGrid);
        gridStack.getChildren().add(whiteSprites.get("White Queen"));

        gridStack = (StackPane) getNode(7, 3, boardGrid);
        gridStack.getChildren().add(whiteSprites.get("White King"));


        for(int i = 0; i<8; i++){
            gridStack = (StackPane) getNode(6, i, boardGrid);
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

    //creates main chessboard/game scene and defines what buttons do, etc
    public Scene createChessBoard() throws Exception {
        this.blackSprites = new Hashtable<>();
        this.whiteSprites = new Hashtable<>();

        BorderPane root = new BorderPane();
        StackPane stack = new StackPane();
        HBox boardControls = new HBox();
        //Button menuButton = new Button("Menu");
        //menuButton.setPrefSize(100, 20);

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

        this.loadImages();

        boardGrid.setOnMouseClicked(this::handleClickEvent);

        boardControls.getChildren().addAll(boardGrid);
        stack.getChildren().addAll(boardControls);

        this.placePieces();

        root.setCenter(stack);
        //menuButton.setOnAction(e -> toMenu(menu)); //CHANGE ME
        return new Scene(root, 616, 616);
    }

    public static void main(String[] args) {
        launch(args);
    }
}