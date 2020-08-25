package fr.rom.gameoflife.controller;


import fr.rom.gameoflife.utils.Properties;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;
import org.apache.log4j.Logger;


public class SettingsController {
    private GameOfLifeController parent;

    private final static Logger logger = Logger.getLogger(InitController.class);


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



    public void init(GameOfLifeController parent){
        this.parent = parent;

        initColorPickers();
        initCheckboxes();
    }

    private void initColorPickers(){
        aliveColorPicker.setValue(Color.valueOf(Properties.getInstance().getCellAliveColor()));
        aliveColorPicker.setOnAction((event) -> parent.updateAliveColorCell(aliveColorPicker.getValue().toString()));

        deadColorPicker.setValue(Color.valueOf(Properties.getInstance().getCellDeadColor()));
        deadColorPicker.setOnAction((event) -> parent.updateDeadColorCell(deadColorPicker.getValue().toString()));
    }

    private void initCheckboxes(){
        Properties p = Properties.getInstance();
        if(p.getComeAliveSet().contains(0)) comeAlive0Checkbox.setSelected(true);
        if(p.getComeAliveSet().contains(1)) comeAlive1Checkbox.setSelected(true);
        if(p.getComeAliveSet().contains(2)) comeAlive2Checkbox.setSelected(true);
        if(p.getComeAliveSet().contains(3)) comeAlive3Checkbox.setSelected(true);
        if(p.getComeAliveSet().contains(4)) comeAlive4Checkbox.setSelected(true);
        if(p.getComeAliveSet().contains(5)) comeAlive5Checkbox.setSelected(true);
        if(p.getComeAliveSet().contains(6)) comeAlive6Checkbox.setSelected(true);
        if(p.getComeAliveSet().contains(7)) comeAlive7Checkbox.setSelected(true);
        if(p.getComeAliveSet().contains(8)) comeAlive8Checkbox.setSelected(true);

        if(p.getStayAliveSet().contains(0)) stayAlive0Checkbox.setSelected(true);
        if(p.getStayAliveSet().contains(1)) stayAlive1Checkbox.setSelected(true);
        if(p.getStayAliveSet().contains(2)) stayAlive2Checkbox.setSelected(true);
        if(p.getStayAliveSet().contains(3)) stayAlive3Checkbox.setSelected(true);
        if(p.getStayAliveSet().contains(4)) stayAlive4Checkbox.setSelected(true);
        if(p.getStayAliveSet().contains(5)) stayAlive5Checkbox.setSelected(true);
        if(p.getStayAliveSet().contains(6)) stayAlive6Checkbox.setSelected(true);
        if(p.getStayAliveSet().contains(7)) stayAlive7Checkbox.setSelected(true);
        if(p.getStayAliveSet().contains(8)) stayAlive8Checkbox.setSelected(true);
    }


    @FXML
    public void saveRules(){
        Properties p = Properties.getInstance();
        if(comeAlive0Checkbox.isSelected()) p.addComeAliveRule(0);
        else p.removeComeAliveRule(0);
        if(comeAlive1Checkbox.isSelected()) p.addComeAliveRule(1);
        else p.removeComeAliveRule(1);
        if(comeAlive2Checkbox.isSelected()) p.addComeAliveRule(2);
        else p.removeComeAliveRule(2);
        if(comeAlive3Checkbox.isSelected()) p.addComeAliveRule(3);
        else p.removeComeAliveRule(3);
        if(comeAlive4Checkbox.isSelected()) p.addComeAliveRule(4);
        else p.removeComeAliveRule(4);
        if(comeAlive5Checkbox.isSelected()) p.addComeAliveRule(5);
        else p.removeComeAliveRule(5);
        if(comeAlive6Checkbox.isSelected()) p.addComeAliveRule(6);
        else p.removeComeAliveRule(6);
        if(comeAlive7Checkbox.isSelected()) p.addComeAliveRule(7);
        else p.removeComeAliveRule(7);
        if(comeAlive8Checkbox.isSelected()) p.addComeAliveRule(8);
        else p.removeComeAliveRule(8);

        if(stayAlive0Checkbox.isSelected()) p.addStayAliveRule(0);
        else p.removeStayAliveRule(0);
        if(stayAlive1Checkbox.isSelected()) p.addStayAliveRule(1);
        else p.removeStayAliveRule(1);
        if(stayAlive2Checkbox.isSelected()) p.addStayAliveRule(2);
        else p.removeStayAliveRule(2);
        if(stayAlive3Checkbox.isSelected()) p.addStayAliveRule(3);
        else p.removeStayAliveRule(3);
        if(stayAlive4Checkbox.isSelected()) p.addStayAliveRule(4);
        else p.removeStayAliveRule(4);
        if(stayAlive5Checkbox.isSelected()) p.addStayAliveRule(5);
        else p.removeStayAliveRule(5);
        if(stayAlive6Checkbox.isSelected()) p.addStayAliveRule(6);
        else p.removeStayAliveRule(6);
        if(stayAlive7Checkbox.isSelected()) p.addStayAliveRule(7);
        else p.removeStayAliveRule(7);
        if(stayAlive8Checkbox.isSelected()) p.addStayAliveRule(8);
        else p.removeStayAliveRule(8);

        logger.info("Règles mises à jour : prend vie " + p.getStayAliveSet() + ", reste en vie " + p.getComeAliveSet());
    }
}
