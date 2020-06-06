package fr.rom.object;

import fr.rom.utils.Util;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;


public class Grid extends GridPane {
    private Grid instance;
    private Cell[][] cells;
    private ArrayList<Cell> newClickedCells = new ArrayList<>();

    private int nbColumns, nbRows;
    private double cellWidth, cellHeight;

    private Cell actualCell = new Cell();

    private double xClick;
    private double yClick;



    /**
     * Event handler détectant une pression de la sourie sur la grille. Permet
     * d'enregistrer les coordonnées de la pression pour permetre un bon déplacement de la grille.
     */
    private EventHandler<MouseEvent> onMousePressed =  new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            xClick = event.getX();
            yClick = event.getY();
        }
    };

    /**
     * Event handler détectant le déplacement de la sourie (sourie cliquée) sur la grille.
     * Permet de déplacer la grille.
     */
    private EventHandler<MouseEvent> onMouseDragForMovePlan =  new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            if(event.getSource() instanceof Grid){
                ((Grid) event.getSource()).setTranslateX(event.getX() - xClick + ((Grid) event.getSource()).getTranslateX());
                ((Grid) event.getSource()).setTranslateY(event.getY() - yClick + ((Grid) event.getSource()).getTranslateY());
                xClick = event.getX();
                yClick = event.getY();
            }
        }
    };

    /**
     * Event handler détectant le déplacement de la sourie (sourie cliquée) sur la grille.
     * Permet de modifier l'etat de plusieurs celules.
     */
    private EventHandler<MouseEvent> onMouseDragForReverse =  new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            Cell cell = Util.getCell(instance, event.getX(), event.getY());
            if(cell == null) return;
            if(cell.equals(actualCell)) return;
            actualCell = cell;
            reverse(cell);

            if(newClickedCells.contains(cell)){
                if(!cell.isAlive()) newClickedCells.remove(cell);
            } else {
                if(cell.isAlive()) newClickedCells.add(cell);
            }
        }
    };

    private transient EventHandler<MouseEvent> onClickCell = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            if(event.getTarget() instanceof Grid) return; //évite de modifier l'etat d'une case lors du move mode

            Cell cell = Util.getCell(instance, event.getX(), event.getY());
            reverse(cell);

            if(newClickedCells.contains(cell)){
                if(!cell.isAlive()) newClickedCells.remove(cell);
            } else {
                if(cell.isAlive()) newClickedCells.add(cell);
            }

            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };





    public Grid(int nbColumns, int nbRows, double cellWidth, double cellHeight){
        super();
        this.cells = new Cell[nbColumns][nbRows];
        //this.aliveCells = new Cell[nbColumns][nbRows];
        this.nbColumns = nbColumns;
        this.nbRows = nbRows;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        init();
        this.instance = this;
    }

    public void init(){

        Cell cell;
        for(int i = 0; i < nbColumns; ++i){
            for(int j = 0; j < nbRows; ++j) {
                cell = new Cell(cellWidth, cellHeight, i, j);
                cell.setDead();
                add(cell.getShape(), cell.getPositionX(), cell.getPositionY());
                cells[i][j] = cell;
            }
        }

        setOnMousePressed(onMousePressed);
        setOnMouseDragged(onMouseDragForMovePlan);
        setOnMouseClicked(onClickCell);
    }

    public void reverse(Cell cell){
        if(cell.getShape().getFill().equals(Util.CELL_ALIVE_COLOR)){
            cell.setDead();
        } else {
            cell.setAlive();
        }
    }

    public void setModeToMove(){
        setOnMouseDragged(onMouseDragForMovePlan);
    }

    public void setModeToReverse(){
        setOnMouseDragged(onMouseDragForReverse);

    }

    public void clearNewClickedCells(){
        newClickedCells = new ArrayList<>();
    }

    public Cell[][] getCells() {
        return cells;
    }

    public double getCellWidth() {
        return cellWidth;
    }

    public double getCellHeight() {
        return cellHeight;
    }

    public int getNbColumns() {
        return nbColumns;
    }

    public int getNbRows() {
        return nbRows;
    }

    public ArrayList<Cell> getNewClickedCells() {
        return newClickedCells;
    }
}
