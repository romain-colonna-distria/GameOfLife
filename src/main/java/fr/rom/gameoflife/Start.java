package fr.rom.gameoflife;

import fr.rom.gameoflife.controller.InitController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;


public class Start extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/init_view.fxml"));
        AnchorPane root = fxmlLoader.load();

        primaryStage.setTitle("Jeu de la vie v2.0");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);

        InitController controller = fxmlLoader.getController();
        controller.init(primaryStage);

        primaryStage.show();
    }
}
