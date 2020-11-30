package edu.wit.shepherdm1dyern1.p2pchess;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Square extends StackPane {
    Color color;
    Rectangle drawnRectangle;


    public Square(){
        drawnRectangle = new Rectangle(75, 75, Color.WHITE);
        drawnRectangle.setStroke(Color.rgb(20, 20, 20));
        drawnRectangle.setStrokeWidth(2);
        getChildren().add(drawnRectangle);
    }

    public void setBlack(){
        drawnRectangle.setFill(Color.rgb(20, 20, 20));
    }
    public void setWhite(){
        drawnRectangle.setFill(Color.WHITE);
    }
}
