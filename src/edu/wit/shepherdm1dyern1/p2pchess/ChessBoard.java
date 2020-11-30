package edu.wit.shepherdm1dyern1.p2pchess;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

public class ChessBoard extends StackPane {
    public GridPane boardGrid = new GridPane();
    public BidiMap<String, Node> blackSprites;
    public BidiMap<String, Node> whiteSprites;
    public BidiMap<String, Node> blackTakenSprites;
    public BidiMap<String, Node> whiteTakenSprites;

    public ChessBoard() {
        this.blackSprites = new DualHashBidiMap<>();
        this.whiteSprites = new DualHashBidiMap<>();
        this.blackTakenSprites = new DualHashBidiMap<>();
        this.whiteTakenSprites = new DualHashBidiMap<>();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Square square = new Square();
                if ((i % 2 != 0 || j % 2 != 0) && (i % 2 == 0 || j % 2 == 0)) {
                    square.setBlack();
                }
                boardGrid.add(square, i, j);
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
    }

    public Scene createScene() {
        return new Scene(this, 616, 616);
    }

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

    public Node getNode (int column, int row) {
        Node result = null;
        ObservableList<Node> gridNodes = boardGrid.getChildren();
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
}
