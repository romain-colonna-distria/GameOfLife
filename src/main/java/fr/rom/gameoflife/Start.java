package fr.rom.gameoflife;

import fr.rom.gameoflife.controller.GameOfLifeController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;


public class Start extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        double width = primaryScreenBounds.getWidth();
        double height = primaryScreenBounds.getHeight() - 25;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/game_of_life_view.fxml"));
        BorderPane root = fxmlLoader.load();
        root.setMinSize(600, 377);
        root.setPrefSize(width, height);

        primaryStage.setScene(new Scene(root));

        GameOfLifeController controller = fxmlLoader.getController();
        controller.init(primaryStage);

        primaryStage.show();
    }
}
