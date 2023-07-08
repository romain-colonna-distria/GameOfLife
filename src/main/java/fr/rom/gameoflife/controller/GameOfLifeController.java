package fr.rom.gameoflife.controller;


import fr.rom.gameoflife.object.Grid;
import fr.rom.gameoflife.object.Cell;
import fr.rom.gameoflife.property.GameProps;
import fr.rom.gameoflife.controller.uicontext.DragContext;
import fr.rom.gameoflife.controller.uicontext.SelectionContext;
import fr.rom.gameoflife.utils.Message;

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
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;


public class GameOfLifeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameOfLifeController.class);

    private Stage settingsStage;
    private Stage statisticsStage;
    private ExecutorService pool;

    private Grid grid;
    private GameMode mode;

    private Set<Cell> activeCells;
    private Cell lastClickedCell;

    private SelectionContext selectContext;
    private ArrayList<Cell> selectedCells;
    private ArrayList<Cell> copiedSelection;

    private int propagationNumber;
    private AtomicBoolean onPropagation;

    private FileWriter statisticsWriter;

    @FXML
    private AnchorPane gameAnchorPane;

    @FXML
    private Menu fileMenu;
    @FXML
    private MenuItem saveMenuItem;
    @FXML
    private MenuItem loadMenuItem;
    @FXML
    private MenuItem settingsMenuItem;
    @FXML
    private MenuItem exitMenuButton;

    @FXML
    private Menu editMenu;
    @FXML
    private MenuItem multiplePropagationMenuItem;
    @FXML
    private MenuItem singlePropagationMenuItem;
    @FXML
    private CheckMenuItem shiftModeMenuItem;
    @FXML
    private CheckMenuItem reverseModeMenuItem;
    @FXML
    private CheckMenuItem selectModeMenuItem;
    @FXML
    private MenuItem copySelectionMenuItem;
    @FXML
    private MenuItem pasteSelectionMenuItem;
    @FXML
    private MenuItem statsMenuItem;
    @FXML
    private MenuItem resetMenuItem;

    @FXML
    private Menu helpMenu;
    @FXML
    private MenuItem aboutMenuItem;

    @FXML
    private Label propagationLabel;
    @FXML
    private Label onPropagationLabel;

    @FXML
    private Label generationLabel;
    @FXML
    private Label generationNumberLabel;

    @FXML
    private Label zoomLabel;
    @FXML
    private Slider zoomSlider;
    @FXML
    private Label zoomPercentLabel;

    @FXML
    private Label refreshLabel;
    @FXML
    private Slider refreshSlider;
    @FXML
    private Label refreshValueLabel;



    public void init() {
        LOGGER.info(Message.get("log.initGame", GameProps.get().getNbColumns(), GameProps.get().getNbRows()));
        double start = System.currentTimeMillis();

        this.pool = Executors.newFixedThreadPool(2);
        this.mode = GameMode.REVERSE_MODE;
        this.activeCells = new HashSet<>();
        this.propagationNumber = 0;
        this.onPropagation = new AtomicBoolean(false);

        this.refreshSlider.setValue(GameProps.get().getRefreshTimeMs());
        this.refreshValueLabel.setText(String.valueOf(GameProps.get().getRefreshTimeMs()));
        this.reverseModeMenuItem.setSelected(true);

        this.selectContext = new SelectionContext();
        this.selectedCells = new ArrayList<>();

        initGrid();
        initListeners();
        addShortcuts();
        initStatsFileWriter();
        initTextWithLocaleLanguage();

        double end =  System.currentTimeMillis();
        LOGGER.info(Message.get("log.gameInitialized", end - start));
    }

    private void initListeners(){
        this.grid.getScene().getWindow().setOnCloseRequest((event -> {
            doOnClose();
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/init_view.fxml"));
                AnchorPane root = fxmlLoader.load();

                Stage stage = new Stage();
                stage.titleProperty().bind(Message.createStringBinding("window.init.title"));
                stage.setScene(new Scene(root));

                InitController controller = fxmlLoader.getController();
                controller.init(stage);

                stage.show();
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                System.exit(1);
            }
        }));
        this.zoomSlider.valueProperty().addListener((observable, oldValue, newValue) -> zoomPercentLabel.textProperty().setValue(String.valueOf((int)zoomSlider.getValue())));
        this.refreshSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            refreshValueLabel.textProperty().setValue(String.valueOf((int) refreshSlider.getValue()));
            GameProps.get().setRefreshTimeMs((int)refreshSlider.getValue());
        });
        this.shiftModeMenuItem.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(Boolean.TRUE.equals(newValue)) {
                this.mode = GameMode.SHIFT_MODE;
                this.reverseModeMenuItem.setSelected(false);
                this.selectModeMenuItem.setSelected(false);
            } else {
                this.shiftModeMenuItem.setSelected(!this.reverseModeMenuItem.isSelected() && !this.selectModeMenuItem.isSelected());
            }
        });
        this.reverseModeMenuItem.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(Boolean.TRUE.equals(newValue)) {
                this.mode = GameMode.REVERSE_MODE;
                this.shiftModeMenuItem.setSelected(false);
                this.selectModeMenuItem.setSelected(false);
            } else {
                this.reverseModeMenuItem.setSelected(!this.shiftModeMenuItem.isSelected() && !this.selectModeMenuItem.isSelected());
            }
        });
        this.selectModeMenuItem.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(Boolean.TRUE.equals(newValue)) {
                this.mode = GameMode.SELECT_MODE;
                this.shiftModeMenuItem.setSelected(false);
                this.reverseModeMenuItem.setSelected(false);
            } else {
                this.selectModeMenuItem.setSelected(!this.shiftModeMenuItem.isSelected() && !this.reverseModeMenuItem.isSelected());
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
        shiftModeMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.O));
        selectModeMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.S));
        copySelectionMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN));
        pasteSelectionMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN));
        statsMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.L, KeyCombination.ALT_DOWN));
        resetMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.BACK_SPACE));

        aboutMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.A));
    }

    private void initStatsFileWriter(){
        final File statisticsFile = new File("stats.txt");
        try {
            if(statisticsFile.createNewFile()){
                this.statisticsWriter = new FileWriter(statisticsFile);
            } else {
                Files.delete(statisticsFile.toPath());
                if(statisticsFile.createNewFile()) {
                    this.statisticsWriter = new FileWriter(statisticsFile);
                } else {
                    LOGGER.warn(Message.get("log.problemCreatingFile", "stats.txt"));
                }
            }
        } catch (IOException e){
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void initTextWithLocaleLanguage(){
        fileMenu.textProperty().bind(Message.createStringBinding("label.file"));
        saveMenuItem.textProperty().bind(Message.createStringBinding("label.save"));
        loadMenuItem.textProperty().bind(Message.createStringBinding("label.load"));
        settingsMenuItem.textProperty().bind(Message.createStringBinding("window.settings.title"));
        exitMenuButton.textProperty().bind(Message.createStringBinding("label.exit"));

        editMenu.textProperty().bind(Message.createStringBinding("label.edit"));
        multiplePropagationMenuItem.textProperty().bind(Message.createStringBinding("label.startPropagation"));
        singlePropagationMenuItem.textProperty().bind(Message.createStringBinding("label.performPropagation"));
        shiftModeMenuItem.textProperty().bind(Message.createStringBinding("label.shiftMode"));
        reverseModeMenuItem.textProperty().bind(Message.createStringBinding("label.reverseMode"));
        selectModeMenuItem.textProperty().bind(Message.createStringBinding("label.selectMode"));
        copySelectionMenuItem.textProperty().bind(Message.createStringBinding("label.copySelection"));
        pasteSelectionMenuItem.textProperty().bind(Message.createStringBinding("label.pasteSelection"));
        statsMenuItem.textProperty().bind(Message.createStringBinding("window.stats.title"));
        resetMenuItem.textProperty().bind(Message.createStringBinding("label.reset"));

        helpMenu.textProperty().bind(Message.createStringBinding("label.help"));
        aboutMenuItem.textProperty().bind(Message.createStringBinding("label.about"));

        propagationLabel.textProperty().bind(Message.createStringBinding("label.propagation"));
        onPropagationLabel.textProperty().bind(Message.createStringBinding("off"));
        generationLabel.textProperty().bind(Message.createStringBinding("label.generation"));
        zoomLabel.textProperty().bind(Message.createStringBinding("label.zoom"));
        refreshLabel.textProperty().bind(Message.createStringBinding("label.refresh"));
    }

    private void doOnClose(){
        if(settingsStage != null) settingsStage.close();
        if(statisticsStage != null) statisticsStage.close();
        if(onPropagation.get()) stopPropagation();

        LOGGER.info(Message.get("log.gameOver"));
        try {
            pool.shutdown();
            statisticsWriter.close();
        } catch (IOException | NullPointerException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void startPropagation(){
        if(onPropagation.get()) return;
        onPropagation.set(true);

        pool.submit(new PropagationHandler());

        Platform.runLater(() -> onPropagationLabel.textProperty().bind(Message.createStringBinding("on")));
        LOGGER.info(Message.get("log.propagationOn"));
    }

    public void stopPropagation(){
        if(!onPropagation.get()) return;
        onPropagation.set(false);

        Platform.runLater(() -> onPropagationLabel.textProperty().bind(Message.createStringBinding("off")));
        LOGGER.info(Message.get("log.propagationOff"));
    }

    public void propagate(){
        List<Cell> activeCellsCopy = new ArrayList<>(this.getActiveCells());
        final Set<Cell> toAlive = new HashSet<>();
        final Set<Cell> toDead = new HashSet<>();
        boolean gameUpdated = false;
        for(Cell cell : activeCellsCopy){
            int nbAliveAroundCells = 0;
            final Set<Cell> aroundCells = cell.getAroundCells();
            if(aroundCells == null) continue;

            for(Cell c : cell.getAroundCells()) {
                if (c.isAlive()) ++nbAliveAroundCells;
            }

            if(cell.isAlive()){
                if(!GameProps.get().getStayAlive().contains(String.valueOf(nbAliveAroundCells))){
                    toDead.add(cell);
                    addActiveCell(cell);
                    gameUpdated = true;
                }
            } else {
                if(GameProps.get().getComesToLife().contains(String.valueOf(nbAliveAroundCells))){
                    toAlive.add(cell);
                    addActiveCell(cell);
                    gameUpdated = true;
                }
            }
        }

        if(!gameUpdated) {
            stopPropagation();
            return;
        }

        toAlive.forEach(Cell::makeAlive);
        toDead.forEach(Cell::makeDead);
        generationNumberLabel.setText(String.valueOf(++propagationNumber));

        colorSelectedCells();
        try {
            statisticsWriter.append(String.valueOf(propagationNumber)).append(";");
            statisticsWriter.append(String.valueOf(toAlive.size())).append("\n");
            statisticsWriter.flush();
        } catch (IOException | NullPointerException e) {
            //IOException peut être soulevée si on ferme le jeu pendant la propagation
            LOGGER.error(e.getMessage(), e);
        }
    }

    public Set<Cell> getActiveCells(){
        final Set<Cell> tmp = new HashSet<>();
        for(Cell cell : this.activeCells){
            if(cell.isAlive()) {
                tmp.add(cell);
                if(cell.getAroundCells() == null) continue;
                tmp.addAll(cell.getAroundCells());
            }
        }
        this.activeCells = tmp;
        return this.activeCells;
    }

    private void addActiveCell(Cell cell){
        activeCells.add(cell);

        final Set<Cell> around = this.grid.getAroundCells(cell);
        if(around == null) return;
        activeCells.addAll(around);
    }

    private void initGrid(){
        this.grid = new Grid(GameProps.get().getNbColumns(), GameProps.get().getNbRows());
        this.gameAnchorPane.getChildren().add(makeGridWithEventFilters(this.grid));

        initCells();
    }

    private Node makeGridWithEventFilters(final Grid grid) {
        final DragContext dragContext = new DragContext();
        final Group wrapGroup = new Group(grid);

        wrapGroup.addEventFilter(MouseEvent.MOUSE_PRESSED, mouseEvent -> {
            resetCellsColor(this.selectedCells);
            this.selectedCells = new ArrayList<>();

            if (this.mode.equals(GameMode.SHIFT_MODE)) {
                // remember initial mouse cursor coordinates
                // and node position
                dragContext.setMouseAnchorX(mouseEvent.getX());
                dragContext.setMouseAnchorY(mouseEvent.getY());
                dragContext.setInitialTranslateX(grid.getTranslateX());
                dragContext.setInitialTranslateY(grid.getTranslateY());
            } else if (this.mode.equals(GameMode.REVERSE_MODE)){
                if(!(mouseEvent.getTarget() instanceof Cell cell)) return;

                cell.reverseState();
                addActiveCell(cell);
            } else if(this.mode.equals(GameMode.SELECT_MODE)) {
                if(!(mouseEvent.getTarget() instanceof Cell cell)) return;

                this.selectContext.setXCellStart(cell.getPositionX());
                this.selectContext.setYCellStart(cell.getPositionY());
            }

            this.copySelectionMenuItem.setDisable(true);
        });

        wrapGroup.addEventFilter(MouseEvent.MOUSE_DRAGGED, mouseEvent -> {
            if (GameMode.SHIFT_MODE.equals(this.mode)) {
                // shift node from its initial position by delta
                // calculated from mouse cursor movement
                grid.setTranslateX(dragContext.getInitialTranslateX() + mouseEvent.getX() - dragContext.getMouseAnchorX());
                grid.setTranslateY(dragContext.getInitialTranslateY() + mouseEvent.getY() - dragContext.getMouseAnchorY());
            } else if(GameMode.REVERSE_MODE.equals(this.mode)) {
                if(!(mouseEvent.getPickResult().getIntersectedNode() instanceof Cell cell)) return;
                if(cell.equals(lastClickedCell)) return;

                lastClickedCell = cell;
                cell.reverseState();
                addActiveCell(cell);
            } else if(GameMode.SELECT_MODE.equals(this.mode)) {
                if(!(mouseEvent.getPickResult().getIntersectedNode() instanceof Cell cell)) return;
                this.selectContext.setXCellEnd(cell.getPositionX());
                this.selectContext.setYCellEnd(cell.getPositionY());

                resetCellsColor(this.selectedCells);
                this.selectedCells = new ArrayList<>();

                final int xMin = Math.min(selectContext.getXCellStart(), selectContext.getXCellEnd());
                final int xMax = Math.max(selectContext.getXCellStart(), selectContext.getXCellEnd());
                final int yMin = Math.min(selectContext.getYCellStart(), selectContext.getYCellEnd());
                final int yMax = Math.max(selectContext.getYCellStart(), selectContext.getYCellEnd());
                for(int i = xMin; i <= xMax; ++i) {
                    for(int j = yMin; j <= yMax; ++j) {
                        final Cell c = this.grid.getCellAtIndex(i, j);
                        if(c == null) continue;
                        this.selectedCells.add(c);
                    }
                }

                colorSelectedCells();
                this.copySelectionMenuItem.setDisable(false);
            }
        });

        return wrapGroup;
    }

    private void colorSelectedCells(){
        for(Cell c : selectedCells){
            final Color color = Color.web(c.isAlive() ? c.getAliveColor() : c.getDeadColor());
            Color newColor;

            if(color.getBlue() + 0.3 > 1) newColor = Color.color(Math.max(color.getRed() - 0.3, 0.0),
                    Math.max(color.getRed() - 0.3, 0.0),
                    color.getBlue());
            else newColor = Color.color(color.getRed(), color.getGreen(), color.getBlue() + 0.3);

            c.setFill(newColor);
        }
    }

    private void initCells(){
        final GameProps p = GameProps.get();
        //TODO: vérifier que le SVGPath est valide
        for(int i = 0; i < p.getNbColumns(); ++i){
            for(int j = 0; j < p.getNbRows(); ++j) {
                Cell cell = new Cell(p.getShapePath(), p.getCellWidth(), p.getCellHeight(), i, j);
                cell.setAliveColor(p.getCellAliveColor());
                cell.setDeadColor(p.getCellDeadColor());
                cell.makeDead();

                grid.addCell(cell);
            }
        }

        for(Cell c : grid.getCells()){
            c.setAroundCells(this.grid.getAroundCells(c));
        }
    }

    private void resetCellsColor(ArrayList<Cell> cells){
        for(Cell c : cells) {
            c.setFill(Color.web(c.isAlive() ? c.getAliveColor() : c.getDeadColor()));
        }
    }

    public void updateAliveColorCell(String newColor){
        GameProps.get().setCellAliveColor(newColor);
        for(Cell c : grid.getCells()){
            c.setAliveColor(newColor);
        }
    }

    public void updateDeadColorCell(String newColor){
        GameProps.get().setCellDeadColor(newColor);
        for(Cell c : grid.getCells()){
            c.setDeadColor(newColor);
        }
    }


    @FXML
    public void saveState(){
        final FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt"));

        final File selectedFile = fileChooser.showSaveDialog(this.gameAnchorPane.getScene().getWindow());
        if(selectedFile == null) return;

        try(ObjectOutputStream objectOut = new ObjectOutputStream(new FileOutputStream(selectedFile))) {
            for (Cell cell : getActiveCells()) {
                objectOut.writeObject(cell);
            }
            LOGGER.info(Message.get("log.gameSavedSuccess"));
        } catch (Exception e) {
            LOGGER.error(Message.get("log.gameNotSaved"), e);
        }
    }

    @FXML
    public void loadSave(){ //TODO: passer a un moyen de stockage type JSON, XML...
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("text", ".txt"));

        final File selectedFile = fileChooser.showOpenDialog(this.gameAnchorPane.getScene().getWindow());
        if(selectedFile == null) return;

        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(selectedFile))) {
            Cell tmp;
            while ((tmp = (Cell) ois.readObject()) != null){
                final Cell cell = grid.getCellAtIndex(tmp.getPositionX(), tmp.getPositionY());
                if(cell == null) continue; //si la taille de la grille a changée par exemple

                if(tmp.isAlive()) cell.makeAlive();
                else cell.makeDead();

                addActiveCell(cell);
            }

        } catch (IOException ioe) {
            LOGGER.error(Message.get("log.gameNotLoaded"), ioe);
        } catch (ClassNotFoundException cnfe) {
            LOGGER.info(Message.get("log.gameSuccessLoaded"));
        }
    }

    @FXML
    public void showSettings(){
        if (this.settingsStage != null && this.settingsStage.isShowing()) return;

        try {
            final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/settings_view.fxml"));
            final VBox root = fxmlLoader.load();

            this.settingsStage = new Stage();
            this.settingsStage.titleProperty().bind(Message.createStringBinding("window.settings.title"));
            this.settingsStage.setResizable(false);
            this.settingsStage.initOwner(this.gameAnchorPane.getScene().getWindow());
            this.settingsStage.setScene(new Scene(root));
            this.settingsStage.setOnCloseRequest((event -> this.settingsStage = null));

            final SettingsController controller = fxmlLoader.getController();
            controller.init(this);

            this.settingsStage.show();
        } catch (IOException e){
            LOGGER.error(e.getMessage(), e);
        }
    }

    @FXML
    public void exit(){
        doOnClose();
        ((Stage) this.gameAnchorPane.getScene().getWindow()).close();
        LOGGER.info(Message.get("log.endSession"));
    }

    @FXML
    public void clickMultiplePropagationMenuItem(){
        if (onPropagation.get()){
            stopPropagation();
            multiplePropagationMenuItem.textProperty().bind(Message.createStringBinding("label.startPropagation"));
        }
        else {
            startPropagation();
            multiplePropagationMenuItem.textProperty().bind(Message.createStringBinding("label.stopPropagation"));
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

        for(Cell cell : getActiveCells()) cell.makeDead();

        initStatsFileWriter();
        resetCellsColor(this.selectedCells);
        this.activeCells = new HashSet<>();
        this.selectedCells = new ArrayList<>();

        Platform.runLater(() -> {
            generationNumberLabel.setText(String.valueOf(propagationNumber));
            onPropagationLabel.textProperty().bind(Message.createStringBinding("off"));
        });
        LOGGER.info(Message.get("log.resetedGrid"));
    }

    @FXML
    public void copySelection(){
        this.copiedSelection = new ArrayList<>();
        final GameProps p = GameProps.get();

        for(Cell c : this.selectedCells){
            final Cell cell = new Cell(p.getShapePath(), p.getCellWidth(), p.getCellHeight(), c.getPositionX(), c.getPositionY());

            if(c.isAlive()) cell.makeAlive();
            else cell.makeDead();

            this.copiedSelection.add(cell);
        }

        this.pasteSelectionMenuItem.setDisable(false);
        LOGGER.info("Parcelle copié!");
    }

    @FXML
    public void pasteSelection(){
        final int xDelta = this.copiedSelection.get(0).getPositionX() - this.selectContext.getXCellStart();
        final int yDelta = this.copiedSelection.get(0).getPositionY() - this.selectContext.getYCellStart();

        for(Cell c : this.copiedSelection){
            final Cell cell = grid.getCellAtIndex(c.getPositionX() - xDelta, c.getPositionY() - yDelta);
            if(cell == null) continue;

            if(c.isAlive()) cell.makeAlive();
            else cell.makeDead();
            this.activeCells.add(cell);
        }

        resetCellsColor(this.selectedCells);
        this.selectedCells = new ArrayList<>();
    }


    @FXML
    public void showStats(){
        if(this.statisticsStage != null) return;

        try {
            final Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
            double width = primaryScreenBounds.getWidth();
            double height = primaryScreenBounds.getHeight() - 25;

            final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/statistics_view.fxml"));
            final AnchorPane root = fxmlLoader.load();
            root.setPrefSize(width, height);

            this.statisticsStage = new Stage();
            this.statisticsStage.titleProperty().bind(Message.createStringBinding("window.stats.title"));
            this.statisticsStage.setResizable(false);
            this.statisticsStage.initModality(Modality.WINDOW_MODAL);
            this.statisticsStage.initOwner(this.gameAnchorPane.getScene().getWindow());
            this.statisticsStage.setScene(new Scene(root));
            this.statisticsStage.setOnCloseRequest((event -> this.statisticsStage = null));

            this.stopPropagation();

            final StatisticsController controller = fxmlLoader.getController();
            controller.init();

            this.statisticsStage.show();
        } catch (IOException e){
            LOGGER.error(e.getMessage(), e);
        }
    }

    @FXML //TODO: optimiser
    public void makeZoom(){
        LOGGER.info(Message.get("log.zoomProcessing"));
        final List<Cell> cells = grid.getCells();

        Platform.runLater(() -> {
            double start = System.currentTimeMillis();
            if(cells.isEmpty()) return;

            for(Cell cell : cells){
                cell.setShapeWidth(GameProps.get().getCellWidth() * (zoomSlider.getValue() / 100));
                cell.setShapeHeight(GameProps.get().getCellHeight() * (zoomSlider.getValue() / 100));
            }

            double end = System.currentTimeMillis();
            LOGGER.info(Message.get("log.zoomApplied", end - start));
        });
    }



    private class PropagationHandler implements Runnable {
        @Override
        public void run() {
            while (onPropagation.get()){
                Platform.runLater(GameOfLifeController.this::propagate);

                try {
                    Thread.sleep(GameProps.get().getRefreshTimeMs());
                } catch (InterruptedException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }

    private enum GameMode {
        SHIFT_MODE,
        REVERSE_MODE,
        SELECT_MODE
    }
}