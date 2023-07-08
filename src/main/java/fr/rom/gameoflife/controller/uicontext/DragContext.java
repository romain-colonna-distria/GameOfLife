package fr.rom.gameoflife.controller.uicontext;

import lombok.Getter;
import lombok.Setter;

public final class DragContext {
    @Getter @Setter
    private double mouseAnchorX;
    @Getter @Setter
    private double mouseAnchorY;
    @Getter @Setter
    private double initialTranslateX;
    @Getter @Setter
    private double initialTranslateY;
}
