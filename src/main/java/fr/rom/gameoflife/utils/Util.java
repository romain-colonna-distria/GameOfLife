package fr.rom.gameoflife.utils;


import fr.rom.gameoflife.object.Cell;
import fr.rom.gameoflife.object.Grid;


import java.util.HashSet;
import java.util.Set;


public class Util {
    public static Set<Cell> getAroundCells(Cell cell, Grid grid){
        int x = cell.getPositionX();
        int y = cell.getPositionY();

        Set<Cell> result = new HashSet<>();
        try {
            result.add(grid.getCellAtIndex(x-1, y-1));
            result.add(grid.getCellAtIndex(x-1, y));
            result.add(grid.getCellAtIndex(x-1, y+1));
            result.add(grid.getCellAtIndex(x, y-1));
            result.add(grid.getCellAtIndex(x, y+1));
            result.add(grid.getCellAtIndex(x+1, y-1));
            result.add(grid.getCellAtIndex(x+1, y));
            result.add(grid.getCellAtIndex(x+1, y+1));

            return result;
        } catch (IndexOutOfBoundsException e){
            return null;
        }
    }

}
