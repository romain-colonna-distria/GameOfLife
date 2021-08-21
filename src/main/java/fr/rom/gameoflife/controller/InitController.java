package fr.rom.gameoflife.controller;


import fr.rom.gameoflife.utils.Properties;

import fr.rom.gameoflife.utils.Validation.InitFormValidator;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import java.util.ArrayList;


public class InitController {
    private Stage stage;

    private final static Logger logger = Logger.getLogger(InitController.class);

    private InitFormValidator validator;

    @FXML
    private TextField nbRowsTextField;
    @FXML
    private TextField nbColomnsTextField;
    @FXML
    private ChoiceBox<String> shapeMenuButton;
    @FXML
    private TextField svgPathTextField;
    @FXML
    private TextField cellHeightTextField;
    @FXML
    private TextField cellWidthTextField;

    @FXML
    private Label nbColumnsErrorLabel;
    @FXML
    private Label nbRowsErrorLabel;
    @FXML
    private Label cellsHeightErrorLabel;
    @FXML
    private Label cellsWidthErrorLabel;
    @FXML
    private Label svgPathErrorLabel;



    public void init(Stage stage){
        this.stage = stage;
        this.validator = new InitFormValidator();
        this.nbRowsTextField.getScene().getWindow().setOnCloseRequest((event -> logger.info("Fin de la session")));
    }

    private boolean checkValidity(){
        boolean isValid = true;
        ArrayList<String> errors;

        if((errors = validator.validateNbRows(nbRowsTextField.getText())).size() > 0){
            nbRowsErrorLabel.textProperty().setValue(errors.get(0));
            isValid = false;
        }
        if((errors = validator.validateNbColumns(nbColomnsTextField.getText())).size() > 0){
            nbColumnsErrorLabel.textProperty().setValue(errors.get(0));
            isValid = false;
        }
        if((errors = validator.validateCellsHeight(cellHeightTextField.getText())).size() > 0){
            cellsHeightErrorLabel.textProperty().setValue(errors.get(0));
            isValid = false;
        }
        if((errors = validator.validateCellsWidth(cellWidthTextField.getText())).size() > 0){
            cellsWidthErrorLabel.textProperty().setValue(errors.get(0));
            isValid = false;
        }

        return isValid;
    }

    private void cleanErrorsMessages(){
        nbRowsErrorLabel.textProperty().setValue("");
        nbColumnsErrorLabel.textProperty().setValue("");
        cellsHeightErrorLabel.textProperty().setValue("");
        cellsWidthErrorLabel.textProperty().setValue("");
        svgPathErrorLabel.textProperty().setValue("");
    }

    @FXML
    public void runPersonalShape(){
        Properties properties = Properties.getInstance();
        cleanErrorsMessages();

        ArrayList<String> errors;
        if((errors = validator.validateSVGPath(svgPathTextField.getText())).size() > 0) {
            checkValidity();
            svgPathErrorLabel.textProperty().setValue(errors.get(0));
            return;
        }

        if(!checkValidity()) return;

        properties.setShapeString(svgPathTextField.getText());

        run();
    }

    @FXML
    public void runDefineShape(){
        Properties properties = Properties.getInstance();
        cleanErrorsMessages();

        if (!checkValidity()) return;

        switch (shapeMenuButton.getValue()) {
            case "rectangle":
                properties.setShapeString("M 0 0 L 10 0 L 10 10 L 0 10 Z");
                break;
            case "ovale":
                properties.setShapeString("M 0 5 a 5 5 0 1 1 10 0 a 5 5 0 1 1 -10 0");
                break;
            case "triangle":
                properties.setShapeString("M 0 0 h 10 L 5 10 Z");
                break;
            case "nuage":
                properties.setShapeString("M 72 19 C 75 15 77 13 80 11 C 83 8 90 3 94 3 C 109 -2 131 3 141 15 C 146 20 149 25 151 31 C 152 33 153 36 153 38 C 153 39 153 40 154 41 C 154 41 156 41 157 42 C 159 42 160 43 162 44 C 168 48 173 55 174 63 C 175 76 169 86 158 91 C 155 92 152 93 150 93 C 150 93 27 93 27 93 C 24 93 22 92 20 92 C 9 88 2 78 1 67 C 1 55 9 45 20 41 C 24 40 27 40 31 40 C 31 38 33 34 34 32 C 38 26 43 22 50 21 C 53 20 55 20 58 20 C 62 20 66 21 69 23 C 70 22 71 20 72 19 Z");
                break;
            case "etoile":
                properties.setShapeString("M 4 5.5 L 6 6.9641 L 5.7321 4.5 L 8 3.5 L 5.7321 2.5 L 6 0.0359 L 4 1.5 L 2 0.0359 L 2.2679 2.5 L 0 3.5 L 2.2679 4.5 L 2 6.9641 z");
                break;
            default:
                logger.warn("Forme de cellule non reconnu : " + shapeMenuButton.getValue() + ". Application de la forme par défaut");
                properties.setShapeString("M 0 0 L 10 0 L 10 10 L 0 10 Z");
                break;
        }
        run();
    }

    public void run(){
        Properties properties = Properties.getInstance();
        properties.setGridNbRows(Integer.parseInt(nbRowsTextField.getText()));
        properties.setGridNbColumns(Integer.parseInt(nbColomnsTextField.getText()));
        properties.setCellWidth(Double.parseDouble(cellWidthTextField.getText()));
        properties.setCellHeight(Double.parseDouble(cellHeightTextField.getText()));

        try {
            Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
            double width = primaryScreenBounds.getWidth();
            double height = primaryScreenBounds.getHeight() - 25;

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/game_of_life_view.fxml"));
            BorderPane root = fxmlLoader.load();
            root.setPrefSize(width, height);

            Stage stage = new Stage();
            stage.setTitle("Jeu de la vie v2.0");
            stage.setScene(new Scene(root));

            GameOfLifeController controller = fxmlLoader.getController();
            controller.init();

            stage.show();
            this.stage.close();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
