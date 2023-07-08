package fr.rom.gameoflife.object;

import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Class abstraite représentant une cellule.
 * Vous pouvez visiter ce <a href="https://yqnn.github.io/svg-path-editor/">site</a> pour créer des SvgPath.
 */
public class Cell extends SVGPath implements Serializable {
    @Serial
    private static final long serialVersionUID = 42L;

    private final int positionX;
    private final int positionY;
    private boolean isAlive = false;

    private String aliveColor = "#000000";
    private String deadColor = "#FFFFFF";

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
        this.setFill(Color.web(aliveColor));
    }

    public void makeDead(){
        this.isAlive = false;
        this.setFill(Color.web(deadColor));
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


    public String getAliveColor(){
        return this.aliveColor;
    }

    public void setAliveColor(String aliveColor) {
        this.aliveColor = aliveColor;
        if(isAlive) this.setFill(Color.web(aliveColor));
    }

    public String getDeadColor(){
        return this.deadColor;
    }

    public void setDeadColor(String deadColor) {
        this.deadColor = deadColor;
        if(!isAlive) this.setFill(Color.web(deadColor));
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
        if (!(o instanceof Cell cell)) return false;
        return positionX == cell.getPositionX() && positionY == cell.getPositionY();
    }

    @Override
    public int hashCode() {
        return Objects.hash(positionX, positionY, isAlive, aliveColor, deadColor);
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
