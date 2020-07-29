package fr.rom.gameoflife.controller;


import fr.rom.gameoflife.objects.CellEllipse;
import fr.rom.gameoflife.objects.CellRectangle;
import fr.rom.gameoflife.objects.Grid;
import fr.rom.gameoflife.objects.ICell;
import fr.rom.gameoflife.utils.Properties;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;


public class GameOfLifeController {
    private Stage settingsStage;
    private Stage statisticsStage;
    private ExecutorService pool;

    private Grid grid;
    private boolean dragModeActive;

    private Set<ICell> activeCells;
    private ICell lastClickedCell;

    private int propagationNumber;
    private AtomicBoolean onPropagation;

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



    public void init() {
        this.onPropagation = new AtomicBoolean(false);
        this.pool = Executors.newFixedThreadPool(Properties.getInstance().getNbSimultaneousThreads());
        this.activeCells = new HashSet<>();
        this.propagationNumber = 0;
        this.dragModeActive = false;

        initGrid();

        this.grid.getScene().getWindow().getScene().addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            if(keyEvent.getCode().equals(KeyCode.ENTER)) {
                if (isOnPropagation()) stopPropagation();
                else startPropagation();
            } else if(keyEvent.getCode().equals(KeyCode.SPACE)){
                propagate();
            }
        });
        this.grid.getScene().getWindow().setOnCloseRequest((event -> {
            doOnClose();
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/init_view.fxml"));
                AnchorPane root = fxmlLoader.load();

                Stage stage = new Stage();
                stage.setTitle("Jeu de la vie v2.0");
                stage.setScene(new Scene(root));

                InitController controller = fxmlLoader.getController();
                controller.init(stage);

                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        this.zoomSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                zoomLabel.textProperty().setValue(String.valueOf((int)zoomSlider.getValue()))
        );

        this.speedSlider.valueProperty().addListener((bservable, oldValue, newValue) -> {
            speedLabel.textProperty().setValue(String.valueOf((int)speedSlider.getValue()));
            Properties.getInstance().setRefreshTimeMs((long) speedSlider.getValue());
        });

        createStatsFile();
    }

    private void createStatsFile(){
        File statisticsFile = new File("stats.txt");
        try {
            if(!statisticsFile.createNewFile()){
                if(statisticsFile.delete()) {
                    if(statisticsFile.createNewFile()) statisticsWriter = new FileWriter(statisticsFile);
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void doOnClose(){
        if(settingsStage != null) settingsStage.close();
        if(statisticsStage != null) statisticsStage.close();

        try {
            if (isOnPropagation()) stopPropagation();
            pool.shutdown();
            statisticsWriter.close();
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
        boolean gameUpdated = false;
        for(ICell cell : activeCells){
            int nbAliveAroundCells = 0;
            Set<ICell> aroundCells = cell.getAroundCells();
            if(aroundCells == null) continue;

            for(ICell c : cell.getAroundCells()) {
                if (c.isAlive()) ++nbAliveAroundCells;
            }

            if(cell.isAlive()){
                if(!Properties.getInstance().getStayAliveSet().contains(nbAliveAroundCells)){
                    toDead.add(cell);
                    addNewActiveCell(cell);
                    gameUpdated = true;
                }
            } else {
                if(Properties.getInstance().getComeAliveSet().contains(nbAliveAroundCells)){
                    toAlive.add(cell);
                    addNewActiveCell(cell);
                    gameUpdated = true;
                }
            }
        }

        if(!gameUpdated) {
            stopPropagation();
            return;
        }

        for(ICell cell : toAlive) cell.makeAlive();
        for(ICell cell : toDead) cell.makeDead();

        try {
            statisticsWriter.append(String.valueOf(propagationNumber)).append(";");
            statisticsWriter.append(String.valueOf(toAlive.size())).append("\n");
            statisticsWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        generationNumberLabel.setText(String.valueOf(++propagationNumber));
    }

    public Set<ICell> getActiveCells(){
        Set<ICell> tmp = new HashSet<>();
        for(ICell cell : this.activeCells){
            if(cell.isAlive()) {
                tmp.add(cell);
                if(cell.getAroundCells() == null) continue;
                tmp.addAll(cell.getAroundCells());
            }
        }
        this.activeCells = new HashSet<>(tmp);

        return activeCells;
    }

    private void addNewActiveCell(ICell cell){
        activeCells.add(cell);

        Set<ICell> around = this.grid.getAroundCells(cell);
        if(around == null) return;
        activeCells.addAll(around);
    }

    private void initGrid(){
        this.grid = new Grid(Properties.getInstance().getGridNbColumns(), Properties.getInstance().getGridNbRows());
        this.gameAnchorPane.getChildren().add(makeGridWithEventFilters(this.grid));

        initCells();
    }

    private Node makeGridWithEventFilters(final Grid grid) {
        final DragContext dragContext = new DragContext();
        final Group wrapGroup = new Group(grid);

        wrapGroup.addEventFilter(MouseEvent.MOUSE_PRESSED, mouseEvent -> {
            if (dragModeActive) {
                // remember initial mouse cursor coordinates
                // and node position
                dragContext.mouseAnchorX = mouseEvent.getX();
                dragContext.mouseAnchorY = mouseEvent.getY();
                dragContext.initialTranslateX = grid.getTranslateX();
                dragContext.initialTranslateY = grid.getTranslateY();
            } else {
                if(!(mouseEvent.getTarget() instanceof ICell)) return;
                ICell cell = (ICell) mouseEvent.getTarget();

                cell.reverseState();
                addNewActiveCell(cell);
            }
        });

        wrapGroup.addEventFilter(MouseEvent.MOUSE_DRAGGED, mouseEvent -> {
            if (dragModeActive) {
                // shift node from its initial position by delta
                // calculated from mouse cursor movement
                grid.setTranslateX(dragContext.initialTranslateX + mouseEvent.getX() - dragContext.mouseAnchorX);
                grid.setTranslateY(dragContext.initialTranslateY + mouseEvent.getY() - dragContext.mouseAnchorY);
            } else {
                if(!(mouseEvent.getPickResult().getIntersectedNode() instanceof ICell)) return;
                ICell cell = (ICell) mouseEvent.getPickResult().getIntersectedNode();

                if(cell.equals(lastClickedCell)) return;
                lastClickedCell = cell;

                cell.reverseState();
                addNewActiveCell(cell);
            }
        });

        return wrapGroup;
    }

    private void initCells(){
        ICell cell;
        Properties p = Properties.getInstance();
        for(int i = 0; i < p.getGridNbColumns(); ++i){
            for(int j = 0; j < p.getGridNbRows(); ++j) {
                if(p.getShapeString().equals("rectangle"))
                    cell = new CellRectangle(p.getCellWidth(), p.getCellHeight(), i, j);
                else if(p.getShapeString().equals("ovale"))
                    cell = new CellEllipse(p.getCellWidth(), p.getCellHeight(), i, j);
                else return;

                cell.setAliveColor(p.getCellAliveColor());
                cell.setDeadColor(p.getCellDeadColor());
                cell.makeDead();
                grid.addCell(cell);
            }
        }

        for(ICell c : grid.getCells()){
            c.setAroundCells(this.grid.getAroundCells(c));
        }
    }

    public void updateAliveColorCell(String newColor){
        Properties.getInstance().setCellAliveColor(newColor);
        for(ICell c : grid.getCells()){
            c.setAliveColor(newColor);
        }
    }

    public void updateDeadColorCell(String newColor){
        Properties.getInstance().setCellDeadColor(newColor);
        for(ICell c : grid.getCells()){
            c.setDeadColor(newColor);
        }
    }

    public boolean isOnPropagation() {
        return this.onPropagation.get();
    }


    @FXML
    public void saveState(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt"));

        File selectedFile = fileChooser.showSaveDialog(this.gameAnchorPane.getScene().getWindow());
        if(selectedFile == null) return;

        try {
            FileOutputStream fileOut = new FileOutputStream(selectedFile);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);

            for (ICell cell : getActiveCells()) {
                objectOut.writeObject(cell);
            }
            System.out.println("Le jeu a bien été sauvegardé.");
            objectOut.close();
            fileOut.close();
        } catch (Exception ex) {
            System.out.println("Le jeu n'a pas pu être sauvegardé.");
        }
    }

    @FXML
    public void loadSave(){ //TODO: passer a un moyen de stockage type JSON, XML...
        FileChooser fileChooser = new FileChooser();
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("text", ".txt"));

        File selectedFile = fileChooser.showOpenDialog(this.gameAnchorPane.getScene().getWindow());
        if(selectedFile == null) return;

        FileInputStream fis;
        ObjectInputStream ois;
        try {
            fis = new FileInputStream(selectedFile);
            ois = new ObjectInputStream(fis);
        } catch (IOException e) {
            System.err.println("La sauvegarde n'a pas pu être chargée. Elle contient des erreurs.");
            return;
        }

        ICell tmp;
        ICell cell;
        try {
            while ((tmp = (ICell) ois.readObject()) != null){
                cell = grid.getCellAtIndex(tmp.getPositionX(), tmp.getPositionY());
                if(cell == null) continue; //si la taille de la grille a changée par exemple

                if(tmp.isAlive()) cell.makeAlive();
                else cell.makeDead();

                addNewActiveCell(cell);
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
            this.settingsStage.initModality(Modality.WINDOW_MODAL);
            this.settingsStage.initOwner(this.gameAnchorPane.getScene().getWindow());
            this.settingsStage.setScene(new Scene(root));
            this.settingsStage.setOnCloseRequest((event -> this.settingsStage = null));

            SettingsController controller = fxmlLoader.getController();
            controller.init(this);

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
    public void resetGrid(){
        if(onPropagation.get()) stopPropagation();
        propagationNumber = 0;

        for(ICell cell : getActiveCells()) cell.makeDead();
        this.activeCells = new HashSet<>();

        createStatsFile();

        Platform.runLater(() -> {
            generationNumberLabel.setText(String.valueOf(propagationNumber));
            onPropagationLabel.setText("off");
        });
    }

    @FXML
    public void setModeToMove(){
        dragModeActive = true;
    }

    @FXML
    public void setModeToReverse(){
        dragModeActive = false;
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
            this.statisticsStage.initModality(Modality.WINDOW_MODAL);
            this.statisticsStage.initOwner(this.gameAnchorPane.getScene().getWindow());
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
        double newCellsWidth = Properties.getInstance().getCellWidth() * (zoomSlider.getValue() / 100);
        double newCellsHeight = Properties.getInstance().getCellHeight() * (zoomSlider.getValue() / 100);
        int nbThread = Properties.getInstance().getNbSimultaneousThreads();
        int from = 0;

        for(int i = 0; i < nbThread; ++i) {
            if(from > cells.size()) break;
            pool.submit(new Zoom_Handler(cells.subList(from, from + cells.size() / nbThread), newCellsWidth, newCellsHeight));
            from += cells.size() / nbThread;
        }
    }



    private class Propagation_Handler implements Runnable {
        @Override
        public void run() {
            while (onPropagation.get()){
                Platform.runLater(GameOfLifeController.this::propagate);

                try {
                    Thread.sleep(Properties.getInstance().getRefreshTimeMs());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class Zoom_Handler implements Runnable {
        private final List<ICell> cells;
        private final double cellWidth;
        private final double cellHeight;


        public Zoom_Handler(List<ICell> cells, double cellWidth, double cellHeight){
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

    private static final class DragContext {
        public double mouseAnchorX;
        public double mouseAnchorY;
        public double initialTranslateX;
        public double initialTranslateY;
    }
}