package fr.rom.gameoflife.controller;


import fr.rom.gameoflife.objects.CellEllipse;
import fr.rom.gameoflife.objects.CellRectangle;
import fr.rom.gameoflife.objects.Grid;
import fr.rom.gameoflife.objects.ICell;
import fr.rom.gameoflife.utils.Properties;

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
    private Properties properties;

    private Set<ICell> activeCells;
    private ICell lastClickedCell;
    private double xClick, yClick;

    private int propagationNumber = 0;
    private AtomicBoolean onPropagation;

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
    private final EventHandler<MouseEvent> onMousePressed =  event -> {
        this.xClick = event.getX();
        this.yClick = event.getY();
    };

    //TODO: mieux gérer le déplacement
    /**
     * Event handler détectant le déplacement de la sourie (sourie cliquée) sur la grille.
     * Permet de déplacer la grille.
     */
    private final EventHandler<MouseEvent> onMouseDragForMovePlan =  event -> {
        if(event.getSource() instanceof Grid){
            ((Grid) event.getSource()).setTranslateX(event.getX() - this.xClick + ((Grid) event.getSource()).getTranslateX());
            ((Grid) event.getSource()).setTranslateY(event.getY() - this.yClick + ((Grid) event.getSource()).getTranslateY());
            this.xClick = event.getX();
            this.yClick = event.getY();
        }
    };

    /**
     * Event handler détectant la pression d'une touche du clavier. Permet
     * d'effectier une propagation (espace) ou de lancer la propagatio autamotique (entré).
     */
    private final EventHandler<KeyEvent> onKeyPressed = event -> {
        if(event.getCode().equals(KeyCode.ENTER)) {
            if (isOnPropagation()) stopPropagation();
            else startPropagation();
        } else if(event.getCode().equals(KeyCode.SPACE)){
            propagate();
        }
    };

    /**
     * Event handler détectant le déplacement de la sourie (sourie cliquée) sur la grille.
     * Permet de modifier l'etat des celules survolées par la sourie.
     */
    private final EventHandler<MouseEvent> onMouseDragForReverse = event -> {
        if(!(event.getPickResult().getIntersectedNode() instanceof ICell)) return;
        ICell cell = (ICell) event.getPickResult().getIntersectedNode();

        if(cell.equals(lastClickedCell)) return;
        lastClickedCell = cell;

        cell.reverseState();
        addNewActiveCell(cell);
    };

    /**
     * Event handler détectant une pression de la sourie sur la grille. Permet
     * de modifer l'etat d'une cellule
     */
    private final EventHandler<MouseEvent> onClickCell = event -> {
        if(!(event.getTarget() instanceof ICell)) return;
        ICell cell = (ICell) event.getTarget();

        cell.reverseState();
        addNewActiveCell(cell);
    };



    public void init(Stage stage, Properties properties) {
        this.properties = properties;
        this.onPropagation = new AtomicBoolean(false);
        this.pool = Executors.newFixedThreadPool(properties.getNbSimultaneousThreads());
        this.activeCells = new HashSet<>();

        initGrid(properties.getGridNbColumns(), properties.getGridNbRows());

        stage.getScene().setOnKeyPressed(this.onKeyPressed);
        stage.setOnCloseRequest((event -> doOnClose()));

        statisticsFile = new File("stats.txt");
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
                if(!this.properties.getStayAliveSet().contains(nbAliveAroundCells)){
                    toDead.add(cell);
                    addNewActiveCell(cell);
                    gameUpdated = true;
                }
            } else {
                if(this.properties.getComeAliveSet().contains(nbAliveAroundCells)){
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
        for(int i = 0; i < nbCols; ++i){
            for(int j = 0; j < nbRws; ++j) {
                if(this.properties.getShapeString().equals("rectangle"))
                    cell = new CellRectangle(this.properties.getCellWidth(), this.properties.getCellHeight(), i, j);
                else if(this.properties.getShapeString().equals("ovale"))
                    cell = new CellEllipse(this.properties.getCellWidth(), this.properties.getCellHeight(), i, j);
                else return;

                cell.setAliveColor(this.properties.getCellAliveColor());
                cell.setDeadColor(this.properties.getCellDeadColor());
                cell.makeDead();
                grid.addCell(cell);
            }
        }

        for(ICell c : grid.getCells()){
            c.setAroundCells(this.grid.getAroundCells(c));
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
    public void resetGrid(){
        if(onPropagation.get()) stopPropagation();
        propagationNumber = 0;

        for(ICell cell : getActiveCells()) cell.makeDead();
        this.activeCells = new HashSet<>();

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
        double newCellsWidth = this.properties.getCellWidth() * (zoomSlider.getValue() / 100);
        double newCellsHeight = this.properties.getCellHeight() * (zoomSlider.getValue() / 100);
        int nbThread = this.properties.getNbSimultaneousThreads();
        int from = 0;

        for(int i = 0; i < nbThread; ++i) {
            if(from > cells.size()) break;
            pool.submit(new Zoom_Handler(cells.subList(from, from + cells.size() / nbThread), newCellsWidth, newCellsHeight));
            from += cells.size() / nbThread;
        }
    }

    @FXML
    public void updateZoomLabel(){
        Platform.runLater(() -> zoomLabel.setText(String.valueOf(zoomSlider.getValue())));
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
                Platform.runLater(GameOfLifeController.this::propagate);

                try {
                    Thread.sleep(properties.getRefreshTimeMs());
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
}