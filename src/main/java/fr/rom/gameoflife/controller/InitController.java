package fr.rom.gameoflife.controller;


import fr.rom.gameoflife.utils.Language;
import fr.rom.gameoflife.utils.Properties;

import fr.rom.gameoflife.utils.Validation.InitFormValidator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;


public class InitController {
    private Stage stage;

    private final static Logger logger = Logger.getLogger(InitController.class);

    private InitFormValidator validator = new InitFormValidator();

    @FXML
    private Label titleLabel;
    @FXML
    private Label versionLabel;
    @FXML
    private ChoiceBox languageChoiceBox;

    @FXML
    private Label gridLabel;
    @FXML
    private Label gridSizeLabel;
    @FXML
    private TextField nbColomnsTextField;
    @FXML
    private Label nbColumnsErrorLabel;
    @FXML
    private TextField nbRowsTextField;
    @FXML
    private Label nbRowsErrorLabel;

    @FXML
    private Label cellsLabel;
    @FXML
    private Label cellsSizeLabel;
    @FXML
    private TextField cellHeightTextField;
    @FXML
    private Label cellsHeightErrorLabel;
    @FXML
    private TextField cellWidthTextField;
    @FXML
    private Label cellsWidthErrorLabel;

    @FXML
    private Label defineShapeLabel;
    @FXML
    private ChoiceBox<String> shapeMenuChoiceBox;
    @FXML
    private Button runDefineButton;
    @FXML
    private Label orLabel;
    @FXML
    private Label personnalShapeLabel;
    @FXML
    private TextField svgPathTextField;
    @FXML
    private Label svgPathErrorLabel;
    @FXML
    private Button runPersonalButton;



    public void init(Stage stage){
        this.stage = stage;
        this.titleLabel.getScene().getWindow().setOnCloseRequest((event -> {
            logger.info(Language.get("log.endSession"));
            File statisticsFile = new File("stats.txt");
            statisticsFile.delete();
        }));

        initShapesChoiceBoxItems();
        initLanguageChoiceBoxItems();
        initTextWithLocaleLanguage();
    }

    private void initLanguageChoiceBoxItems(){
        ObservableList<String> languages = FXCollections.observableArrayList();
        Language.getSupportedLocales().forEach(language -> languages.add(language.toString()));
        this.languageChoiceBox.setItems(languages);
        this.languageChoiceBox.setValue(Language.getLocale().toString());

        this.languageChoiceBox.setOnAction((event -> {
            Locale locale = Language.getLocaleFromString(this.languageChoiceBox.getValue().toString());
            Language.setLocale(locale);
            initShapesChoiceBoxItems();
        }));
    }

    private void initShapesChoiceBoxItems(){
        ObservableList<String> shapes = FXCollections.observableArrayList();
        shapes.add(Language.get("label.defineShape.rectangle"));
        shapes.add(Language.get("label.defineShape.oval"));
        shapes.add(Language.get("label.defineShape.triangle"));
        shapes.add(Language.get("label.defineShape.cloud"));
        shapes.add(Language.get("label.defineShape.star"));

        this.shapeMenuChoiceBox.setItems(shapes);
        this.shapeMenuChoiceBox.setValue(Language.get("label.defineShape.rectangle"));
    }

    private void initTextWithLocaleLanguage(){
        this.titleLabel.textProperty().bind(Language.createStringBinding("label.title"));
        this.versionLabel.textProperty().bind(Language.createStringBinding("label.version"));

        this.gridLabel.textProperty().bind(Language.createStringBinding("label.grid"));
        this.gridSizeLabel.textProperty().bind(Language.createStringBinding("label.gridSize"));
        this.nbColomnsTextField.promptTextProperty().bind(Language.createStringBinding("placeholder.grid.nbColumns"));
        this.nbRowsTextField.promptTextProperty().bind(Language.createStringBinding("placeholder.grid.nbRows"));

        this.cellsLabel.textProperty().bind(Language.createStringBinding("label.cells"));
        this.cellsSizeLabel.textProperty().bind(Language.createStringBinding("label.cellsSize"));
        this.cellHeightTextField.promptTextProperty().bind(Language.createStringBinding("placeholder.cell.pixelHeight"));
        this.cellWidthTextField.promptTextProperty().bind(Language.createStringBinding("placeholder.cell.pixelWidth"));

        this.defineShapeLabel.textProperty().bind(Language.createStringBinding("label.defineShape"));
        this.runDefineButton.textProperty().bind(Language.createStringBinding("label.run"));
        this.orLabel.textProperty().bind(Language.createStringBinding("label.shape.or"));
        this.personnalShapeLabel.textProperty().bind(Language.createStringBinding("label.personnalShape"));
        this.runPersonalButton.textProperty().bind(Language.createStringBinding("label.run"));
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

        if(shapeMenuChoiceBox.getValue().equals(Language.get("label.defineShape.rectangle"))) {
            properties.setShapeString("M 0 0 L 10 0 L 10 10 L 0 10 Z");
        } else if(shapeMenuChoiceBox.getValue().equals(Language.get("label.defineShape.oval"))) {
            properties.setShapeString("M 0 5 a 5 5 0 1 1 10 0 a 5 5 0 1 1 -10 0");
        } else if(shapeMenuChoiceBox.getValue().equals(Language.get("label.defineShape.triangle"))) {
            properties.setShapeString("M 0 0 h 10 L 5 10 Z");
        } else if(shapeMenuChoiceBox.getValue().equals(Language.get("label.defineShape.cloud"))) {
            properties.setShapeString("M 72 19 C 75 15 77 13 80 11 C 83 8 90 3 94 3 C 109 -2 131 3 141 15 C 146 20 149 25 151 31 C 152 33 153 36 153 38 C 153 39 153 40 154 41 C 154 41 156 41 157 42 C 159 42 160 43 162 44 C 168 48 173 55 174 63 C 175 76 169 86 158 91 C 155 92 152 93 150 93 C 150 93 27 93 27 93 C 24 93 22 92 20 92 C 9 88 2 78 1 67 C 1 55 9 45 20 41 C 24 40 27 40 31 40 C 31 38 33 34 34 32 C 38 26 43 22 50 21 C 53 20 55 20 58 20 C 62 20 66 21 69 23 C 70 22 71 20 72 19 Z");
        } else if(shapeMenuChoiceBox.getValue().equals(Language.get("label.defineShape.star"))) {
            properties.setShapeString("M 4 5.5 L 6 6.9641 L 5.7321 4.5 L 8 3.5 L 5.7321 2.5 L 6 0.0359 L 4 1.5 L 2 0.0359 L 2.2679 2.5 L 0 3.5 L 2.2679 4.5 L 2 6.9641 z");
        } else {
            logger.warn(Language.get("log.unreadableShape") + shapeMenuChoiceBox.getValue() + ". " + Language.createStringBinding("log.applyDefaultShape"));
            properties.setShapeString("M 0 0 L 10 0 L 10 10 L 0 10 Z");
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
            stage.titleProperty().bind(Language.createStringBinding("window.title"));
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
