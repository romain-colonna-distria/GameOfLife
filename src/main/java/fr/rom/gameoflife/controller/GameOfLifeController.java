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
import javafx.scene.control.*;
import javafx.scene.input.*;
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
import org.apache.log4j.Logger;


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

    private final static Logger logger = Logger.getLogger(GameOfLifeController.class);

    @FXML
    private AnchorPane gameAnchorPane;
    @FXML
    private MenuItem saveMenuItem;
    @FXML
    private MenuItem loadMenuItem;
    @FXML
    private MenuItem settingsMenuItem;
    @FXML
    private MenuItem exitMenuButton;
    @FXML
    private MenuItem multiplePropagationMenuItem;
    @FXML
    private MenuItem singlePropagationMenuItem;
    @FXML
    private CheckMenuItem moveModeMenuItem;
    @FXML
    private CheckMenuItem reverseModeMenuItem;
    @FXML
    private MenuItem statsMenuItem;
    @FXML
    private MenuItem resetMenuItem;
    @FXML
    private MenuItem aboutMenuItem;
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
        logger.info("Initialisation d'une nouvelle partie ("
                + Properties.getInstance().getGridNbColumns() + " colonnes/"
                + Properties.getInstance().getGridNbRows() + " lignes)...");
        double start = System.currentTimeMillis();

        this.pool = Executors.newFixedThreadPool(2);
        this.dragModeActive = false;
        this.activeCells = new HashSet<>();
        this.propagationNumber = 0;
        this.onPropagation = new AtomicBoolean(false);

        this.speedSlider.setValue(Properties.getInstance().getRefreshTimeMs());
        this.speedLabel.setText(String.valueOf(Properties.getInstance().getRefreshTimeMs()));
        this.reverseModeMenuItem.setSelected(true);

        initGrid();
        initListeners();
        addShortcuts();
        initStatsFileWritter();

        double end =  System.currentTimeMillis();
        logger.info("Partie initialisé (" + (end - start) + "ms)");
    }

    private void initListeners(){
        this.grid.getScene().getWindow().setOnCloseRequest((event -> {
            doOnClose();
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/init_view.fxml"));
                AnchorPane root = fxmlLoader.load();

                Stage stage = new Stage();
                stage.setTitle("Jeu de la vie v2.0");
                stage.setScene(new Scene(root));

                InitController controller = fxmlLoader.getController();
                controller.init(stage);

                stage.show();
            } catch (Exception e) {
                logger.fatal(e.getMessage());
                System.exit(1);
            }
        }));
        this.zoomSlider.valueProperty().addListener((observable, oldValue, newValue) -> zoomLabel.textProperty().setValue(String.valueOf((int)zoomSlider.getValue())));
        this.speedSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            speedLabel.textProperty().setValue(String.valueOf((int)speedSlider.getValue()));
            Properties.getInstance().setRefreshTimeMs((long) speedSlider.getValue());
        });
        this.moveModeMenuItem.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue) {
                this.dragModeActive = true;
                this.reverseModeMenuItem.setSelected(false);
            } else {
                this.moveModeMenuItem.setSelected(!this.reverseModeMenuItem.isSelected());
            }
        });
        this.reverseModeMenuItem.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue) {
                this.dragModeActive = false;
                this.moveModeMenuItem.setSelected(false);
            } else {
                this.reverseModeMenuItem.setSelected(!this.moveModeMenuItem.isSelected());
            }
        });
    }

    private void addShortcuts(){
        saveMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        loadMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN));
        settingsMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.E));
        exitMenuButton.setAccelerator(new KeyCodeCombination(KeyCode.ESCAPE, KeyCombination.CONTROL_DOWN));

        multiplePropagationMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.ENTER));
        singlePropagationMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.SPACE));
        reverseModeMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.P));
        moveModeMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.O));
        statsMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.L, KeyCombination.ALT_DOWN));
        resetMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.BACK_SPACE));

        aboutMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.A));
    }

    private void initStatsFileWritter(){
        File statisticsFile = new File("stats.txt");
        try {
            if(!statisticsFile.createNewFile()){
                if(statisticsFile.delete()) {
                    if(statisticsFile.createNewFile()) statisticsWriter = new FileWriter(statisticsFile);
                    else logger.warn("Un problème est survenu lors de la création du fichir stats.txt");
                } else {
                    logger.warn("Un problème est survenu lors de la création du fichir stats.txt");
                }
            }
        } catch (IOException e){
            logger.error(e.getMessage());
        }
    }

    private void doOnClose(){
        if(settingsStage != null) settingsStage.close();
        if(statisticsStage != null) statisticsStage.close();
        if(onPropagation.get()) stopPropagation();

        logger.info("Fin de la partie");
        try {
            pool.shutdown();
            statisticsWriter.close();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public void startPropagation(){
        if(onPropagation.get()) return;
        onPropagation.set(true);

        pool.submit(new Propagation_Handler());

        Platform.runLater(() -> onPropagationLabel.setText("on"));
        logger.info("Propagation ON");
    }

    public void stopPropagation(){
        if(!onPropagation.get()) return;
        onPropagation.set(false);

        Platform.runLater(() -> onPropagationLabel.setText("off"));
        logger.info("Propagation OFF");
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
        generationNumberLabel.setText(String.valueOf(++propagationNumber));

        try {
            statisticsWriter.append(String.valueOf(propagationNumber)).append(";");
            statisticsWriter.append(String.valueOf(toAlive.size())).append("\n");
            statisticsWriter.flush();
        } catch (IOException e) {
            //peut être déclanché si on ferme le jeu pendant la propagation
            logger.error(e.getMessage());
        }
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
            logger.info("La partie a bien été sauvegardée");
            objectOut.close();
            fileOut.close();
        } catch (Exception ex) {
            logger.error("La partie n'a pas pu être sauvegardé : " + ex.getMessage());
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
            logger.error("La sauvegarde n'a pas pu être chargée : " + e.getMessage());
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
            logger.info("La partie a bien été chargée");
        } catch (IOException | ClassNotFoundException e){
            logger.error("La sauvegarde contient une erreur : " + e.getMessage());
        }
    }

    @FXML
    public void showSettings(){
        if(this.settingsStage != null) return;

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/settings_view.fxml"));
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
            logger.error(e.getMessage());
        }
    }

    @FXML
    public void exit(){
        doOnClose();
        Stage stage = (Stage) this.gameAnchorPane.getScene().getWindow();
        stage.close();
        logger.info("Fin de la session");
    }

    @FXML
    public void clickMultiplePropagationMenuItem(){
        if (onPropagation.get()){
            stopPropagation();
            multiplePropagationMenuItem.setText("Lancer propagation");
        }
        else {
            startPropagation();
            multiplePropagationMenuItem.setText("Arrêter propagation");
        }
    }

    @FXML
    public void clickSinglePropagationMenuItem(){
        propagate();
    }

    @FXML
    public void resetGrid(){
        if(onPropagation.get()) stopPropagation();
        propagationNumber = 0;

        for(ICell cell : getActiveCells()) cell.makeDead();
        this.activeCells = new HashSet<>();

        initStatsFileWritter();

        Platform.runLater(() -> {
            generationNumberLabel.setText(String.valueOf(propagationNumber));
            onPropagationLabel.setText("off");
        });
        logger.info("Grille réinitialisée");
    }

    @FXML
    public void showStats(){
        if(this.statisticsStage != null) return;

        try {
            Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
            double width = primaryScreenBounds.getWidth();
            double height = primaryScreenBounds.getHeight() - 25;

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/statistics_view.fxml"));
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
            logger.error(e.getMessage());
        }
    }

    @FXML //TODO: optimiser
    public void makeZoom(){
        logger.info("Application du zoom...");
        List<ICell> cells = grid.getCells();
        double newCellsWidth = Properties.getInstance().getCellWidth() * (zoomSlider.getValue() / 100);
        double newCellsHeight = Properties.getInstance().getCellHeight() * (zoomSlider.getValue() / 100);
        pool.submit(new Zoom_Handler(cells, newCellsWidth, newCellsHeight));
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
                double start = System.currentTimeMillis();
                if(this.cells.size() < 1) return;

                for(ICell cell : cells){
                    cell.setShapeWidth(this.cellWidth);
                    cell.setShapeHeight(this.cellHeight);
                }

                double end = System.currentTimeMillis();
                logger.info("Zoom appliqué par " + Thread.currentThread().getName() + " en " + (end - start) + "ms");
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