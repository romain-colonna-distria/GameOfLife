package fr.rom.gameoflife.objects.cells;

public class TriangleCell extends AbstractCell {
    public TriangleCell(double width, double height, int positionX, int positionY) {
        super("M 0 0 h 10 L 5 10 Z", width, height, positionX, positionY);
    }
}
