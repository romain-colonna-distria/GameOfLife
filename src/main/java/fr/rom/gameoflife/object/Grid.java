package fr.rom.gameoflife.object;


import javafx.scene.Node;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;


public class Grid extends GridPane {
    private int nbColumns, nbRows;



    public Grid(int nbColumns, int nbRows){
        super();

        this.nbColumns = nbColumns;
        this.nbRows = nbRows;
    }



    public List<Cell> getCells() {
        List<Cell> cells = new ArrayList<>();

        for(Node n : this.getChildren())
            cells.add((Cell) n);

        return cells;
    }

    public Cell getCellAtIndex(int i, int j) throws IndexOutOfBoundsException {
        if(i < 0 || i > this.nbColumns - 1 ) throw new IndexOutOfBoundsException();
        if(j < 0 || j > this.nbRows - 1 ) throw new IndexOutOfBoundsException();

        return (Cell) this.getChildren().get(i * nbColumns + j);
    }

    public Cell getCellAtCoordinates(double x, double y){
        double cellWidth = this.getCells().get(0).getWidth();
        double cellHeight = this.getCells().get(0).getHeight();

        int i = (int)(x / cellWidth);
        int j = (int)(y / cellHeight);
        try{
            return this.getCellAtIndex(i, j);
        } catch (ArrayIndexOutOfBoundsException e){
            e.printStackTrace();
            return null;
        }
    }

    public void addCell(Cell cell){
        this.add(cell, cell.getPositionX(), cell.getPositionY());
    }
}