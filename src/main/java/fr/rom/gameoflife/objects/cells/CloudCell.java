package fr.rom.gameoflife.objects.cells;


public class CloudCell extends AbstractCell {
    public CloudCell(double width, double height, int positionX, int positionY){
        super("M 72 19 C 75 15 77 13 80 11 C 83 8 90 3 94 3 C 109 -2 131 3 141 15 C 146 20 149 25 151 31 C 152 33 153 36 153 38 C 153 39 153 40 154 41 C 154 41 156 41 157 42 C 159 42 160 43 162 44 C 168 48 173 55 174 63 C 175 76 169 86 158 91 C 155 92 152 93 150 93 C 150 93 27 93 27 93 C 24 93 22 92 20 92 C 9 88 2 78 1 67 C 1 55 9 45 20 41 C 24 40 27 40 31 40 C 31 38 33 34 34 32 C 38 26 43 22 50 21 C 53 20 55 20 58 20 C 62 20 66 21 69 23 C 70 22 71 20 72 19 Z",
                width, height, positionX, positionY);
    }
}
