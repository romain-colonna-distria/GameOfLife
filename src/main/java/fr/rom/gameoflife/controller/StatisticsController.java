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

import java.io.*;


public class StatisticsController {
    @FXML
    private AnchorPane rootPane;

    private final EventHandler<ActionEvent> exportStatsHandler = event -> {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));

        File copied = fileChooser.showSaveDialog(null);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    };


    public void init(){
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Génération");
        yAxis.setLabel("Population");

        final LineChart<Number,Number> evolutionLineChart = new LineChart<>(xAxis, yAxis);
        evolutionLineChart.setLegendVisible(false);

        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        double width = primaryScreenBounds.getWidth();
        double height = primaryScreenBounds.getHeight() - 25;
        evolutionLineChart.setPrefWidth(width - 50);
        evolutionLineChart.setPrefHeight(height - 150);
        evolutionLineChart.setTitle("Évolution de la population");

        XYChart.Series<Number, Number> series = new XYChart.Series<>();

        int popMin = -1;
        int popMax = -1;
        double popAvg = 0;
        int nbLine = 1;
        try {
            FileReader fileReader = new FileReader(new File("stats.txt"));
            BufferedReader statisticsReader = new BufferedReader(fileReader);

            String line;

            while ((line = statisticsReader.readLine()) != null) {
                String[] parsedLine = line.split(";");
                if(parsedLine.length != 2) return;
                series.getData().add(new XYChart.Data<>(Integer.parseInt(parsedLine[0]), Integer.parseInt(parsedLine[1])));

                if(popMin == -1) popMin = Integer.parseInt(parsedLine[1]);
                if(popMax == -1) popMax = Integer.parseInt(parsedLine[1]);

                if(Integer.parseInt(parsedLine[1]) < popMin) popMin = Integer.parseInt(parsedLine[1]);
                if(Integer.parseInt(parsedLine[1]) > popMax) popMax = Integer.parseInt(parsedLine[1]);

                popAvg += Integer.parseInt(parsedLine[1]);
                ++nbLine;
            }

            popAvg /= nbLine;
        } catch (IOException e) {
            e.printStackTrace();
        }

        evolutionLineChart.getData().add(series);

        Button exportChartButton = new Button("Exporter les données sous forme de fichier CSV");
        exportChartButton.setLayoutX(width - 370);
        exportChartButton.setLayoutY(height - 140);
        exportChartButton.setOnAction(this.exportStatsHandler);

        GridPane pane = new GridPane();
        pane.setLayoutX(30);
        pane.setLayoutY(height - 150);

        pane.add(new Label("Population minimum : "), 0, 0);
        pane.add(new Label(String.valueOf(popMin)), 1, 0);
        pane.add(new Label("Population maximum : "), 0, 1);
        pane.add(new Label(String.valueOf(popMax)), 1, 1);
        pane.add(new Label("Population moyenne : "), 0, 2);
        pane.add(new Label(String.valueOf(popAvg)), 1, 2);

        rootPane.getChildren().add(evolutionLineChart);
        rootPane.getChildren().add(exportChartButton);
        rootPane.getChildren().add(pane);
    }
}
