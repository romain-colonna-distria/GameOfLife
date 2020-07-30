package fr.rom.gameoflife.objects;


import java.util.Set;

public interface ICell { //TODO: faire etendre shape ou node et construire la forme dynamiquement (lineto etc)
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

    void setShapeWidth(double width);
    void setShapeHeight(double height);
}
