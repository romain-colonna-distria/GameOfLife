package fr.rom.gameoflife.controller;


import fr.rom.gameoflife.objects.CellEllipse;
import fr.rom.gameoflife.objects.CellRectangle;
import fr.rom.gameoflife.objects.Grid;
import fr.rom.gameoflife.objects.ICell;
import fr.rom.gameoflife.utils.Properties;
import fr.rom.gameoflife.utils.Util;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;


public class GameOfLifeController {
    private Stage settingsStage;
    private Stage statisticsStage;
    private Grid grid;
    private Properties properties;
    private ExecutorService pool;

    private Set<ICell> activeCells;

    private ICell lastClickedCell;
    private double xClick, yClick;

    private int propagationNumber = 0;
    private AtomicBoolean onPropagation;

    private boolean wantStatistics = true;
    private File statisticsFile;
    private FileWriter statisticsWriter;

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
    private final EventHandler<MouseEvent> onMousePressed =  new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            xClick = event.getX();
            yClick = event.getY();
        }
    };

    //TODO: mieux gérer le déplacement
    /**
     * Event handler détectant le déplacement de la sourie (sourie cliquée) sur la grille.
     * Permet de déplacer la grille.
     */
    private final EventHandler<MouseEvent> onMouseDragForMovePlan =  new EventHandler<MouseEvent>() {
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
    private final EventHandler<KeyEvent> onKeyPressed = new EventHandler<KeyEvent>() {
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
    private final EventHandler<MouseEvent> onMouseDragForReverse =  new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            if(!(event.getPickResult().getIntersectedNode() instanceof ICell)) return;
            ICell cell = (ICell) event.getPickResult().getIntersectedNode();

            if(cell.equals(lastClickedCell)) return;
            lastClickedCell = cell;

            cell.reverseState();
            activeCells.add(cell);

            Set<ICell> around = Util.getAroundCells(cell, grid);
            if(around == null) return;
            activeCells.addAll(around);
        }
    };

    /**
     * Event handler détectant une pression de la sourie sur la grille. Permet
     * de modifer l'etat d'une cellule
     */
    private final EventHandler<MouseEvent> onClickCell = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            if(event.getTarget() instanceof Grid) return; //évite de modifier l'etat d'une case lors du move mode
            if(!(event.getTarget() instanceof ICell)) return;
            ICell cell = (ICell) event.getTarget();

            cell.reverseState();
            activeCells.add(cell);

            Set<ICell> around = Util.getAroundCells(cell, grid);
            if(around == null) return;
            activeCells.addAll(around);
        }
    };



    public void init(Stage stage, Properties properties) {
        double time1 = System.currentTimeMillis();
        this.properties = properties;
        this.onPropagation = new AtomicBoolean(false);
        this.pool = Executors.newFixedThreadPool(properties.getNbSimultaneousThreads());
        this.activeCells = new HashSet<>();

        initGrid(properties.getGridNbColumns(), properties.getGridNbRows());

        stage.getScene().setOnKeyPressed(this.onKeyPressed);
        stage.setOnCloseRequest((event -> {
            doOnClose();
        }));
        double time2 = System.currentTimeMillis();
        System.out.println("Init grid: " + (time2 - time1) + " ms");
    }

    private void doOnClose(){
        if(settingsStage != null) settingsStage.close();
        if (isOnPropagation()) stopPropagation();
        pool.shutdown();

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/init_view.fxml"));
            AnchorPane root = fxmlLoader.load();

            Stage s = new Stage();
            s.setTitle("Jeu de la vie v2.0");
            s.setScene(new Scene(root));

            InitController controller = fxmlLoader.getController();
            controller.init(s);

            s.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startPropagation(){
        if(onPropagation.get()) return;
        onPropagation.set(true);

        Thread propagation = new Thread(new Propagation_Handler());
        propagation.start();

        Platform.runLater(() -> onPropagationLabel.setText("on"));
    }

    public void stopPropagation(){
        if(!onPropagation.get()) return;

        onPropagation.set(false);
        Platform.runLater(() -> onPropagationLabel.setText("off"));
    }

    public void propagate(){
        List<ICell> activeCells = new ArrayList<>(this.getActiveCells());

        Set<ICell> toAlive = new HashSet<>();
        Set<ICell> toDead = new HashSet<>();
        boolean changed = false;
        for(ICell cell : activeCells){
            int nbAroundCells = 0;
            Set<ICell> aroundCells = cell.getAroundCells();
            if(aroundCells == null) continue;

            for(ICell c : cell.getAroundCells()) {
                if (c.isAlive()) ++nbAroundCells;
            }

            if(cell.isAlive()){
                if(!this.properties.getStayAliveSet().contains(nbAroundCells)){
                    toDead.add(cell);
                    this.activeCells.add(cell);
                    this.activeCells.addAll(Objects.requireNonNull(Util.getAroundCells(cell, grid)));
                    changed = true;
                }
            } else {
                if(this.properties.getComeAliveSet().contains(nbAroundCells)){
                    toAlive.add(cell);
                    this.activeCells.add(cell);
                    this.activeCells.addAll(Objects.requireNonNull(Util.getAroundCells(cell, grid)));
                    changed = true;
                }
            }
        }

        if(!changed) {
            stopPropagation();
            return;
        }

        for(ICell cell : toAlive) cell.makeAlive();
        for(ICell cell : toDead) cell.makeDead();

        if(wantStatistics){
            if(statisticsFile == null){
                statisticsFile = new File("stats.txt");
                try {
                    if(!statisticsFile.createNewFile()){
                        if(statisticsFile.delete()) {
                            if(!statisticsFile.createNewFile()){
                                System.err.println("1?????");
                            }
                        } else {
                            System.err.println("2?????");
                        }
                    }
                    statisticsWriter = new FileWriter(statisticsFile);
                } catch (IOException e){
                    e.printStackTrace();
                }
            }

            try {
                statisticsWriter.append(String.valueOf(propagationNumber)).append(";");
                statisticsWriter.append(String.valueOf(toAlive.size())).append("\n");
                statisticsWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Set<ICell> getActiveCells(){
        Set<ICell> tmp = new HashSet<>(this.activeCells);
        for(ICell cell : tmp) {
            int nbAroundCells = 0;
            Set<ICell> aroundCells = cell.getAroundCells();
            if(aroundCells == null) continue;

            for (ICell c : aroundCells) {
                if (c.isAlive()) ++nbAroundCells;
            }

            if (!cell.isAlive() && nbAroundCells < 1) this.activeCells.remove(cell);
        }

        return activeCells;
    }

    public boolean isOnPropagation() {
        return this.onPropagation.get();
    }

    private void initGrid(int nbCols, int nbRws){
        this.grid = new Grid(nbCols, nbRws);
        this.gameAnchorPane.getChildren().add(this.grid);

        this.grid.setOnMousePressed(onMousePressed);
        this.grid.setOnMouseDragged(onMouseDragForReverse);
        this.grid.setOnMouseClicked(onClickCell);

        initCells(nbCols, nbRws);
    }

    private void initCells(int nbCols, int nbRws){
        ICell cell;

        if(this.properties.getShapeString().equals("rectangle")){
            for(int i = 0; i < nbCols; ++i){
                for(int j = 0; j < nbRws; ++j) {
                    cell = new CellRectangle(this.properties.getCellWidth(), this.properties.getCellHeight(), i, j);
                    cell.setAliveColor(this.properties.getCellAliveColor());
                    cell.setDeadColor(this.properties.getCellDeadColor());
                    cell.makeDead();
                    grid.addCell(cell);
                }
            }
        } else {
            for(int i = 0; i < nbCols; ++i){
                for(int j = 0; j < nbRws; ++j) {
                    cell = new CellEllipse(this.properties.getCellWidth(), this.properties.getCellHeight(), i, j);
                    cell.setAliveColor(this.properties.getCellAliveColor());
                    cell.setDeadColor(this.properties.getCellDeadColor());
                    cell.makeDead();
                    grid.addCell(cell);
                }
            }
        }


        for(ICell c : grid.getCells()){
            c.setAroundCells(Util.getAroundCells(c, this.grid));
        }
    }

    public void updateAliveColorCell(String newColor){
        this.properties.setCellAliveColor(newColor);
        for(ICell c : grid.getCells()){
            c.setAliveColor(newColor);
        }
    }

    public void updateDeadColorCell(String newColor){
        this.properties.setCellDeadColor(newColor);
        for(ICell c : grid.getCells()){
            c.setDeadColor(newColor);
        }
    }


    @FXML
    public void saveState(){
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);
        File selectedFile = fileChooser.showSaveDialog(null);
        try {
            FileOutputStream fileOut = new FileOutputStream(selectedFile);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);

            for (ICell cell : getActiveCells()) {
                objectOut.writeObject(cell);
            }
            System.out.println("Le jeu a bien été sauvegardé.");
            objectOut.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    public void loadSave(){ //TODO: passer a un moyen de stockage type JSON, XML...
        FileChooser fileChooser = new FileChooser();
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("text", ".txt"));
        File selectedFile = fileChooser.showOpenDialog(null);

        FileInputStream fis;
        ObjectInputStream ois = null;
        try {
            System.out.println(selectedFile);
            fis = new FileInputStream(selectedFile);
            ois = new ObjectInputStream(fis);
        } catch (IOException e) {
            System.err.println("La sauvegarde n'a pas pu être chargée. Elle contient des erreurs.");
        }

        ICell tmp;
        ICell cell;
        try {
            while ((tmp = (ICell) (ois != null ? ois.readObject() : null)) != null){
                cell = grid.getCellAtIndex(tmp.getPositionX(), tmp.getPositionY());
                if(cell == null) continue; //si la taille de la grille a changée par exemple

                if(tmp.isAlive())
                    cell.makeAlive();
                else {
                    cell.makeDead();
                }

                activeCells.add(cell);
                if(cell.getAroundCells() == null) continue;
                activeCells.addAll(cell.getAroundCells());
            }
        } catch (EOFException e){
            System.out.println("La partie a bien été chargée.");
        } catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    @FXML
    public void showSettings(){
        if(this.settingsStage != null) return;

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/settings_view.fxml"));
            VBox root = fxmlLoader.load();

            this.settingsStage = new Stage();
            this.settingsStage.setTitle("Paramètres");
            this.settingsStage.setAlwaysOnTop(true);
            this.settingsStage.setResizable(false);
            this.settingsStage.setScene(new Scene(root));

            this.settingsStage.setOnCloseRequest((event -> this.settingsStage = null));

            SettingsController controller = fxmlLoader.getController();
            controller.init(this, this.properties);

            this.settingsStage.show();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    @FXML
    public void exit(){
        doOnClose();
        Stage stage = (Stage) this.gameAnchorPane.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void cleanActives(){
        System.err.println("avant: " + this.activeCells.size());
        Set<ICell> tmp = new HashSet<>();
        for(ICell c : this.activeCells){
            if(c.isAlive()) {
                tmp.add(c);
                tmp.addAll(c.getAroundCells());
            }
        }
        activeCells = new HashSet<>(tmp);
        System.err.println("apres: " + this.activeCells.size());
    }

    @FXML
    public void resetGrid(){
        onPropagation.set(false);
        propagationNumber = 0;

        for(ICell cell : getActiveCells()) cell.makeDead();

        Platform.runLater(() -> {
            generationNumberLabel.setText(String.valueOf(propagationNumber));
            onPropagationLabel.setText("off");
        });
    }

    @FXML
    public void setModeToMove(){
        this.grid.setOnMouseDragged(this.onMouseDragForMovePlan);
    }

    @FXML
    public void setModeToReverse(){
        this.grid.setOnMouseDragged(this.onMouseDragForReverse);
    }

    @FXML
    public void showStats(){
        if(this.statisticsStage != null) return;

        try {
            Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
            double width = primaryScreenBounds.getWidth();
            double height = primaryScreenBounds.getHeight() - 25;

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/statistics_view.fxml"));
            AnchorPane root = fxmlLoader.load();
            root.setPrefSize(width, height);

            this.statisticsStage = new Stage();
            this.statisticsStage.setTitle("Statistiques");
            this.statisticsStage.setResizable(false);
            //this.statisticsStage.setAlwaysOnTop(true);
            this.statisticsStage.setScene(new Scene(root));
            this.statisticsStage.setOnCloseRequest((event -> this.statisticsStage = null));

            this.stopPropagation();

            StatisticsController controller = fxmlLoader.getController();
            controller.init();

            this.statisticsStage.show();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    //TODO: optimiser
    @FXML
    public void makeZoom(){
        List<ICell> cells = grid.getCells();
        double cellWidth = this.properties.getCellWidth() * (zoomSlider.getValue() / 100);
        double cellHeight = this.properties.getCellHeight() * (zoomSlider.getValue() / 100);

        //int nbThread = Runtime.getRuntime().availableProcessors();
        int nbThread = this.properties.getNbSimultaneousThreads();
        int from = 0;
        for(int i = 0; i < nbThread; ++i) {
            if(from > cells.size()) break;
            pool.submit(new ZoomCell_Handler(cells.subList(from, from + cells.size() / nbThread), cellWidth, cellHeight));
            from += cells.size() / nbThread;
        }

    }

    @FXML
    public void updateZoomLabel(){
        Platform.runLater(() -> zoomLabel.setText(String.valueOf((int)zoomSlider.getValue())));
    }

    @FXML
    public void changeSpeed(){
        this.properties.setRefreshTimeMs((long) speedSlider.getValue());

        Platform.runLater(() -> speedLabel.setText(String.valueOf(this.properties.getRefreshTimeMs())));
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
                    Thread.sleep(properties.getRefreshTimeMs());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class ZoomCell_Handler implements Runnable {
        private final List<ICell> cells;
        private final double cellWidth;
        private final double cellHeight;


        public ZoomCell_Handler(List<ICell> cells, double cellWidth, double cellHeight){
            this.cells = cells;
            this.cellWidth = cellWidth;
            this.cellHeight = cellHeight;
        }

        @Override
        public void run() {
            Platform.runLater(() -> {
                if(this.cells.size() < 1) return;

                for(ICell cell : cells){
                    cell.setShapeWidth(this.cellWidth);
                    cell.setShapeHeight(this.cellHeight);
                }
            });
        }
    }
}