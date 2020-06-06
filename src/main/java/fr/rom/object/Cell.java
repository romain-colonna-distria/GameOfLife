package fr.rom.object;

import fr.rom.utils.Util;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;

import java.io.Serializable;


public class Cell implements Serializable {
    private static final long serialVersionUID = 42L;
    private transient Rectangle shape;
    private int positionX, positionY;
    private boolean isAlive;
    private int nbAround;



    public Cell(){}

    public Cell(double width, double height, int positionX, int positionY){
        this.shape = new Rectangle(width, height);
        this.shape.setFill(Util.CELL_DEAD_COLOR);
        this.positionX = positionX;
        this.positionY = positionY;
        this.setDead();
    }

    public void setAlive(){
        isAlive = true;
        this.shape.setFill(Util.CELL_ALIVE_COLOR);
    }

    public void setDead(){
        this.isAlive = false;
        this.shape.setFill(Util.CELL_DEAD_COLOR);
    }

    public boolean isAlive(){
        return isAlive;
    }

    public int getPositionX() {
        return positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public Rectangle getShape() {
        return shape;
    }

    public int getNbAround() {
        return nbAround;
    }

    public void setNbAround(int nbAround) {
        this.nbAround = nbAround;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cell cell = (Cell) o;
        return positionX == cell.positionX &&
                positionY == cell.positionY;
    }


    @Override
    public String toString() {
        return "Cell{" +
                "positionX=" + positionX +
                ", positionY=" + positionY +
                ", isAlive=" + isAlive +
                '}';
    }
}
