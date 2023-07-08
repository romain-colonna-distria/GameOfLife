package fr.rom.gameoflife.controller.uicontext;

import lombok.Getter;
import lombok.Setter;

public class SelectionContext {
    @Getter @Setter
    private int xCellStart;
    @Getter @Setter
    private int yCellStart;
    @Getter @Setter
    private int xCellEnd;
    @Getter @Setter
    private int yCellEnd;
}
