package fr.rom;

import fr.rom.utils.Util;
import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.GridPane;

public class SettingsController {
    @FXML
    private GridPane rootGridPane;

    public void init(){
        ColorPicker alivePicker = new ColorPicker(Util.CELL_ALIVE_COLOR);
        ColorPicker deadPicker = new ColorPicker(Util.CELL_DEAD_COLOR);
        alivePicker.getStyleClass().add("button");
        deadPicker.getStyleClass().add("button");

        rootGridPane.add(alivePicker, 1, 0);
        rootGridPane.add(deadPicker, 1, 1);
    }
}
