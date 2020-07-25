package fr.rom.gameoflife.objects;


import javafx.scene.Node;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


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

    public Set<ICell> getAroundCells(ICell cell){
        int x = cell.getPositionX();
        int y = cell.getPositionY();

        Set<ICell> result = new HashSet<>();
        try {
            result.add(this.getCellAtIndex(x-1, y-1));
            result.add(this.getCellAtIndex(x-1, y));
            result.add(this.getCellAtIndex(x-1, y+1));
            result.add(this.getCellAtIndex(x, y-1));
            result.add(this.getCellAtIndex(x, y+1));
            result.add(this.getCellAtIndex(x+1, y-1));
            result.add(this.getCellAtIndex(x+1, y));
            result.add(this.getCellAtIndex(x+1, y+1));

            return result;
        } catch (IndexOutOfBoundsException e){
            return null;
        }
    }

    public void addCell(ICell cell){ //TODO: trouver meilleur moyen que cast
        if(!(cell instanceof  Node)) return;
        this.add((Node) cell, cell.getPositionX(), cell.getPositionY());
    }
}