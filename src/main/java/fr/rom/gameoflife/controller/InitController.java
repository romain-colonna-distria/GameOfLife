package fr.rom.gameoflife.controller;

import fr.rom.gameoflife.utils.Properties;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class InitController {
    private Stage stage;

    @FXML
    private TextField nbRowsTextField;
    @FXML
    private TextField nbColomnsTextField;

    @FXML
    private ChoiceBox shapeMenuButton;

    @FXML
    private TextField cellHeightTextField;
    @FXML
    private TextField cellWidthTextField;

    @FXML
    private TextField nbThreads;

    public void init(Stage stage){
        this.stage = stage;
    }

    @FXML
    public void run(){
        if (checkValidity()){
            Properties properties = new Properties();
            properties.setGridNbRows(Integer.parseInt(nbRowsTextField.getText()));
            properties.setGridNbColumns(Integer.parseInt(nbColomnsTextField.getText()));
            properties.setShapeString(shapeMenuButton.getValue().toString());
            properties.setCellWidth(Double.parseDouble(cellWidthTextField.getText()));
            properties.setCellHeight(Double.parseDouble(cellHeightTextField.getText()));
            properties.setNbSimultaneousThreads(Integer.parseInt(nbThreads.getText()));


            try {
                Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
                double width = primaryScreenBounds.getWidth();
                double height = primaryScreenBounds.getHeight() - 25;

                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/game_of_life_view.fxml"));
                BorderPane root = fxmlLoader.load();
                root.setPrefSize(width, height);

                Stage stage = new Stage();
                stage.setTitle("Jeu de la vie v2.0");
                stage.setScene(new Scene(root));

                GameOfLifeController controller = fxmlLoader.getController();
                controller.init(stage, properties);

                stage.show();

                this.stage.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    private boolean checkValidity(){
        try {
            Integer.parseInt(nbRowsTextField.getText());
            Integer.parseInt(nbColomnsTextField.getText());

            if(!shapeMenuButton.getValue().toString().equals("rectangle") && !shapeMenuButton.getValue().toString().equals("ovale")) return false;

            Double.parseDouble(cellHeightTextField.getText());
            Double.parseDouble(cellWidthTextField.getText());

            Integer.parseInt(nbThreads.getText());

            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}