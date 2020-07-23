package fr.rom.gameoflife.utils;


import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class Properties {
    private double cellWidth = 10;
    private double cellHeight = 10;
    private String cellAliveColor = "black";
    private String cellDeadColor = "white";

    private String shapeString;

    private int gridNbColumns = 200;
    private int gridNbRows = 200;

    private final AtomicLong refreshTimeMs;
    private int nbSimultaneousThreads;

    private final Set<Integer> comeAliveSet;
    private final Set<Integer> stayAliveSet;

    public Properties(){
        refreshTimeMs = new AtomicLong(100);
        nbSimultaneousThreads = 2;
        comeAliveSet = new HashSet<>(Collections.singletonList(3));
        stayAliveSet = new HashSet<>(Arrays.asList(2, 3));
    }

    public double getCellWidth() {
        return cellWidth;
    }

    public void setCellWidth(double cellWidth) {
        this.cellWidth = cellWidth;
    }

    public double getCellHeight() {
        return cellHeight;
    }

    public void setCellHeight(double cellHeight) {
        this.cellHeight = cellHeight;
    }

    public String getCellAliveColor() {
        return cellAliveColor;
    }

    public void setCellAliveColor(String cellAliveColor) {
        this.cellAliveColor = cellAliveColor;
    }

    public String getCellDeadColor() {
        return cellDeadColor;
    }

    public void setCellDeadColor(String cellDeadColor) {
        this.cellDeadColor = cellDeadColor;
    }

    public String getShapeString() {
        return shapeString;
    }

    public void setShapeString(String shapeString) {
        this.shapeString = shapeString;
    }

    public int getGridNbColumns() {
        return gridNbColumns;
    }

    public void setGridNbColumns(int gridNbColumns) {
        this.gridNbColumns = gridNbColumns;
    }

    public int getGridNbRows() {
        return gridNbRows;
    }

    public void setGridNbRows(int gridNbRows) {
        this.gridNbRows = gridNbRows;
    }

    public long getRefreshTimeMs() {
        return refreshTimeMs.get();
    }

    public void setRefreshTimeMs(long refreshTimeMs) {
        this.refreshTimeMs.set(refreshTimeMs);
    }

    public int getNbSimultaneousThreads() {
        return nbSimultaneousThreads;
    }

    public void setNbSimultaneousThreads(int nbSimultaneousThreads) {
        this.nbSimultaneousThreads = nbSimultaneousThreads;
    }

    public Set<Integer> getComeAliveSet() {
        return comeAliveSet;
    }

    public void addComeAliveRule(int comeAliveNumber){
        this.comeAliveSet.add(comeAliveNumber);
    }

    public void removeComeAliveRule(int comeAliveNumber){
        this.comeAliveSet.remove(comeAliveNumber);
    }

    public Set<Integer> getStayAliveSet() {
        return stayAliveSet;
    }

    public void addStayAliveRule(int stayAliveNumber){
        this.stayAliveSet.add(stayAliveNumber);
    }

    public void removeStayAliveRule(int stayAliveNumber){
        this.stayAliveSet.remove(stayAliveNumber);
    }
}