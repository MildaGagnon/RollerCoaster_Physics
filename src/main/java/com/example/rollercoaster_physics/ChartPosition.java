package com.example.rollercoaster_physics;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class ChartPosition {

    private static final double W = 390;
    private static final double H = 150;

    // chart margins
    private static final double LEFT   = 42;
    private static final double BOTTOM = H - 22;
    private static final double RIGHT  = W - 8;
    private static final double TOP    = 20;

    private Pane pane;
    private Polyline line;   // the actual graph line
    private Text     maxLabel;
    private Text     minLabel;
    private Text     title;

    public ChartPosition() {

        pane = new Pane();
        pane.setPrefSize(W, H);

        // white background
        Rectangle bg = new Rectangle(0, 0, W, H);
        bg.setFill(Color.WHITE);
        bg.setStroke(Color.rgb(180, 180, 200));
        bg.setStrokeWidth(1);

        // title
        title = new Text(6, 13, "Position (m) vs Time (s)");
        title.setFill(Color.rgb(30, 50, 100));
        title.setFont(Font.font("Courier New", 11));

        // y axis
        Line yAxis = new Line(LEFT, TOP, LEFT, BOTTOM);
        yAxis.setStroke(Color.rgb(100, 100, 120));

        // x axis
        Line xAxis = new Line(LEFT, BOTTOM, RIGHT, BOTTOM);
        xAxis.setStroke(Color.rgb(100, 100, 120));

        // axis value labels (updated when data changes)
        maxLabel = new Text(2, TOP + 6, "");
        maxLabel.setFill(Color.rgb(80, 80, 100));
        maxLabel.setFont(Font.font("Courier New", 9));

        minLabel = new Text(2, BOTTOM, "0");
        minLabel.setFill(Color.rgb(80, 80, 100));
        minLabel.setFont(Font.font("Courier New", 9));

        // the polyline that traces the graph
        line = new Polyline();
        line.setStroke(Color.rgb(30, 100, 220));
        line.setStrokeWidth(2);
        line.setFill(null);

        pane.getChildren().addAll(bg, title, yAxis, xAxis,
                maxLabel, minLabel, line);
    }

    public Pane getPane() {
        return pane;
    }

    // redraws the graph every time new data arrives
    public void update(MotionData data) {

        line.getPoints().clear();

        if (data.size() < 2) {
            maxLabel.setText("");
            return;
        }

        double[] yValues = new double[data.size()];
        for (int i = 0; i < data.size(); i++) {
            yValues[i] = data.getPositions().get(i);
        }

        // find min and max so we can scale the graph
        double maxVal = yValues[0];
        double minVal = yValues[0];
        for (int i = 1; i < yValues.length; i++) {
            if (yValues[i] > maxVal) { maxVal = yValues[i]; }
            if (yValues[i] < minVal) { minVal = yValues[i]; }
        }

        double range  = maxVal - minVal;
        if (range == 0) { range = 1; }

        double chartW = RIGHT  - LEFT;
        double chartH = BOTTOM - TOP;

        maxLabel.setText(String.format("%.1f", maxVal));

        // add a point for every data entry
        for (int i = 0; i < yValues.length; i++) {
            double x = LEFT + (double) i / (yValues.length - 1) * chartW;
            double y = BOTTOM - ((yValues[i] - minVal) / range) * chartH;
            line.getPoints().addAll(x, y);
        }
    }
}
