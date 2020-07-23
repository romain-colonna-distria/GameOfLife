package fr.rom.gameoflife.objects;


import javafx.scene.Node;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;


public class Grid extends GridPane {
    private final int nbColumns;
    private final int nbRows;


    public Grid(int nbColumns, int nbRows){
        super();

        this.nbColumns = nbColumns;
        this.nbRows = nbRows;
    }



    public List<ICell> getCells() {
        List<ICell> cells = new ArrayList<>();

        for(Node n : this.getChildren())
            cells.add((ICell) n);

        return cells;
    }

    public ICell getCellAtIndex(int i, int j) throws IndexOutOfBoundsException {
        if(i < 0 || i > this.nbColumns - 1 ) throw new IndexOutOfBoundsException();
        if(j < 0 || j > this.nbRows - 1 ) throw new IndexOutOfBoundsException();

        return (ICell) this.getChildren().get(i * nbColumns + j);
    }

    public void addCell(ICell cell){
        //TODO: trouver meilleur moyen que cast
        this.add((Node) cell, cell.getPositionX(), cell.getPositionY());
    }
}

/*
    public ICell getCellAtCoordinates(double x, double y){
        double cellWidth = this.getCells().get(0).getShapeWidth();
        double cellHeight = this.getCells().get(0).getShapeHeight();

        int i = (int)(x / cellWidth);
        int j = (int)(y / cellHeight);
        try{
            return this.getCellAtIndex(i, j);
        } catch (ArrayIndexOutOfBoundsException e){
            e.printStackTrace();
            return null;
        }
    }
 */