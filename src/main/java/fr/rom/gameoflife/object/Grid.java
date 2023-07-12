package fr.rom.gameoflife.object;


import fr.rom.gameoflife.utils.Coordinate;
import fr.rom.gameoflife.utils.HashMapSet;
import fr.rom.gameoflife.utils.Message;
import javafx.scene.Group;
import javafx.scene.layout.GridPane;

import java.util.*;


public class Grid extends GridPane implements CellStateListener {

    private final HashMapSet<Coordinate, Cell> adjacentCellsByCoordinate;
    private final Map<Coordinate, Cell> cellsByCoordinate;
    private final Set<Cell> aliveCells;

    public Grid(){
        super();
        this.adjacentCellsByCoordinate = new HashMapSet<>();
        this.cellsByCoordinate = new HashMap<>();
        this.aliveCells = new HashSet<>();
    }

    public void addCell(Cell cell){
        //Le group sert lors du changement de la taille du svg
        this.add(new Group(cell), cell.getCoordinate().getX(), cell.getCoordinate().getY());
        onCellStateUpdated(cell);
        cellsByCoordinate.put(cell.getCoordinate(), cell);

        Set<Coordinate> adjacentCoordinates = Coordinate.getAdjacentCoordinate(cell.getCoordinate());
        for(Coordinate adjacentCoordinate : adjacentCoordinates) {
            adjacentCellsByCoordinate.put(adjacentCoordinate, cell);
            adjacentCellsByCoordinate.put(cell.getCoordinate(), cellsByCoordinate.get(adjacentCoordinate));
        }
    }

    public Cell getCell(int x, int y) throws IndexOutOfBoundsException {
        Cell cell = cellsByCoordinate.get(new Coordinate(x, y));
        if(cell == null) throw new IndexOutOfBoundsException(Message.get("log.cellIndexOutOfBound", x, y));
        return cell;
    }

    public Collection<Cell> getCells() {
        return Collections.unmodifiableCollection(cellsByCoordinate.values());
    }

    public Collection<Cell> getAroundCells(Cell cell){
        Set<Cell> cells = adjacentCellsByCoordinate.get(cell.getCoordinate());
        cells.removeAll(Collections.singleton(null));
        return Collections.unmodifiableSet(cells);
    }

    public Collection<Cell> getAliveCells() {
        return Collections.unmodifiableSet(aliveCells);
    }

    @Override
    public void onCellStateUpdated(Cell cell) {
        if(cell.isAlive()) aliveCells.add(cell);
        else aliveCells.remove(cell);
    }
}