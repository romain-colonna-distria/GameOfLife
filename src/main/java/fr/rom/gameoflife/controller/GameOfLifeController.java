package fr.rom.gameoflife.controller;


import fr.rom.gameoflife.objects.Grid;
import fr.rom.gameoflife.objects.Cell;
import fr.rom.gameoflife.utils.Language;
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
import javafx.scene.paint.Color;
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
    private GameMode mode;

    private Set<Cell> activeCells;
    private Cell lastClickedCell;

    private SelectContext selectContext;
    private ArrayList<Cell> selectedCells;
    private ArrayList<Cell> copiedSelection;

    private int propagationNumber;
    private AtomicBoolean onPropagation;

    private File statisticsFile;
    private FileWriter statisticsWriter;

    private final static Logger logger = Logger.getLogger(GameOfLifeController.class);

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
        logger.info(Language.get("log.initNewGame") + " ("
                + Properties.getInstance().getGridNbColumns()
                + Language.get("log.colomns") +"/"
                + Properties.getInstance().getGridNbRows()
                + Language.get("log.rows") + " )...");
        double start = System.currentTimeMillis();

        this.pool = Executors.newFixedThreadPool(2);
        this.mode = GameMode.REVERSE_MODE;
        this.activeCells = new HashSet<>();
        this.propagationNumber = 0;
        this.onPropagation = new AtomicBoolean(false);

        this.refreshSlider.setValue(Properties.getInstance().getRefreshTimeMs());
        this.refreshValueLabel.setText(String.valueOf(Properties.getInstance().getRefreshTimeMs()));
        this.reverseModeMenuItem.setSelected(true);

        this.selectContext = new SelectContext();
        this.selectedCells = new ArrayList<>();

        initGrid();
        initListeners();
        addShortcuts();
        initStatsFileWritter();
        initTextWithLocaleLanguage();

        double end =  System.currentTimeMillis();
        logger.info(Language.get("log.gameInitialized") + " (" + (end - start) + "ms)");
    }

    private void initListeners(){
        this.grid.getScene().getWindow().setOnCloseRequest((event -> {
            doOnClose();
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/init_view.fxml"));
                AnchorPane root = fxmlLoader.load();

                Stage stage = new Stage();
                stage.titleProperty().bind(Language.createStringBinding("window.init.title"));
                stage.setScene(new Scene(root));

                InitController controller = fxmlLoader.getController();
                controller.init(stage);

                stage.show();
            } catch (Exception e) {
                logger.fatal(e.getMessage());
                System.exit(1);
            }
        }));
        this.zoomSlider.valueProperty().addListener((observable, oldValue, newValue) -> zoomPercentLabel.textProperty().setValue(String.valueOf((int)zoomSlider.getValue())));
        this.refreshSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            refreshValueLabel.textProperty().setValue(String.valueOf((int) refreshSlider.getValue()));
            Properties.getInstance().setRefreshTimeMs((long) refreshSlider.getValue());
        });
        this.shiftModeMenuItem.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue) {
                this.mode = GameMode.SHIFT_MODE;
                this.reverseModeMenuItem.setSelected(false);
                this.selectModeMenuItem.setSelected(false);
            } else {
                this.shiftModeMenuItem.setSelected(!this.reverseModeMenuItem.isSelected() && !this.selectModeMenuItem.isSelected());
            }
        });
        this.reverseModeMenuItem.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue) {
                this.mode = GameMode.REVERSE_MODE;
                this.shiftModeMenuItem.setSelected(false);
                this.selectModeMenuItem.setSelected(false);
            } else {
                this.reverseModeMenuItem.setSelected(!this.shiftModeMenuItem.isSelected() && !this.selectModeMenuItem.isSelected());
            }
        });
        this.selectModeMenuItem.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue) {
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

    private void initStatsFileWritter(){
        this.statisticsFile = new File("stats.txt");
        try {
            if(this.statisticsFile.createNewFile()){
                this.statisticsWriter = new FileWriter(this.statisticsFile);
            } else {
                if(this.statisticsFile.delete()) {
                    if(this.statisticsFile.createNewFile()) {
                        this.statisticsWriter = new FileWriter(this.statisticsFile);
                    }
                    else logger.warn(Language.get("log.problemCreatingFile") + " stats.txt");
                } else {
                    logger.warn(Language.get("log.problemCreatingFile") + " stats.txt");
                }
            }
        } catch (IOException e){
            logger.error(e.getMessage());
        }
    }

    private void initTextWithLocaleLanguage(){
        fileMenu.textProperty().bind(Language.createStringBinding("label.file"));
        saveMenuItem.textProperty().bind(Language.createStringBinding("label.save"));
        loadMenuItem.textProperty().bind(Language.createStringBinding("label.load"));
        settingsMenuItem.textProperty().bind(Language.createStringBinding("window.settings.title"));
        exitMenuButton.textProperty().bind(Language.createStringBinding("label.exit"));

        editMenu.textProperty().bind(Language.createStringBinding("label.edit"));
        multiplePropagationMenuItem.textProperty().bind(Language.createStringBinding("label.startPropagation"));
        singlePropagationMenuItem.textProperty().bind(Language.createStringBinding("label.performPropagation"));
        shiftModeMenuItem.textProperty().bind(Language.createStringBinding("label.shiftMode"));
        reverseModeMenuItem.textProperty().bind(Language.createStringBinding("label.reverseMode"));
        selectModeMenuItem.textProperty().bind(Language.createStringBinding("label.selectMode"));
        copySelectionMenuItem.textProperty().bind(Language.createStringBinding("label.copySelection"));
        pasteSelectionMenuItem.textProperty().bind(Language.createStringBinding("label.pasteSelection"));
        statsMenuItem.textProperty().bind(Language.createStringBinding("window.stats.title"));
        resetMenuItem.textProperty().bind(Language.createStringBinding("label.reset"));

        helpMenu.textProperty().bind(Language.createStringBinding("label.help"));
        aboutMenuItem.textProperty().bind(Language.createStringBinding("label.about"));

        propagationLabel.textProperty().bind(Language.createStringBinding("label.propagation"));
        onPropagationLabel.textProperty().bind(Language.createStringBinding("off"));
        generationLabel.textProperty().bind(Language.createStringBinding("label.generation"));
        zoomLabel.textProperty().bind(Language.createStringBinding("label.zoom"));
        refreshLabel.textProperty().bind(Language.createStringBinding("label.refresh"));
    }

    private void doOnClose(){
        if(settingsStage != null) settingsStage.close();
        if(statisticsStage != null) statisticsStage.close();
        if(onPropagation.get()) stopPropagation();

        logger.info(Language.get("log.gameOver"));
        try {
            pool.shutdown();
            statisticsWriter.close();
        } catch (IOException e) {
            logger.error(e.getMessage());
        } catch (NullPointerException e) {
            System.out.println("???????????1");
        }
    }

    public void startPropagation(){
        if(onPropagation.get()) return;
        onPropagation.set(true);

        pool.submit(new Propagation_Handler());

        Platform.runLater(() -> onPropagationLabel.textProperty().bind(Language.createStringBinding("on")));
        logger.info(Language.get("label.propagation") + " " + Language.get("on"));
    }

    public void stopPropagation(){
        if(!onPropagation.get()) return;
        onPropagation.set(false);

        Platform.runLater(() -> onPropagationLabel.textProperty().bind(Language.createStringBinding("off")));
        logger.info(Language.get("label.propagation") + " " + Language.get("off"));
    }

    public void propagate(){
        List<Cell> activeCells = new ArrayList<>(this.getActiveCells());
        Set<Cell> toAlive = new HashSet<>();
        Set<Cell> toDead = new HashSet<>();
        boolean gameUpdated = false;
        for(Cell cell : activeCells){
            int nbAliveAroundCells = 0;
            Set<Cell> aroundCells = cell.getAroundCells();
            if(aroundCells == null) continue;

            for(Cell c : cell.getAroundCells()) {
                if (c.isAlive()) ++nbAliveAroundCells;
            }

            if(cell.isAlive()){
                if(!Properties.getInstance().getStayAliveSet().contains(nbAliveAroundCells)){
                    toDead.add(cell);
                    addActiveCell(cell);
                    gameUpdated = true;
                }
            } else {
                if(Properties.getInstance().getComeAliveSet().contains(nbAliveAroundCells)){
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

        for(Cell cell : toAlive) cell.makeAlive();
        for(Cell cell : toDead) cell.makeDead();
        generationNumberLabel.setText(String.valueOf(++propagationNumber));

        colorSelectedCells();
        try {
            statisticsWriter.append(String.valueOf(propagationNumber)).append(";");
            statisticsWriter.append(String.valueOf(toAlive.size())).append("\n");
            statisticsWriter.flush();
        } catch (IOException e) {
            //peut être déclanché si on ferme le jeu pendant la propagation
            logger.error(e.getMessage());
        } catch (NullPointerException e){
            System.out.println("???????????2");
        }
    }

    public Set<Cell> getActiveCells(){
        Set<Cell> tmp = new HashSet<>();
        for(Cell cell : this.activeCells){
            if(cell.isAlive()) {
                tmp.add(cell);
                if(cell.getAroundCells() == null) continue;
                tmp.addAll(cell.getAroundCells());
            }
        }
        this.activeCells = new HashSet<>(tmp);

        return activeCells;
    }

    private void addActiveCell(Cell cell){
        activeCells.add(cell);

        Set<Cell> around = this.grid.getAroundCells(cell);
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
            resetCellsColor(this.selectedCells);
            this.selectedCells = new ArrayList<>();

            if (this.mode.equals(GameMode.SHIFT_MODE)) {
                // remember initial mouse cursor coordinates
                // and node position
                dragContext.mouseAnchorX = mouseEvent.getX();
                dragContext.mouseAnchorY = mouseEvent.getY();
                dragContext.initialTranslateX = grid.getTranslateX();
                dragContext.initialTranslateY = grid.getTranslateY();
            } else if (this.mode.equals(GameMode.REVERSE_MODE)){
                if(!(mouseEvent.getTarget() instanceof Cell)) return;
                Cell cell = (Cell) mouseEvent.getTarget();

                cell.reverseState();
                addActiveCell(cell);
            } else if(this.mode.equals(GameMode.SELECT_MODE)) {
                if(!(mouseEvent.getTarget() instanceof Cell)) return;

                this.selectContext.xCellStart = ((Cell) mouseEvent.getTarget()).getPositionX();
                this.selectContext.yCellStart = ((Cell) mouseEvent.getTarget()).getPositionY();
            }

            this.copySelectionMenuItem.setDisable(true);
        });

        wrapGroup.addEventFilter(MouseEvent.MOUSE_DRAGGED, mouseEvent -> {
            if (this.mode.equals(GameMode.SHIFT_MODE)) {
                // shift node from its initial position by delta
                // calculated from mouse cursor movement
                grid.setTranslateX(dragContext.initialTranslateX + mouseEvent.getX() - dragContext.mouseAnchorX);
                grid.setTranslateY(dragContext.initialTranslateY + mouseEvent.getY() - dragContext.mouseAnchorY);
            } else if(this.mode.equals(GameMode.REVERSE_MODE)) {
                if(!(mouseEvent.getPickResult().getIntersectedNode() instanceof Cell)) return;
                Cell cell = (Cell) mouseEvent.getPickResult().getIntersectedNode();

                if(cell.equals(lastClickedCell)) return;
                lastClickedCell = cell;

                cell.reverseState();
                addActiveCell(cell);
            } else if(this.mode.equals(GameMode.SELECT_MODE)) {
                if(!(mouseEvent.getPickResult().getIntersectedNode() instanceof Cell)) return;
                this.selectContext.xCellEnd = ((Cell) mouseEvent.getPickResult().getIntersectedNode()).getPositionX();
                this.selectContext.yCellEnd = ((Cell) mouseEvent.getPickResult().getIntersectedNode()).getPositionY();

                resetCellsColor(this.selectedCells);
                this.selectedCells = new ArrayList<>();

                int xMin = Math.min(selectContext.xCellStart, selectContext.xCellEnd);
                int xMax = Math.max(selectContext.xCellStart, selectContext.xCellEnd);
                int yMin = Math.min(selectContext.yCellStart, selectContext.yCellEnd);
                int yMax = Math.max(selectContext.yCellStart, selectContext.yCellEnd);
                for(int i = xMin; i <= xMax; ++i) {
                    for(int j = yMin; j <= yMax; ++j) {
                        Cell cell = this.grid.getCellAtIndex(i, j);
                        if(cell == null) continue;
                        this.selectedCells.add(cell);
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
            if(c.isAlive()) {
                Color color = Color.web(c.getAliveColor());
                Color newColor;

                if(color.getBlue() + 0.3 > 1) newColor = Color.color(Math.max(color.getRed() - 0.3, 0.0),
                        Math.max(color.getRed() - 0.3, 0.0),
                        color.getBlue());
                else newColor = Color.color(color.getRed(), color.getGreen(), color.getBlue() + 0.3);

                c.setFill(newColor);
            } else {
                Color color = Color.web(c.getDeadColor());
                Color newColor;

                if(color.getBlue() + 0.3 > 1) newColor = Color.color(Math.max(color.getRed() - 0.3, 0.0),
                        Math.max(color.getRed() - 0.3, 0.0),
                        color.getBlue());
                else newColor = Color.color(color.getRed(), color.getGreen(), color.getBlue() + 0.3);

                c.setFill(newColor);
            }
        }
    }

    private void initCells(){
        Cell cell;
        Properties p = Properties.getInstance();
        //TODO: vérifier que le SVGPath est valide
        for(int i = 0; i < p.getGridNbColumns(); ++i){
            for(int j = 0; j < p.getGridNbRows(); ++j) {
                cell = new Cell(p.getShapeString(), p.getCellWidth(), p.getCellHeight(), i, j);
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
            if(c.isAlive()) c.setFill(Color.web(c.getAliveColor()));
            else c.setFill(Color.web(c.getDeadColor()));
        }
    }

    public void updateAliveColorCell(String newColor){
        Properties.getInstance().setCellAliveColor(newColor);
        for(Cell c : grid.getCells()){
            c.setAliveColor(newColor);
        }
    }

    public void updateDeadColorCell(String newColor){
        Properties.getInstance().setCellDeadColor(newColor);
        for(Cell c : grid.getCells()){
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

            for (Cell cell : getActiveCells()) {
                objectOut.writeObject(cell);
            }
            logger.info(Language.get("log.gameSavedSuccess"));
            objectOut.close();
            fileOut.close();
        } catch (Exception ex) {
            logger.error(Language.get("log.gameNotSaved") + " : " + ex.getMessage());
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

            logger.error(Language.get("log.gameNotLoaded") + " : " + e.getMessage());
            return;
        }

        Cell tmp;
        Cell cell;
        try {
            while ((tmp = (Cell) ois.readObject()) != null){
                cell = grid.getCellAtIndex(tmp.getPositionX(), tmp.getPositionY());
                if(cell == null) continue; //si la taille de la grille a changée par exemple

                if(tmp.isAlive()) cell.makeAlive();
                else cell.makeDead();

                addActiveCell(cell);
            }
        } catch (EOFException e){
            logger.info(Language.get("log.gameSuccessLoaded"));
        } catch (IOException | ClassNotFoundException e){
            logger.error(Language.get("log.gameSaveContainError") + " : " + e.getMessage());
        }
    }

    @FXML
    public void showSettings(){
        if (this.settingsStage != null && this.settingsStage.isShowing()) return;

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/settings_view.fxml"));
            VBox root = fxmlLoader.load();

            this.settingsStage = new Stage();
            this.settingsStage.titleProperty().bind(Language.createStringBinding("window.settings.title"));
            this.settingsStage.setResizable(false);
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
        logger.info(Language.get("log.endSession"));
    }

    @FXML
    public void clickMultiplePropagationMenuItem(){
        if (onPropagation.get()){
            stopPropagation();
            multiplePropagationMenuItem.textProperty().bind(Language.createStringBinding("label.startPropagation"));
        }
        else {
            startPropagation();
            multiplePropagationMenuItem.textProperty().bind(Language.createStringBinding("label.stopPropagation"));
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

        initStatsFileWritter();
        resetCellsColor(this.selectedCells);
        this.activeCells = new HashSet<>();
        this.selectedCells = new ArrayList<>();

        Platform.runLater(() -> {
            generationNumberLabel.setText(String.valueOf(propagationNumber));
            onPropagationLabel.textProperty().bind(Language.createStringBinding("off"));
        });
        logger.info(Language.get("log.resetedGrid"));
    }

    @FXML
    public void copySelection(){
        this.copiedSelection = new ArrayList<>();
        Properties p = Properties.getInstance();

        for(Cell c : this.selectedCells){
            Cell cell = new Cell(p.getShapeString(), p.getCellWidth(), p.getCellHeight(), c.getPositionX(), c.getPositionY());

            if(c.isAlive()) cell.makeAlive();
            else cell.makeDead();

            this.copiedSelection.add(cell);
        }

        this.pasteSelectionMenuItem.setDisable(false);
        logger.info("Parcelle copié!");
    }

    @FXML
    public void pasteSelection(){
        Cell cell;
        int xDelta = this.copiedSelection.get(0).getPositionX() - this.selectContext.xCellStart;
        int yDelta = this.copiedSelection.get(0).getPositionY() - this.selectContext.yCellStart;
        for(Cell c : this.copiedSelection){
            try {
                cell = grid.getCellAtIndex(c.getPositionX() - xDelta, c.getPositionY() - yDelta);
            } catch (IndexOutOfBoundsException e) {
                continue;
            }

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
            Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
            double width = primaryScreenBounds.getWidth();
            double height = primaryScreenBounds.getHeight() - 25;

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/statistics_view.fxml"));
            AnchorPane root = fxmlLoader.load();
            root.setPrefSize(width, height);

            this.statisticsStage = new Stage();
            this.statisticsStage.titleProperty().bind(Language.createStringBinding("window.stats.title"));
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
        logger.info(Language.get("log.zoomProcessing"));
        List<Cell> cells = grid.getCells();
        Properties.getInstance().setCellWidth(Properties.getInstance().getCellWidth() * (zoomSlider.getValue() / 100));
        Properties.getInstance().setCellHeight(Properties.getInstance().getCellHeight() * (zoomSlider.getValue() / 100));
        pool.submit(new Zoom_Handler(cells));
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
        private final List<Cell> cells;


        public Zoom_Handler(List<Cell> cells){
            this.cells = cells;
        }

        @Override
        public void run() {
            Platform.runLater(() -> {
                double start = System.currentTimeMillis();
                if(this.cells.size() < 1) return;

                for(Cell cell : cells){
                    cell.setShapeWidth(Properties.getInstance().getCellWidth());
                    cell.setShapeHeight(Properties.getInstance().getCellHeight());
                }

                double end = System.currentTimeMillis();
                logger.info(Language.get("log.zoomApplied") + " : " + Thread.currentThread().getName() + ", " + (end - start) + "ms");
            });
        }
    }

    private static final class DragContext {
        public double mouseAnchorX;
        public double mouseAnchorY;
        public double initialTranslateX;
        public double initialTranslateY;
    }

    private static final class SelectContext {
        public int xCellStart;
        public int yCellStart;
        public int xCellEnd;
        public int yCellEnd;
    }

    private enum GameMode {
        SHIFT_MODE,
        REVERSE_MODE,
        SELECT_MODE
    }
}