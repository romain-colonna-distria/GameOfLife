package fr.rom.gameoflife.object;

import fr.rom.gameoflife.utils.Coordinate;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Class représentant une cellule.
 * Vous pouvez visiter ce <a href="https://yqnn.github.io/svg-path-editor/">site</a> pour créer des SvgPath.
 */
public class Cell extends SVGPath implements Serializable {
    @Serial
    private static final long serialVersionUID = 42L;

    private final transient Set<CellStateListener> stateListeners;

    private Coordinate coordinate;
    private boolean isAlive;
    private String aliveColor;
    private String deadColor;

    public Cell(final String path, final double width, final double height, final Coordinate coordinate, final String aliveColor, final String deadColor, final boolean isAlive){
        super();
        this.setContent(path);
        this.stateListeners = new HashSet<>();
        this.coordinate = coordinate;
        this.aliveColor = aliveColor;
        this.deadColor = deadColor;
        this.setWidth(width);
        this.setHeight(height);
        if(isAlive) this.makeAlive();
        else this.makeDead();
    }

    public void makeAlive(){
        this.isAlive = true;
        this.setFill(Color.web(this.aliveColor));
        this.stateListeners.forEach(l -> l.onCellStateUpdated(this));
    }

    public void makeDead(){
        this.isAlive = false;
        this.setFill(Color.web(this.deadColor));
        this.stateListeners.forEach(l -> l.onCellStateUpdated(this));
    }

    public void reverseState(){
        if(this.isAlive) this.makeDead();
        else this.makeAlive();
        this.stateListeners.forEach(l -> l.onCellStateUpdated(this));
    }

    public boolean isAlive() {
        return this.isAlive;
    }

    public Coordinate getCoordinate() {
        return this.coordinate;
    }

    public void setCoordinate(final Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public String getAliveColor(){
        return this.aliveColor;
    }

    public void setAliveColor(final String aliveColor) {
        this.aliveColor = aliveColor;
        if(this.isAlive) this.setFill(Color.web(aliveColor));
    }

    public String getDeadColor(){
        return this.deadColor;
    }

    public void setDeadColor(final String deadColor) {
        this.deadColor = deadColor;
        if(!this.isAlive) this.setFill(Color.web(deadColor));
    }

    public void setWidth(final double width){
        double originalWidth = this.prefWidth(-1);
        double scaleX = width / originalWidth;

        this.setScaleX(scaleX);
    }

    public void setHeight(final double height){
        double originalHeight = this.prefHeight(-1);
        double scaleY = height / originalHeight;

        this.setScaleY(scaleY);
    }

    public void addStateListener(final CellStateListener stateListener) {
        this.stateListeners.add(stateListener);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cell cell)) return false;
        return this.isAlive == cell.isAlive && this.coordinate.equals(cell.coordinate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.coordinate, this.isAlive);
    }

    @Override
    public String toString() {
        return "Cell{" +
                "positionX:" + this.coordinate.getX() +
                ", positionY:" + this.coordinate.getY() +
                ", isAlive:" + this.isAlive +
                '}';
    }
}
