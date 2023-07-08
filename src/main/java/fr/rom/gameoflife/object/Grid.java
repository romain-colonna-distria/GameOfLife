package fr.rom.gameoflife.object;


import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Grid extends GridPane {
    private static final Logger LOGGER = LoggerFactory.getLogger(Grid.class);

    private final int nbColumns;
    private final int nbRows;


    public Grid(int nbColumns, int nbRows){
        super();

        this.nbColumns = nbColumns;
        this.nbRows = nbRows;
    }


    public List<Cell> getCells() {
        List<Cell> cells = new ArrayList<>();

        for(Node n : this.getChildren())
            cells.add((Cell) ((Group)n).getChildren().get(0));

        return cells;
    }

    public Cell getCellAtIndex(int i, int j) throws IndexOutOfBoundsException {
        if((i < 0 || i > this.nbColumns - 1) || (j < 0 || j > this.nbRows - 1)) return null;
        return (Cell) ((Group) this.getChildren().get(i * nbRows + j)).getChildren().get(0);
    }

    public Set<Cell> getAroundCells(Cell cell){
        int x = cell.getPositionX();
        int y = cell.getPositionY();

        Set<Cell> result = new HashSet<>();
        try {
            if(x-1 > 0 && y-1 > 0)
                result.add(this.getCellAtIndex(x-1, y-1));
            if(x-1 > 0 && y+1 < this.nbRows - 1)
                result.add(this.getCellAtIndex(x-1, y+1));
            if(x-1 > 0)
                result.add(this.getCellAtIndex(x-1, y));
            if(x+1 < this.nbColumns - 1 && y-1 > 0)
                result.add(this.getCellAtIndex(x+1, y-1));
            if(x+1 < this.nbColumns - 1 && y+1 < this.nbRows)
                result.add(this.getCellAtIndex(x+1, y+1));
            if(x+1 < this.nbColumns)
                result.add(this.getCellAtIndex(x+1, y));
            if(y-1 > 0)
                result.add(this.getCellAtIndex(x, y-1));
            if(y+1 < this.nbRows)
                result.add(this.getCellAtIndex(x, y+1));

            return result;
        } catch (IndexOutOfBoundsException e){
            LOGGER.error(e.getMessage(), e);
            return result;
        }
    }

    public void addCell(Cell cell){
        //Le group sert lors du changement de la taille du svg
        this.add(new Group(cell), cell.getPositionX(), cell.getPositionY());
    }
}