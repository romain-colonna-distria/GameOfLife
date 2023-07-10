package fr.rom.gameoflife.controller;


import fr.rom.gameoflife.property.GameProps;
import fr.rom.gameoflife.utils.Message;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SettingsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SettingsController.class);

    private GameOfLifeController parent;

    @FXML
    private Tab colorsTab;
    @FXML
    private Tab rulesTab;
    @FXML
    private Label aliveCellsLabel;
    @FXML
    private Label deadCellsLabel;
    @FXML
    private ColorPicker aliveColorPicker;
    @FXML
    private ColorPicker deadColorPicker;
    @FXML
    private Label comesLifeLabel;
    @FXML
    private CheckBox comeAlive0Checkbox;
    @FXML
    private CheckBox comeAlive1Checkbox;
    @FXML
    private CheckBox comeAlive2Checkbox;
    @FXML
    private CheckBox comeAlive3Checkbox;
    @FXML
    private CheckBox comeAlive4Checkbox;
    @FXML
    private CheckBox comeAlive5Checkbox;
    @FXML
    private CheckBox comeAlive6Checkbox;
    @FXML
    private CheckBox comeAlive7Checkbox;
    @FXML
    private CheckBox comeAlive8Checkbox;
    @FXML
    private Label stayAliveLabel;
    @FXML
    private CheckBox stayAlive0Checkbox;
    @FXML
    private CheckBox stayAlive1Checkbox;
    @FXML
    private CheckBox stayAlive2Checkbox;
    @FXML
    private CheckBox stayAlive3Checkbox;
    @FXML
    private CheckBox stayAlive4Checkbox;
    @FXML
    private CheckBox stayAlive5Checkbox;
    @FXML
    private CheckBox stayAlive6Checkbox;
    @FXML
    private CheckBox stayAlive7Checkbox;
    @FXML
    private CheckBox stayAlive8Checkbox;
    @FXML
    private Button applyButton;



    public void init(GameOfLifeController parent){
        this.parent = parent;

        colorsTab.textProperty().bind(Message.createStringBinding("label.colors"));
        rulesTab.textProperty().bind(Message.createStringBinding("label.rules"));
        aliveCellsLabel.textProperty().bind(Message.createStringBinding("label.aliveCells"));
        deadCellsLabel.textProperty().bind(Message.createStringBinding("label.deadCells"));
        comesLifeLabel.textProperty().bind(Message.createStringBinding("label.comesToLife"));
        stayAliveLabel.textProperty().bind(Message.createStringBinding("label.stayAlive"));
        applyButton.textProperty().bind(Message.createStringBinding("label.apply"));

        initColorPickers();
        initCheckboxes();
    }

    private void initColorPickers(){
        aliveColorPicker.setValue(Color.valueOf(GameProps.get().getCellAliveColor()));
        aliveColorPicker.setOnAction(e -> parent.updateAliveColorCell(aliveColorPicker.getValue().toString()));

        deadColorPicker.setValue(Color.valueOf(GameProps.get().getCellDeadColor()));
        deadColorPicker.setOnAction(e -> parent.updateDeadColorCell(deadColorPicker.getValue().toString()));
    }

    private void initCheckboxes(){
        final GameProps p = GameProps.get();
        if(p.getComesToLife().contains("0")) comeAlive0Checkbox.setSelected(true);
        if(p.getComesToLife().contains("1")) comeAlive1Checkbox.setSelected(true);
        if(p.getComesToLife().contains("2")) comeAlive2Checkbox.setSelected(true);
        if(p.getComesToLife().contains("3")) comeAlive3Checkbox.setSelected(true);
        if(p.getComesToLife().contains("4")) comeAlive4Checkbox.setSelected(true);
        if(p.getComesToLife().contains("5")) comeAlive5Checkbox.setSelected(true);
        if(p.getComesToLife().contains("6")) comeAlive6Checkbox.setSelected(true);
        if(p.getComesToLife().contains("7")) comeAlive7Checkbox.setSelected(true);
        if(p.getComesToLife().contains("8")) comeAlive8Checkbox.setSelected(true);

        if(p.getStayAlive().contains("0")) stayAlive0Checkbox.setSelected(true);
        if(p.getStayAlive().contains("1")) stayAlive1Checkbox.setSelected(true);
        if(p.getStayAlive().contains("2")) stayAlive2Checkbox.setSelected(true);
        if(p.getStayAlive().contains("3")) stayAlive3Checkbox.setSelected(true);
        if(p.getStayAlive().contains("4")) stayAlive4Checkbox.setSelected(true);
        if(p.getStayAlive().contains("5")) stayAlive5Checkbox.setSelected(true);
        if(p.getStayAlive().contains("6")) stayAlive6Checkbox.setSelected(true);
        if(p.getStayAlive().contains("7")) stayAlive7Checkbox.setSelected(true);
        if(p.getStayAlive().contains("8")) stayAlive8Checkbox.setSelected(true);
    }


    @FXML
    public void saveRules(){
        final GameProps p = GameProps.get();
        if(comeAlive0Checkbox.isSelected()) p.addComesToLife("0");
        else p.removeComesToLife("0");
        if(comeAlive1Checkbox.isSelected()) p.addComesToLife("1");
        else p.removeComesToLife("1");
        if(comeAlive2Checkbox.isSelected()) p.addComesToLife("2");
        else p.removeComesToLife("2");
        if(comeAlive3Checkbox.isSelected()) p.addComesToLife("3");
        else p.removeComesToLife("3");
        if(comeAlive4Checkbox.isSelected()) p.addComesToLife("4");
        else p.removeComesToLife("4");
        if(comeAlive5Checkbox.isSelected()) p.addComesToLife("5");
        else p.removeComesToLife("5");
        if(comeAlive6Checkbox.isSelected()) p.addComesToLife("6");
        else p.removeComesToLife("6");
        if(comeAlive7Checkbox.isSelected()) p.addComesToLife("7");
        else p.removeComesToLife("7");
        if(comeAlive8Checkbox.isSelected()) p.addComesToLife("8");
        else p.removeComesToLife("8");

        if(stayAlive0Checkbox.isSelected()) p.addStayAlive("0");
        else p.removeStayAlive("0");
        if(stayAlive1Checkbox.isSelected()) p.addStayAlive("1");
        else p.removeStayAlive("1");
        if(stayAlive2Checkbox.isSelected()) p.addStayAlive("2");
        else p.removeStayAlive("2");
        if(stayAlive3Checkbox.isSelected()) p.addStayAlive("3");
        else p.removeStayAlive("3");
        if(stayAlive4Checkbox.isSelected()) p.addStayAlive("4");
        else p.removeStayAlive("4");
        if(stayAlive5Checkbox.isSelected()) p.addStayAlive("5");
        else p.removeStayAlive("5");
        if(stayAlive6Checkbox.isSelected()) p.addStayAlive("6");
        else p.removeStayAlive("6");
        if(stayAlive7Checkbox.isSelected()) p.addStayAlive("7");
        else p.removeStayAlive("7");
        if(stayAlive8Checkbox.isSelected()) p.addStayAlive("8");
        else p.removeStayAlive("8");

        LOGGER.info(Message.get("log.rulesUpdated", p.getComesToLife(), p.getStayAlive()));
    }
}
