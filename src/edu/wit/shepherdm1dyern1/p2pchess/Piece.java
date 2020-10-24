/*
package edu.wit.shepherdm1dyern1.p2pchess;

abstract class Piece
{
    String pieceType;
    int xPos;
    int yPos;

    Piece(String type, int x, int y)
    {
        this.pieceType = type;
        this.xPos = x;
        this.yPos = y;
    }

    public void moveTo(int x, int y)
    {
        this.xPos = x;
        this.yPos = y;
    }

    // abstract methods which will be
    // implemented by its subclass(es)
    abstract public double area();
    abstract public void draw();
}

class Queen extends Piece
{

    int length, width;

    // constructor
    Queen(int length, int width, String name)
    {

        super(name);
        this.length = length;
        this.width = width;
    }

    @Override
    public void draw()
    {
        System.out.println("Rectangle has been drawn ");
    }

    @Override
    public double area()
    {
        return (double)(length*width);
    }
}
*/