package fr.rom.gameoflife;

import fr.rom.gameoflife.controller.InitController;
import fr.rom.gameoflife.utils.Language;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.io.IOException;


public class Start extends Application {

    private final static Logger logger = Logger.getLogger(Start.class);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/init_view.fxml"));
        AnchorPane root = fxmlLoader.load();

        primaryStage.titleProperty().bind(Language.createStringBinding("window.init.title"));
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);

        InitController controller = fxmlLoader.getController();
        controller.init(primaryStage);

        primaryStage.show();
        logger.info(Language.get("log.newSession"));
    }
}
