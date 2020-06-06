package fr.rom;

import fr.rom.object.Cell;
import fr.rom.object.Grid;
import fr.rom.utils.Util;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class GameOfLifeController {
    private Grid grid;
    private ArrayList<Cell> aroundCells;

    private final int NB_COLS = 300 ;
    private final int NB_ROWS = 300 ;

    private AtomicBoolean onPropagation = new AtomicBoolean(false);
    private AtomicLong refreshTimeMs = new AtomicLong(200);

    private int propagationNumber = 0;

    @FXML
    private AnchorPane gameAnchorPane;
    @FXML
    private Label generationNumberLabel;
    @FXML
    private Label onPropagationLabel;
    @FXML
    private MenuItem aliveMenuItem;
    @FXML
    private MenuItem deadMenuItem;
    @FXML
    private MenuItem bonusMenuItem;
    @FXML
    private HBox hbox;





    public void init(Stage stage){
        int size = 10;
        grid = new Grid(NB_COLS, NB_ROWS, size, size);
        aroundCells = new ArrayList<>(NB_COLS * NB_ROWS / 2);

        gameAnchorPane.getChildren().add(grid);

        ColorPicker alivePicker = new ColorPicker(Util.CELL_ALIVE_COLOR);
        ColorPicker deadPicker = new ColorPicker(Util.CELL_DEAD_COLOR);
        ColorPicker bonusPicker = new ColorPicker(Color.WHITE);
        alivePicker.getStyleClass().add("button");
        deadPicker.getStyleClass().add("button");
        bonusPicker.getStyleClass().add("button");

        alivePicker.setOnAction(event -> {
            Util.setCellAliveColor(alivePicker.getValue());
            refreshGrid();
        });
        deadPicker.setOnAction(event -> {
            Util.setCellDeadColor(deadPicker.getValue());
            refreshGrid();
        });
        bonusPicker.setOnAction((event -> {
            Util.setCellDeadColor(bonusPicker.getValue());
            refresh();
        }));

        aliveMenuItem.setGraphic(alivePicker);
        deadMenuItem.setGraphic(deadPicker);
        bonusMenuItem.setGraphic(bonusPicker);



        stage.getScene().setOnKeyPressed((event -> {
            if(event.getCode().equals(KeyCode.ENTER)) {
                if (isOnPropagation()) {
                    stopPropagation();
                } else {
                    startPropagation();
                }
            } else if(event.getCode().equals(KeyCode.SPACE)){
                propagate();
            }
        }));

        stage.setOnCloseRequest((event -> {
            if (isOnPropagation()) stopPropagation();
        }));
    }


    public void startPropagation(){
        onPropagation.set(true);
        Thread propagation = new Thread(new Propagation_Handler());
        propagation.start();
        Platform.runLater(() -> {
            onPropagationLabel.setText("on");
        });
    }


    public void stopPropagation(){
        onPropagation.set(false);
        Platform.runLater(() -> {
            onPropagationLabel.setText("off");
        });
    }


    public void propagate(){
        for(int x = 0; x < NB_COLS; ++x){
            for(int y = 0; y < NB_ROWS; ++y){
                int nbAround = Util.getAroudAlive(grid.getCells()[x][y], grid.getCells());

                if(grid.getCells()[x][y].isAlive() || nbAround == 3){
                    grid.getCells()[x][y].setNbAround(nbAround);
                    aroundCells.add(grid.getCells()[x][y]);
                }
            }
        }

        for(Cell cell : aroundCells){
            if(cell.isAlive()){
                if(cell.getNbAround() != 2 && cell.getNbAround() != 3){
                    grid.getCells()[cell.getPositionX()][cell.getPositionY()].setDead();
                    //aroundCells.remove(grid.getCells()[cell.getPositionX()][cell.getPositionY()]);
                }
            } else {
                if(cell.getNbAround() == 3){
                    grid.getCells()[cell.getPositionX()][cell.getPositionY()].setAlive();

                }
            }
        }


        generationNumberLabel.setText(String.valueOf(++propagationNumber));
    }

    @FXML
    public void cleanGrid(Event event){
        if(grid.getNewClickedCells().size() > 0)
            aroundCells.addAll(grid.getNewClickedCells());

        for(Cell cell : aroundCells){
            if(cell.getShape() == null) continue;
            cell.setDead();
        }
        onPropagation.set(false);
        propagationNumber = 0;
        aroundCells.clear();
        refreshGrid();
        Platform.runLater(() -> {
            generationNumberLabel.setText(String.valueOf(propagationNumber));
            onPropagationLabel.setText("off");
        });
    }

    @FXML
    public void saveState(Event event){
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showSaveDialog(null);
        try {
            FileOutputStream fileOut = new FileOutputStream(selectedFile);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);


            if(grid.getNewClickedCells().size() > 0) {
                aroundCells.addAll(grid.getNewClickedCells());
                grid.clearNewClickedCells();
            }

            for (Cell cell : aroundCells) {
                objectOut.writeObject(cell);
            }

            objectOut.close();
            System.out.println("The Object was succesfully written to a file");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    public void loadSave(Event event){ //TODO: passer a un moyen de stockage type JSON, XML...
        FileChooser fileChooser = new FileChooser();
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("text", ".txt"));
        File selectedFile = fileChooser.showOpenDialog(null);

        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = new FileInputStream(selectedFile);
            ois = new ObjectInputStream(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }

        aroundCells.clear();

        Cell cell;
        try {
            while ((cell = (Cell) ois.readObject()) != null){
                aroundCells.add(cell);
            }
        } catch (EOFException e){
            refresh();
            System.out.println("The Object was succesfully readen from a file");
        } catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    @FXML
    public void setModeToMove(Event event){
        grid.setModeToMove();
    }

    @FXML
    public void setModeToReverse(Event event){
        grid.setModeToReverse();
    }


    public void refresh(){
        for (Cell cell : aroundCells) {
            if(cell.isAlive()) grid.getCells()[cell.getPositionX()][cell.getPositionY()].setAlive();
            else grid.getCells()[cell.getPositionX()][cell.getPositionY()].setDead();
        }
    }

    public void refreshGrid(){
        for(int x = 0; x < NB_COLS; ++x){
            for(int y = 0; y < NB_ROWS; ++y){
                if(grid.getCells()[x][y].isAlive()) grid.getCells()[x][y].setAlive();
                else grid.getCells()[x][y].setDead();
            }
        }
    }


    private class Propagation_Handler implements Runnable {
        @Override
        public void run() {
            while (onPropagation.get()){
                Platform.runLater(() -> propagate());

                try {
                    Thread.sleep(refreshTimeMs.get());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public boolean isOnPropagation() {
        return onPropagation.get();
    }
}
