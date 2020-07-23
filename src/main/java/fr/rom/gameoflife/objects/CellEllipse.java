package fr.rom.gameoflife.objects;

import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class CellEllipse extends Ellipse implements ICell, Serializable {
    private static final long serialVersionUID = 42L;

    private final int positionX;
    private final int positionY;
    private boolean isAlive = false;

    private String aliveColor = "black";
    private String deadColor = "white";

    private transient Set<ICell> aroundCells;



    public CellEllipse(double width, double height, int positionX, int positionY){
        super(width, height);

        this.positionX = positionX;
        this.positionY = positionY;
        this.aroundCells = new HashSet<>();

        this.makeDead();
    }



    public void makeAlive(){
        this.isAlive = true;
        this.setFill(Color.valueOf(aliveColor));
    }

    public void makeDead(){
        this.isAlive = false;
        this.setFill(Color.valueOf(deadColor));
    }

    public void reverseState(){
        if(this.isAlive){
            this.makeDead();
        } else {
            this.makeAlive();
        }
    }



    public int getPositionX() {
        return positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setAliveColor(String aliveColor) {
        this.aliveColor = aliveColor;
        if(isAlive)
            this.setFill(Color.valueOf(aliveColor));
    }

    public void setDeadColor(String deadColor) {
        this.deadColor = deadColor;
        if(!isAlive)
            this.setFill(Color.valueOf(deadColor));
    }

    public Set<ICell> getAroundCells() {
        return aroundCells;
    }

    public void setAroundCells(Set<ICell> aroundCells) {
        this.aroundCells = aroundCells;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CellEllipse cellEllipse = (CellEllipse) o;
        return positionX == cellEllipse.positionX &&
                positionY == cellEllipse.positionY;
    }


    public double getShapeWidth(){
        return this.getRadiusX();
    }
    public double getShapeHeight(){
        return this.getRadiusY();
    }

    public void setShapeWidth(double width){
        this.setRadiusX(width);
    }
    public void setShapeHeight(double height){
        this.setRadiusY(height);
    }

    @Override
    public String toString() {
        return "Cell{" +
                "positionX:" + positionX +
                ", positionY:" + positionY +
                ", isAlive:" + isAlive +
                //", nbAround:" + aroundCells.size() +
                '}';
    }
}
