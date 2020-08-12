package fr.rom.gameoflife.objects.cells;

public class RectangleCell extends AbstractCell {
    public RectangleCell(double width, double height, int positionX, int positionY) {
        super("M 0 0 L 10 0 L 10 10 L 0 10 Z", width, height, positionX, positionY);
    }
}
