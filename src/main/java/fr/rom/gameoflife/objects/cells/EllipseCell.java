package fr.rom.gameoflife.objects.cells;


public class EllipseCell extends AbstractCell {
    public EllipseCell(double width, double height, int positionX, int positionY){
        super("M 0 5 a 5 5 0 1 1 10 0 a 5 5 0 1 1 -10 0", width, height, positionX, positionY);
    }
}
