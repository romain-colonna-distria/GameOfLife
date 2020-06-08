package fr.rom.gameoflife.controller;


import fr.rom.gameoflife.utils.PropertyValuesGetter;

import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.io.FileNotFoundException;



public class SettingsController {
    private PropertyValuesGetter propertyValuesGetter;

    @FXML
    private VBox rootVBox;
    @FXML
    private ColorPicker aliveColorPicker;
    @FXML
    private ColorPicker deadColorPicker;

    public void init(){
        try {
            propertyValuesGetter = new PropertyValuesGetter("config.properties");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        initColorPickers();
    }

    private void initColorPickers(){
        aliveColorPicker = new ColorPicker(Color.valueOf(propertyValuesGetter.getPropertyValue("CELL_ALIVE_COLOR")));
        aliveColorPicker.getStyleClass().add("button");
        aliveColorPicker.setOnAction(event -> {
        });

        deadColorPicker = new ColorPicker(Color.valueOf(propertyValuesGetter.getPropertyValue("CELL_DEAD_COLOR")));
        deadColorPicker.getStyleClass().add("button");
        deadColorPicker.setOnAction(event -> {
        });
    }
}
