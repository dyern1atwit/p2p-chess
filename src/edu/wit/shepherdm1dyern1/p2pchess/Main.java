package edu.wit.shepherdm1dyern1.p2pchess;
import javafx.application.*;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.stage.*;

public class Main extends Application {

    public GridPane grid = new GridPane();

    //// to do: add a 2d array as a "backup" for the board that allows for easier access to objects on the board/nodes

    @Override
    public void start(Stage primaryStage) {
        StackPane root = new StackPane();

        for(int i = 0; i<8; i++){
            for(int j = 0; j<8; j++){
                Rectangle rect = new Rectangle(50, 50, Color.WHITE);
                rect.setStroke(Color.BLACK);
                rect.setStrokeWidth(2);
                if ((i % 2 != 0 || j % 2 != 0) && (i % 2 == 0 || j % 2 == 0)) {
                    rect.setFill(Color.BLACK);
                }
                grid.add(rect, i, j);
            }
        }
        grid.setOnMouseClicked(this::clickGrid);

        root.getChildren().add(grid);
        Scene scene = new Scene(root, 500, 500);
        primaryStage.setResizable(false);
        primaryStage.setTitle("ChessBoard");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void clickGrid(MouseEvent event) {
        Node clickedNode = event.getPickResult().getIntersectedNode();
        if (clickedNode != grid) {
            Integer col = GridPane.getColumnIndex(clickedNode);
            Integer row = GridPane.getRowIndex(clickedNode);
            Rectangle rect = nodeFromGrid(row, col, grid);
            rect.setStroke(Color.GREEN);
        }
    }

    public Rectangle nodeFromGrid (int row, int column, GridPane gridPane) {
        Rectangle result = null;
        ObservableList<Node> gridNodes = gridPane.getChildren();
        for (Node node : gridNodes) {
            if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == column) {
                result = (Rectangle) node;
                break;
            }
        }
        return result;
    }


    public static void main(String[] args) {
        launch(args);
    }
}
