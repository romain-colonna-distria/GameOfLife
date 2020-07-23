package fr.rom.gameoflife.objects;


import java.util.Set;

public interface ICell { //TODO: faire etendre shape et construire la forme dynamiquement (liteto
    void makeAlive();
    void makeDead();
    void reverseState();

    int getPositionX();
    int getPositionY();
    boolean isAlive();

    void setAliveColor(String aliveColor);
    void setDeadColor(String deadColor);

    Set<ICell> getAroundCells();
    void setAroundCells(Set<ICell> aroundCells);

    double getShapeWidth();
    double getShapeHeight();

    void setShapeWidth(double width);
    void setShapeHeight(double height);
}
