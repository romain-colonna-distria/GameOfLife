package fr.rom.gameoflife.controller;


import fr.rom.gameoflife.property.GameProps;
import fr.rom.gameoflife.utils.Message;

import fr.rom.gameoflife.controller.validator.InitFormValidator;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;


public class InitController {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitController.class);

    private Stage stage;
    private final InitFormValidator validator = new InitFormValidator();

    @FXML
    private Label titleLabel;
    @FXML
    private Label versionLabel;
    @FXML
    private ChoiceBox<String> languageChoiceBox;

    @FXML
    private Label gridLabel;
    @FXML
    private Label gridSizeLabel;
    @FXML
    private TextField nbColumnsTextField;
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
    private Label personalShapeLabel;
    @FXML
    private TextField svgPathTextField;
    @FXML
    private Label svgPathErrorLabel;
    @FXML
    private Button runPersonalButton;



    public void init(Stage stage){
        this.stage = stage;
        this.stage.getScene().getWindow().setOnCloseRequest((e -> LOGGER.info(Message.get("log.endSession"))));

        initGameDefaultProperty();
        initShapesChoiceBoxItems();
        initLanguageChoiceBoxItems();
        initTextWithLocaleLanguage();
    }

    private void initGameDefaultProperty() {
        this.nbColumnsTextField.setText(String.valueOf(GameProps.get().getNbColumns()));
        this.nbRowsTextField.setText(String.valueOf(GameProps.get().getNbRows()));
        this.cellWidthTextField.setText(String.valueOf(GameProps.get().getCellWidth()));
        this.cellHeightTextField.setText(String.valueOf(GameProps.get().getCellHeight()));
    }

    private void initLanguageChoiceBoxItems(){
        ObservableList<String> languages = FXCollections.observableArrayList();
        Message.getSupportedLocales().forEach(language -> languages.add(language.toString()));
        this.languageChoiceBox.setItems(languages);
        this.languageChoiceBox.setValue(Message.getLocale().toString());

        this.languageChoiceBox.setOnAction((event -> {
            Locale locale = Message.getLocaleFromString(this.languageChoiceBox.getValue());
            Message.setLocale(locale);
            initShapesChoiceBoxItems();
        }));
    }

    private void initShapesChoiceBoxItems(){
        final ObservableList<String> shapes = FXCollections.observableArrayList();
        shapes.add(Message.get("label.defineShape.rectangle"));
        shapes.add(Message.get("label.defineShape.oval"));
        shapes.add(Message.get("label.defineShape.triangle"));
        shapes.add(Message.get("label.defineShape.cloud"));
        shapes.add(Message.get("label.defineShape.star"));

        this.shapeMenuChoiceBox.setItems(shapes);
        this.shapeMenuChoiceBox.setValue(shapes.get(0));
    }

    private void initTextWithLocaleLanguage(){
        this.titleLabel.textProperty().bind(Message.createStringBinding("game.name"));
        this.versionLabel.textProperty().bind(Message.createStringBinding("label.version"));

        this.gridLabel.textProperty().bind(Message.createStringBinding("label.grid"));
        this.gridSizeLabel.textProperty().bind(Message.createStringBinding("label.gridSize"));
        this.nbColumnsTextField.promptTextProperty().bind(Message.createStringBinding("placeholder.grid.nbColumns"));
        this.nbRowsTextField.promptTextProperty().bind(Message.createStringBinding("placeholder.grid.nbRows"));

        this.cellsLabel.textProperty().bind(Message.createStringBinding("label.cells"));
        this.cellsSizeLabel.textProperty().bind(Message.createStringBinding("label.cellsSize"));
        this.cellHeightTextField.promptTextProperty().bind(Message.createStringBinding("placeholder.cell.pixelHeight"));
        this.cellWidthTextField.promptTextProperty().bind(Message.createStringBinding("placeholder.cell.pixelWidth"));

        this.defineShapeLabel.textProperty().bind(Message.createStringBinding("label.defineShape"));
        this.runDefineButton.textProperty().bind(Message.createStringBinding("label.run"));
        this.orLabel.textProperty().bind(Message.createStringBinding("label.shape.or"));
        this.personalShapeLabel.textProperty().bind(Message.createStringBinding("label.personalShape"));
        this.runPersonalButton.textProperty().bind(Message.createStringBinding("label.run"));
    }

    private boolean checkValidity(){
        boolean isValid = true;

        List<String> errors = validator.validateNbRows(nbRowsTextField.getText());
        if(!errors.isEmpty()){
            nbRowsErrorLabel.textProperty().setValue(errors.get(0));
            isValid = false;
        }
        errors = validator.validateNbColumns(nbRowsTextField.getText());
        if(!errors.isEmpty()){
            nbColumnsErrorLabel.textProperty().setValue(errors.get(0));
            isValid = false;
        }
        errors = validator.validateCellsHeight(nbRowsTextField.getText());
        if(!errors.isEmpty()){
            cellsHeightErrorLabel.textProperty().setValue(errors.get(0));
            isValid = false;
        }
        errors = validator.validateCellsWidth(nbRowsTextField.getText());
        if(!errors.isEmpty()){
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
        cleanErrorsMessages();

        List<String> errors = validator.validateSVGPath(svgPathTextField.getText());
        if(!errors.isEmpty()) {
            checkValidity();
            svgPathErrorLabel.textProperty().setValue(errors.get(0));
            return;
        }

        if(!checkValidity()) return;

        GameProps.get().setShapePath(svgPathTextField.getText());

        run();
    }

    @FXML
    public void runDefineShape(){
        cleanErrorsMessages();
        final GameProps properties = GameProps.get();

        if (!checkValidity()) return;

        if(shapeMenuChoiceBox.getValue().equals(Message.get("label.defineShape.rectangle"))) {
            properties.setShapePath("M 0 0 L 10 0 L 10 10 L 0 10 Z");
        } else if(shapeMenuChoiceBox.getValue().equals(Message.get("label.defineShape.oval"))) {
            properties.setShapePath("M 0 5 a 5 5 0 1 1 10 0 a 5 5 0 1 1 -10 0");
        } else if(shapeMenuChoiceBox.getValue().equals(Message.get("label.defineShape.triangle"))) {
            properties.setShapePath("M 0 0 h 10 L 5 10 Z");
        } else if(shapeMenuChoiceBox.getValue().equals(Message.get("label.defineShape.cloud"))) {
            properties.setShapePath("M 72 19 C 75 15 77 13 80 11 C 83 8 90 3 94 3 C 109 -2 131 3 141 15 C 146 20 149 25 151 31 C 152 33 153 36 153 38 C 153 39 153 40 154 41 C 154 41 156 41 157 42 C 159 42 160 43 162 44 C 168 48 173 55 174 63 C 175 76 169 86 158 91 C 155 92 152 93 150 93 C 150 93 27 93 27 93 C 24 93 22 92 20 92 C 9 88 2 78 1 67 C 1 55 9 45 20 41 C 24 40 27 40 31 40 C 31 38 33 34 34 32 C 38 26 43 22 50 21 C 53 20 55 20 58 20 C 62 20 66 21 69 23 C 70 22 71 20 72 19 Z");
        } else if(shapeMenuChoiceBox.getValue().equals(Message.get("label.defineShape.star"))) {
            properties.setShapePath("M 4 5.5 L 6 6.9641 L 5.7321 4.5 L 8 3.5 L 5.7321 2.5 L 6 0.0359 L 4 1.5 L 2 0.0359 L 2.2679 2.5 L 0 3.5 L 2.2679 4.5 L 2 6.9641 z");
        } else {
            LOGGER.warn(Message.get("log.unreadableShape", shapeMenuChoiceBox.getValue()));
            properties.setShapePath("M 0 0 L 10 0 L 10 10 L 0 10 Z");
        }

        run();
    }

    public void run(){
        final GameProps properties = GameProps.get();
        properties.setNbRows(Integer.parseInt(nbRowsTextField.getText()));
        properties.setNbColumns(Integer.parseInt(nbColumnsTextField.getText()));
        properties.setCellWidth(Integer.parseInt(cellWidthTextField.getText()));
        properties.setCellHeight(Integer.parseInt(cellHeightTextField.getText()));

        try {
            final Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
            final double width = primaryScreenBounds.getWidth();
            final double height = primaryScreenBounds.getHeight() - 25;

            final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/game_of_life_view.fxml"));
            final BorderPane root = fxmlLoader.load();
            root.setPrefSize(width, height);

            final Stage gameStage = new Stage();
            gameStage.titleProperty().bind(Message.createStringBinding("window.game.title"));
            gameStage.setScene(new Scene(root));

            final GameOfLifeController controller = fxmlLoader.getController();
            controller.init();

            gameStage.show();
            this.stage.close();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
