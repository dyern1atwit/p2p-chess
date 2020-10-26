package edu.wit.shepherdm1dyern1.p2pchess;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public abstract class Piece
{
    String pieceType;
    String color;
    int row;
    int col;
    ImageView sprite;


    public Piece(String type, String color, int row, int col, String spriteFile) {
        this.pieceType = type;
        this.color = color;
        this.row = row;
        this.col = col;
        Image image = new Image(spriteFile);
        sprite.setImage(image);
    }

    public void setPos(int row, int col)
    {
        this.row = row;
        this.col = col;
    }

}
