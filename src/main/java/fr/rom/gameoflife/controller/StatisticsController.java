package fr.rom.gameoflife.controller;


import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import org.apache.log4j.Logger;

import java.io.*;


public class StatisticsController {
    private double screenWidth;
    private double screenHeight;

    private int popMin = -1;
    private int popMax = -1;
    private double popAvg = 0;

    private final static Logger logger = Logger.getLogger(InitController.class);

    @FXML
    private AnchorPane rootPane;

    private final EventHandler<ActionEvent> exportStatsHandler = event -> {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));

        File copied = fileChooser.showSaveDialog(this.rootPane.getScene().getWindow());
        if(copied == null) return;
        File original = new File("stats.txt");
        try {
            InputStream in = new BufferedInputStream(new FileInputStream(original));
            OutputStream out = new BufferedOutputStream(new FileOutputStream(copied));

            out.write("generationNb;populationNb\n".getBytes());
            out.flush();

            byte[] buffer = new byte[1024];
            int lengthRead;
            while ((lengthRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, lengthRead);
                out.flush();
            }
            logger.info("Statistiques sauvegardé dans le fichier " + copied.getPath());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    };


    public void init(){
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        this.screenWidth = primaryScreenBounds.getWidth();
        this.screenHeight = primaryScreenBounds.getHeight() - 25;

        initChart();
        initExportButton();
        initGridStats();
    }

    private void initChart(){
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Génération");
        yAxis.setLabel("Population");

        final LineChart<Number,Number> evolutionLineChart = new LineChart<>(xAxis, yAxis);
        evolutionLineChart.setLegendVisible(false);

        evolutionLineChart.setPrefWidth(this.screenWidth - 50);
        evolutionLineChart.setPrefHeight(this.screenHeight - 150);
        evolutionLineChart.setLayoutX(10);
        evolutionLineChart.setTitle("Évolution de la population");

        XYChart.Series<Number, Number> series = new XYChart.Series<>();

        int nbLine = 1;
        try {
            FileReader fileReader = new FileReader(new File("stats.txt"));
            BufferedReader statisticsReader = new BufferedReader(fileReader);

            String line;

            while ((line = statisticsReader.readLine()) != null) {
                String[] parsedLine = line.split(";");
                if(parsedLine.length != 2) return;
                series.getData().add(new XYChart.Data<>(Integer.parseInt(parsedLine[0]), Integer.parseInt(parsedLine[1])));

                if(this.popMin == -1) this.popMin = Integer.parseInt(parsedLine[1]);
                if(this.popMax == -1) this.popMax = Integer.parseInt(parsedLine[1]);

                if(Integer.parseInt(parsedLine[1]) < this.popMin) this.popMin = Integer.parseInt(parsedLine[1]);
                if(Integer.parseInt(parsedLine[1]) > this.popMax) this.popMax = Integer.parseInt(parsedLine[1]);

                this.popAvg += Integer.parseInt(parsedLine[1]);
                ++nbLine;
            }

            this.popAvg /= nbLine;
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

        evolutionLineChart.getData().add(series);

        this.rootPane.getChildren().add(evolutionLineChart);
    }

    private void initExportButton(){
        Button exportChartButton = new Button("Exporter les données sous forme de fichier CSV");
        exportChartButton.setLayoutX(this.screenWidth - 370);
        exportChartButton.setLayoutY(this.screenHeight - 140);
        exportChartButton.setOnAction(this.exportStatsHandler);

        this.rootPane.getChildren().add(exportChartButton);
    }

    private void initGridStats(){
        GridPane pane = new GridPane();
        pane.setLayoutX(30);
        pane.setLayoutY(this.screenHeight - 150);

        pane.add(new Label("Population minimum : "), 0, 0);
        pane.add(new Label(String.valueOf(this.popMin)), 1, 0);
        pane.add(new Label("Population maximum : "), 0, 1);
        pane.add(new Label(String.valueOf(this.popMax)), 1, 1);
        pane.add(new Label("Population moyenne : "), 0, 2);
        pane.add(new Label(String.valueOf(this.popAvg)), 1, 2);

        this.rootPane.getChildren().add(pane);
    }
}
