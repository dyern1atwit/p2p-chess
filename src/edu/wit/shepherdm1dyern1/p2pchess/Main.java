package edu.wit.shepherdm1dyern1.p2pchess;
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

public class Main extends Application {
    //public variables for easier helper access
    public GridPane boardGrid = new GridPane();
    public Stage stage;
    public Scene chessBoard;
    public Scene menu;
    public Node selectedNode;

    /*
    - do pieces
    - change what buttons do
    - implement networking
    - player control
     */

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        this.chessBoard = createChessBoard();
        this.menu = createMenu();
        this.stage.setTitle("Chess");
        this.stage.setScene(menu);
        this.stage.show();
    }

    //takes port and game scene, will be used later to handle creating new game on given port
    public void createGame (TextField port, Scene game){
        this.stage.setScene(game);
        System.out.println(port.getText());
        port.clear();
    }

    //takes ip and game scene, will be used later to handle connecting to existing game
    public void joinGame (TextField ip, Scene game){
        this.stage.setScene(game);
        System.out.println(ip.getText());
        ip.clear();
    }

    //takes menu scene, returns to menu, will be used later to disconnect
    public void toMenu(Scene menu){
        this.stage.setScene(menu);
    }

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
                shape.setStroke(Color.BLACK);
        }
        else {
            shape.setStroke(Color.GREEN);
        }
    }

    //moves a node given piece (node being moved) and destination node (space/node clicked on)
    public void movePiece(Node piece, Node dest){           //WILL NEED REWORK FOR PIECE OBJECTS - ONLY TESTING MOVING NODES
        System.out.print("moving");

        StackPane sourceStack = (StackPane) piece.getParent();
        sourceStack.getChildren().remove(piece);

        StackPane destStack = (StackPane) dest.getParent();
        destStack.getChildren().add(piece);
        this.selectedNode = null;
    }

    //helper method to handle click events on nodes
    public void handleClickEvent(MouseEvent event){ //WILL NEED REWORK FOR PIECE OBJECTS - ONLY TESTING SELECTING/MOVING NODES
        Node clickedNode = getNode(event);
        if(this.selectedNode!=null){
            if(clickedNode==this.selectedNode) {
                greenBorder(clickedNode);
                this.selectedNode = null;
            }
            else {
                greenBorder(selectedNode);
                movePiece(this.selectedNode, clickedNode);
            }
        }
        else {
            if(clickedNode instanceof Circle) {
                greenBorder(clickedNode);
                this.selectedNode = clickedNode;
            }
            else{
                System.out.println("nope");
            }
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

        create.setOnAction(e -> createGame(portField, chessBoard)); //CHANGE ME
        join.setOnAction(e -> joinGame(ipField, chessBoard)); //CHANGE ME

        create.setOnAction(e -> createGame(portField, chessBoard)); //CHANGE ME
        join.setOnAction(e -> joinGame(ipField, chessBoard)); //CHANGE ME

        return new Scene(startJoin, 725, 500);
    }

    //creates main chessboard/game scene and defines what buttons do, etc
    public Scene createChessBoard(){
        BorderPane root = new BorderPane();
        StackPane stack = new StackPane();
        HBox boardControls = new HBox();
        Button menuButton = new Button("Menu");
        menuButton.setPrefSize(100, 20);

        for(int i = 0; i<8; i++){
            for(int j = 0; j<8; j++){
                Rectangle rect = new Rectangle(75, 75, Color.WHITE);
                rect.setStroke(Color.BLACK);
                rect.setStrokeWidth(2);
                StackPane gridStack = new StackPane();
                if ((i % 2 != 0 || j % 2 != 0) && (i % 2 == 0 || j % 2 == 0)) {
                    rect.setFill(Color.BLACK);
                }
                gridStack.getChildren().add(rect);
                boardGrid.add(gridStack, i, j);
            }
        }

        StackPane gridStack = (StackPane) getNode(0, 0, boardGrid);
        Circle TESTPIECE = new Circle(25, Color.RED);
        TESTPIECE.setStroke(Color.BLACK);
        gridStack.getChildren().add(TESTPIECE);

        boardGrid.setOnMouseClicked(this::handleClickEvent);

        boardControls.getChildren().addAll(boardGrid, menuButton);
        stack.getChildren().addAll(boardControls);

        root.setCenter(stack);
        menuButton.setOnAction(e -> toMenu(menu)); //CHANGE ME
        return new Scene(root, 1000, 1000);
    }

    public static void main(String[] args) {
        launch(args);
    }
}