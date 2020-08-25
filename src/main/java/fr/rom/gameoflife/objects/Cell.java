package fr.rom.gameoflife.objects;

import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

import java.util.HashSet;
import java.util.Set;

/**
 * Class abstraite représentant une cellule.
 * Lien utile pour créer des SvgPath : https://yqnn.github.io/svg-path-editor/
 */
public class Cell extends SVGPath {
    private static final long serialVersionUID = 42L;

    private int positionX;
    private int positionY;
    private boolean isAlive = false;

    private String aliveColor = "black";
    private String deadColor = "white";

    private transient Set<Cell> aroundCells;

    public Cell(String path, double width, double height, int positionX, int positionY){
        super();
        this.setContent(path);

        this.positionX = positionX;
        this.positionY = positionY;
        this.aroundCells = new HashSet<>();

        this.setShapeWidth(width);
        this.setShapeHeight(height);
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
        if(this.isAlive) this.makeDead();
        else this.makeAlive();
    }

    public boolean isAlive() {
        return isAlive;
    }

    public int getPositionX() {
        return positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public Set<Cell> getAroundCells() {
        return aroundCells;
    }

    public void setAliveColor(String aliveColor) {
        this.aliveColor = aliveColor;
        if(isAlive) this.setFill(Color.valueOf(aliveColor));
    }

    public void setDeadColor(String deadColor) {
        this.deadColor = deadColor;
        if(!isAlive) this.setFill(Color.valueOf(deadColor));
    }

    public void setAroundCells(Set<Cell> aroundCells) {
        this.aroundCells = aroundCells;
    }

    public void setShapeWidth(double width){
        double originalWidth = this.prefWidth(-1);
        double scaleX = width / originalWidth;

        this.setScaleX(scaleX);
    }

    public void setShapeHeight(double height){
        double originalHeight = this.prefHeight(-1);
        double scaleY = height / originalHeight;

        this.setScaleY(scaleY);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cell cell = (Cell) o;
        return positionX == cell.getPositionX() && positionY == cell.getPositionY();
    }

    @Override
    public String toString() {
        return "Cell{" +
                "positionX:" + positionX +
                ", positionY:" + positionY +
                ", isAlive:" + isAlive +
                '}';
    }
}
