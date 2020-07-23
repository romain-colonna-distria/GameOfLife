package fr.rom.gameoflife;

import fr.rom.gameoflife.controller.GameOfLifeController;
import fr.rom.gameoflife.controller.InitController;
import fr.rom.gameoflife.utils.Properties;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;


public class Start extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/init_view.fxml"));
        AnchorPane root = fxmlLoader.load();

        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);

        InitController controller = fxmlLoader.getController();
        controller.init(primaryStage);

        primaryStage.show();
    }
}
