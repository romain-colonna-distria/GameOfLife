package fr.rom.gameoflife.controller;



import fr.rom.gameoflife.object.Cell;
import fr.rom.gameoflife.object.Grid;
import fr.rom.gameoflife.utils.PropertyValuesGetter;
import fr.rom.gameoflife.utils.Util;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;



public class GameOfLifeController {
    private Grid grid;
    private PropertyValuesGetter propertyValuesGetter;
    private ExecutorService pool;

    private Set<Cell> activeCells;

    private int propagationNumber = 0;
    private Cell lastClickedCell;
    private double xClick, yClick;

    private int actualCellWidth;
    private int actualCellHeight;
    private String actualAliveColor;
    private String actualDeadColor;

    private AtomicBoolean onPropagation = new AtomicBoolean(false);
    private AtomicLong refreshTimeMs;

    @FXML
    private AnchorPane gameAnchorPane;
    @FXML
    private Label generationNumberLabel;
    @FXML
    private Label onPropagationLabel;
    @FXML
    private Slider zoomSlider;
    @FXML
    private Label zoomLabel;
    @FXML
    private Slider speedSlider;
    @FXML
    private Label speedLabel;

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
     * Event handler détectant la pression d'une touche du clavier. Permet
     * de d'effectier un/des propagations.
     */
    private EventHandler<KeyEvent> onKeyPressed = new EventHandler<KeyEvent>() {
        @Override
        public void handle(KeyEvent event) {
            if(event.getCode().equals(KeyCode.ENTER)) {
                if (isOnPropagation()) {
                    stopPropagation();
                } else {
                    startPropagation();
                }
            } else if(event.getCode().equals(KeyCode.SPACE)){
                propagate();
                generationNumberLabel.setText(String.valueOf(++propagationNumber));
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
            Cell cell = grid.getCellAtCoordinates(event.getX(), event.getY());
            if(cell == null) return;

            if(cell.equals(lastClickedCell)) return;
            lastClickedCell = cell;

            cell.reverseState();
            activeCells.add(cell);
            activeCells.addAll(Objects.requireNonNull(Util.getAroundCells(cell, grid)));
        }
    };

    /**
     * Event handler détectant une pression de la sourie sur la grille. Permet
     * de modifer l'etat d'une cellule
     */
    private EventHandler<MouseEvent> onClickCell = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            if(event.getTarget() instanceof Grid) return; //évite de modifier l'etat d'une case lors du move mode

            Cell cell = grid.getCellAtCoordinates(event.getX(), event.getY());
            if(cell == null) return;

            cell.reverseState();
            activeCells.add(cell);
            activeCells.addAll(Objects.requireNonNull(Util.getAroundCells(cell, grid)));
        }
    };



    public void init(Stage stage) {
        initPropertyValuesGetter();

        this.actualCellWidth = Integer.parseInt(this.propertyValuesGetter.getPropertyValue("CELL_WIDTH"));
        this.actualCellHeight = Integer.parseInt(this.propertyValuesGetter.getPropertyValue("CELL_HEIGHT"));
        this.actualAliveColor = this.propertyValuesGetter.getPropertyValue("CELL_ALIVE_COLOR");
        this.actualDeadColor = this.propertyValuesGetter.getPropertyValue("CELL_DEAD_COLOR");

        int nbCols = Integer.parseInt(this.propertyValuesGetter.getPropertyValue("NB_COLOMNS"));
        int nbRws = Integer.parseInt(this.propertyValuesGetter.getPropertyValue("NB_ROWS"));

        this.grid = new Grid(nbCols, nbRws);
        this.grid.setOnMousePressed(onMousePressed);
        this.grid.setOnMouseDragged(onMouseDragForReverse);
        this.grid.setOnMouseClicked(onClickCell);

        Cell cell;
        for(int i = 0; i < nbCols; ++i){
            for(int j = 0; j < nbRws; ++j) {
                cell = new Cell(actualCellWidth, actualCellHeight, i, j);
                //cell.setAliveColor(actualAliveColor);
                //cell.setDeadColor(actualDeadColor);
                cell.makeDead();
                grid.addCell(cell);
            }
        }

        for(Cell c : grid.getCells()){
            c.setAroundCells(Util.getAroundCells(c, this.grid));
        }

        this.gameAnchorPane.getChildren().add(this.grid);
        //initGrid(nbCols, nbRws);
        //initCells(nbCols, nbRws);

        this.pool = Executors.newFixedThreadPool(Integer.parseInt(propertyValuesGetter.getPropertyValue("NB_SIMULTANEOUS_THREADS")));
        this.refreshTimeMs = new AtomicLong(Long.parseLong(propertyValuesGetter.getPropertyValue("REFRESH_TIME_MS")));
        this.activeCells = new HashSet<>();

        stage.getScene().setOnKeyPressed(this.onKeyPressed);
        stage.setOnCloseRequest((event -> {
            if (isOnPropagation()) stopPropagation();
            pool.shutdown();
        }));
    }

    public void startPropagation(){
        if(onPropagation.get()) return;
        onPropagation.set(true);

        Thread propagation = new Thread(new Propagation_Handler());
        propagation.start();

        Platform.runLater(() -> {
            onPropagationLabel.setText("on");
        });
    }

    public void stopPropagation(){
        if(!onPropagation.get()) return;

        onPropagation.set(false);
        Platform.runLater(() -> {
            onPropagationLabel.setText("off");
        });
    }

    //TODO: arrêter quand bouge plus
    public void propagate(){
        List<Cell> activeCells = new ArrayList<>(this.getActiveCells());

//        if(oldActiveCells.containsAll(activeCells) && activeCells.containsAll(oldActiveCells)) {
//            stopPropagation();
//            return;
//        } else if(activeCells.size() < 1){
//            stopPropagation();
//            return;
//        }
//        oldActiveCells = new HashSet<>(activeCells);

        Set<Cell> toAlive = new HashSet<>();
        Set<Cell> toDead = new HashSet<>();
        for(Cell cell : activeCells){
            int nbAroundCells = 0;
            Set<Cell> aroundCells = cell.getAroundCells();
            if(aroundCells == null) continue;

            for(Cell c : cell.getAroundCells())
                if(c.isAlive()) ++nbAroundCells;

            //TODO: pouvoir changer de règles de propagation facilement
            if(cell.isAlive()){
                if(nbAroundCells != 2 && nbAroundCells != 3){
                    toDead.add(cell);
                    this.activeCells.add(cell);
                    this.activeCells.addAll(Objects.requireNonNull(Util.getAroundCells(cell, grid)));
                }
            } else {
                if(nbAroundCells == 3){
                    toAlive.add(cell);
                    this.activeCells.add(cell);
                    this.activeCells.addAll(Objects.requireNonNull(Util.getAroundCells(cell, grid)));
                }
            }
        }

        for(Cell cell : toAlive) cell.makeAlive();
        for(Cell cell : toDead) cell.makeDead();
    }

    public Set<Cell> getActiveCells(){
        Set<Cell> tmp = new HashSet<>(this.activeCells);
        for(Cell cell : tmp) {
            int nbAroundCells = 0;
            Set<Cell> aroundCells = cell.getAroundCells();
            if(aroundCells == null) continue;

            for (Cell c : aroundCells) {
                if (c.isAlive()) ++nbAroundCells;
            }

            if (!cell.isAlive() && nbAroundCells < 1) this.activeCells.remove(cell);
        }

        return activeCells;
    }

    public boolean isOnPropagation() {
        return this.onPropagation.get();
    }

    private void initPropertyValuesGetter(){
        try {
            this.propertyValuesGetter = new PropertyValuesGetter("config.properties");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void initGrid(int nbCols, int nbRws){

    }

    private void initCells(int nbCols, int nbRws){

    }



    @FXML
    public void cleanGrid(Event event){
        onPropagation.set(false);
        propagationNumber = 0;

        for(Cell cell : getActiveCells()) cell.makeDead();

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

            for (Cell cell : getActiveCells()) {
                objectOut.writeObject(cell);
            }

            System.out.println("Le jeu a bien été sauvegardé.");
            objectOut.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //TODO: refaire marcher
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
            System.err.println("La sauvegarde n'a pas pu être chargée. Elle contient des erreurs.");
        }

        Cell cell;
        Cell tmp;
        try {
            while ((cell = (Cell) ois.readObject()) != null){
                //TODO: mettre a jour ces cellules
                tmp = grid.getCellAtCoordinates(cell.getX(), cell.getY());
                if(tmp == null) continue;

                if(cell.isAlive())
                    tmp.makeAlive();
                else {
                    tmp.makeDead();
                }
            }
        } catch (EOFException e){
            System.out.println("La partie a bien été chargée.");
        } catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    @FXML
    public void showSettings(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/settings_view.fxml"));
            VBox root = fxmlLoader.load();
            root.setPrefSize(600, 500);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));

            SettingsController controller = fxmlLoader.getController();
            controller.init();

            stage.show();
        } catch (IOException e){
            e.printStackTrace();
        }

    }

    //TODO: optimiser
    @FXML
    public void makeZoom(){
        List<Cell> cells = grid.getCells();
        this.actualCellWidth = (int) (Integer.parseInt(propertyValuesGetter.getPropertyValue("CELL_WIDTH")) * (zoomSlider.getValue() / 100));
        this.actualCellHeight = (int) (Integer.parseInt(propertyValuesGetter.getPropertyValue("CELL_HEIGHT")) * (zoomSlider.getValue() / 100));

        int nbThread = Integer.parseInt(propertyValuesGetter.getPropertyValue("NB_SIMULTANEOUS_THREADS"));
        int from = 0;
        for(int i = 0; i < nbThread; ++i) {
            if(from > cells.size()) break;
            pool.submit(new ZoomCell_Handler(cells.subList(from, from + cells.size() / nbThread)));
            from += cells.size() / nbThread;
        }

        Platform.runLater(() -> {
            zoomLabel.setText(String.valueOf((int)zoomSlider.getValue()));
        });
    }

    @FXML
    public void updateZoomLabel(){
        Platform.runLater(() -> {
            zoomLabel.setText(String.valueOf((int)zoomSlider.getValue()));
        });
    }

    @FXML
    public void changeSpeed(){
        refreshTimeMs.set((long) speedSlider.getValue());

        Platform.runLater(() -> {
            speedLabel.setText(refreshTimeMs.toString());
        });
    }

    @FXML
    public void setModeToMove(Event event){
        this.grid.setOnMouseDragged(this.onMouseDragForMovePlan);
    }

    @FXML
    public void setModeToReverse(Event event){
        this.grid.setOnMouseDragged(this.onMouseDragForReverse);
    }



    private class Propagation_Handler implements Runnable {
        @Override
        public void run() {
            while (onPropagation.get()){
                Platform.runLater(() -> {
                    propagate();
                    generationNumberLabel.setText(String.valueOf(++propagationNumber));
                });

                try {
                    Thread.sleep(refreshTimeMs.get());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class ZoomCell_Handler implements Runnable {
        List<Cell> cells;

        public ZoomCell_Handler(List<Cell> cells){
            this.cells = cells;
        }

        @Override
        public void run() {
            System.err.println("nbCells: " + cells.size());
            Platform.runLater(() -> {
                if(cells.size() < 1) return;

                for(Cell cell : cells){
                    cell.setWidth(actualCellWidth);
                    cell.setHeight(actualCellHeight);
                }
            });
        }
    }
}