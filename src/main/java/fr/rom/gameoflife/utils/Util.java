package fr.rom.gameoflife.utils;


import fr.rom.gameoflife.objects.Grid;
import fr.rom.gameoflife.objects.ICell;

import java.util.HashSet;
import java.util.Set;


public class Util {
    public static Set<ICell> getAroundCells(ICell cell, Grid grid){
        int x = cell.getPositionX();
        int y = cell.getPositionY();

        Set<ICell> result = new HashSet<>();
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
