package fr.rom.gameoflife.controller;


import fr.rom.gameoflife.utils.Properties;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;



public class SettingsController {
    private GameOfLifeController parent;
    private Properties properties;

    @FXML
    private ColorPicker aliveColorPicker;

    @FXML
    private ColorPicker deadColorPicker;

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

//    @FXML
//    private Button saveRulesButton;



    public void init(GameOfLifeController parent, Properties properties){
        this.parent = parent;
        this.properties = properties;

        initColorPickers();
        initCheckboxes();
    }

    private void initColorPickers(){
        aliveColorPicker.setValue(Color.valueOf(this.properties.getCellAliveColor()));
        aliveColorPicker.setOnAction((event) -> parent.updateAliveColorCell(aliveColorPicker.getValue().toString()));

        deadColorPicker.setValue(Color.valueOf(this.properties.getCellDeadColor()));
        deadColorPicker.setOnAction((event) -> parent.updateDeadColorCell(deadColorPicker.getValue().toString()));
    }

    private void initCheckboxes(){
        if(this.properties.getComeAliveSet().contains(0)) comeAlive0Checkbox.setSelected(true);
        if(this.properties.getComeAliveSet().contains(1)) comeAlive1Checkbox.setSelected(true);
        if(this.properties.getComeAliveSet().contains(2)) comeAlive2Checkbox.setSelected(true);
        if(this.properties.getComeAliveSet().contains(3)) comeAlive3Checkbox.setSelected(true);
        if(this.properties.getComeAliveSet().contains(4)) comeAlive4Checkbox.setSelected(true);
        if(this.properties.getComeAliveSet().contains(5)) comeAlive5Checkbox.setSelected(true);
        if(this.properties.getComeAliveSet().contains(6)) comeAlive6Checkbox.setSelected(true);
        if(this.properties.getComeAliveSet().contains(7)) comeAlive7Checkbox.setSelected(true);
        if(this.properties.getComeAliveSet().contains(8)) comeAlive8Checkbox.setSelected(true);

        if(this.properties.getStayAliveSet().contains(0)) stayAlive0Checkbox.setSelected(true);
        if(this.properties.getStayAliveSet().contains(1)) stayAlive1Checkbox.setSelected(true);
        if(this.properties.getStayAliveSet().contains(2)) stayAlive2Checkbox.setSelected(true);
        if(this.properties.getStayAliveSet().contains(3)) stayAlive3Checkbox.setSelected(true);
        if(this.properties.getStayAliveSet().contains(4)) stayAlive4Checkbox.setSelected(true);
        if(this.properties.getStayAliveSet().contains(5)) stayAlive5Checkbox.setSelected(true);
        if(this.properties.getStayAliveSet().contains(6)) stayAlive6Checkbox.setSelected(true);
        if(this.properties.getStayAliveSet().contains(7)) stayAlive7Checkbox.setSelected(true);
        if(this.properties.getStayAliveSet().contains(8)) stayAlive8Checkbox.setSelected(true);
    }

    @FXML
    public void saveRules(){
        if(comeAlive0Checkbox.isSelected()) this.properties.addComeAliveRule(0);
        else this.properties.removeComeAliveRule(0);
        if(comeAlive1Checkbox.isSelected()) this.properties.addComeAliveRule(1);
        else this.properties.removeComeAliveRule(1);
        if(comeAlive2Checkbox.isSelected()) this.properties.addComeAliveRule(2);
        else this.properties.removeComeAliveRule(2);
        if(comeAlive3Checkbox.isSelected()) this.properties.addComeAliveRule(3);
        else this.properties.removeComeAliveRule(3);
        if(comeAlive4Checkbox.isSelected()) this.properties.addComeAliveRule(4);
        else this.properties.removeComeAliveRule(4);
        if(comeAlive5Checkbox.isSelected()) this.properties.addComeAliveRule(5);
        else this.properties.removeComeAliveRule(5);
        if(comeAlive6Checkbox.isSelected()) this.properties.addComeAliveRule(6);
        else this.properties.removeComeAliveRule(6);
        if(comeAlive7Checkbox.isSelected()) this.properties.addComeAliveRule(7);
        else this.properties.removeComeAliveRule(7);
        if(comeAlive8Checkbox.isSelected()) this.properties.addComeAliveRule(8);
        else this.properties.removeComeAliveRule(8);

        if(stayAlive0Checkbox.isSelected()) this.properties.addStayAliveRule(0);
        else this.properties.removeStayAliveRule(0);
        if(stayAlive1Checkbox.isSelected()) this.properties.addStayAliveRule(1);
        else this.properties.removeStayAliveRule(1);
        if(stayAlive2Checkbox.isSelected()) this.properties.addStayAliveRule(2);
        else this.properties.removeStayAliveRule(2);
        if(stayAlive3Checkbox.isSelected()) this.properties.addStayAliveRule(3);
        else this.properties.removeStayAliveRule(3);
        if(stayAlive4Checkbox.isSelected()) this.properties.addStayAliveRule(4);
        else this.properties.removeStayAliveRule(4);
        if(stayAlive5Checkbox.isSelected()) this.properties.addStayAliveRule(5);
        else this.properties.removeStayAliveRule(5);
        if(stayAlive6Checkbox.isSelected()) this.properties.addStayAliveRule(6);
        else this.properties.removeStayAliveRule(6);
        if(stayAlive7Checkbox.isSelected()) this.properties.addStayAliveRule(7);
        else this.properties.removeStayAliveRule(7);
        if(stayAlive8Checkbox.isSelected()) this.properties.addStayAliveRule(8);
        else this.properties.removeStayAliveRule(8);

//        Stage stage = (Stage) saveRulesButton.getScene().getWindow();
//        stage.close();
    }
}