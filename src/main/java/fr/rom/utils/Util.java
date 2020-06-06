package fr.rom.utils;

import fr.rom.object.Cell;
import fr.rom.object.Grid;
import javafx.scene.paint.Color;

public class Util {
    public static Color CELL_ALIVE_COLOR = Color.SADDLEBROWN;
    public static Color CELL_DEAD_COLOR = Color.LIGHTSKYBLUE;


    public static int getAroudAlive(Cell cell, Cell[][] cells){
        int x = cell.getPositionX();
        int y = cell.getPositionY();

        int nbAlive = 0;

        if(x == 0 || y == 0) return 0;
        if(x >= cells.length - 1 || y >= cells[0].length - 1) return 0;

        if(cells[x-1][y-1].isAlive()) {
            ++nbAlive;
        }
        if(cells[x-1][y].isAlive()) {
            ++nbAlive;
        }
        if(cells[x-1][y+1].isAlive()) {
            ++nbAlive;
        }

        if(cells[x][y-1].isAlive()) {
            ++nbAlive;
        }
        if(cells[x][y+1].isAlive()) {
            ++nbAlive;
        }

        if(cells[x+1][y-1].isAlive()) {
            ++nbAlive;
        }
        if(cells[x+1][y].isAlive()) {
            ++nbAlive;
        }
        if(cells[x+1][y+1].isAlive()) {
            ++nbAlive;
        }

        return nbAlive;
    }

    public static Cell getCell(Grid grid, double x, double y){
        if(x > grid.getNbColumns() * grid.getCellWidth()) return null;
        if(y > grid.getNbRows() * grid.getCellHeight()) return null;

        int i = (int)(x / grid.getCellWidth());
        int j = (int)(y / grid.getCellHeight());

        Cell cell;
        try{
            cell = grid.getCells()[i][j];
        } catch (ArrayIndexOutOfBoundsException e){
            return null;
        }

        return cell;
    }

    public static void setCellAliveColor(Color cellAliveColor) {
        CELL_ALIVE_COLOR = cellAliveColor;
    }

    public static void setCellDeadColor(Color cellDeadColor) {
        CELL_DEAD_COLOR = cellDeadColor;
    }
}
