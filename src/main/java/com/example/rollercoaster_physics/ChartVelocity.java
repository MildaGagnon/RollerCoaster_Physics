package com.example.rollercoaster_physics;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;

public class ChartVelocity {

    private static final double W = 175;
    private static final double H = 100;
    private static final double LEFT   = 28;
    private static final double BOTTOM = H - 8;
    private static final double RIGHT  = W - 6;
    private static final double TOP    = 14;


    private Pane     pane;
    private Polyline line;
    private Text     maxLabel;

    public ChartVelocity() {
        pane = new Pane();
        pane.setPrefSize(W, H);

        Rectangle bg = new Rectangle(0, 0, W, H);
        bg.setFill(Color.WHITE);
        bg.setStroke(Color.rgb(180, 180, 200));
        bg.setStrokeWidth(0.5);

        Text title = new Text(5, 12, "Velocity (m/s) vs Time (s)");
        title.setFill(Color.rgb(30, 50, 100));
        title.setFont(Font.font("Courier New", 10));

        Line yAxis = new Line(LEFT, TOP, LEFT, BOTTOM);
        yAxis.setStroke(Color.rgb(100, 100, 120));
        yAxis.setStrokeWidth(0.8);

        Line xAxis = new Line(LEFT, BOTTOM, RIGHT, BOTTOM);
        xAxis.setStroke(Color.rgb(100, 100, 120));
        xAxis.setStrokeWidth(0.8);

        maxLabel = new Text(2, TOP + 6, "");
        maxLabel.setFill(Color.rgb(80, 80, 100));
        maxLabel.setFont(Font.font("Courier New", 8));

        Text minLabel = new Text(2, BOTTOM, "0");
        minLabel.setFill(Color.rgb(80, 80, 100));
        minLabel.setFont(Font.font("Courier New", 8));

        line = new Polyline();
        line.setStroke(Color.rgb(0, 170, 70));
        line.setStrokeWidth(1.5);
        line.setFill(null);

        pane.getChildren().addAll(bg, title, yAxis, xAxis, maxLabel, minLabel, line);
    }

    public Pane getPane() { return pane; }

    public void update(MotionData data) {
        line.getPoints().clear();
        ArrayList<Double> yValues = data.getVelocities();
        if (yValues.size() < 2) { maxLabel.setText(""); return; }

        double maxVal = yValues.get(0);
        double minVal = yValues.get(0);
        for (int i = 1; i < yValues.size(); i++) {
            if (yValues.get(i) > maxVal) maxVal = yValues.get(i);
            if (yValues.get(i) < minVal) minVal = yValues.get(i);
        }
        double range = maxVal - minVal;
        if (range == 0) range = 1;

        maxLabel.setText(String.format("%.1f", maxVal));

        double chartW = RIGHT - LEFT;
        double chartH = BOTTOM - TOP;
        for (int i = 0; i < yValues.size(); i++) {
            double x = LEFT + (double) i / (yValues.size() - 1) * chartW;
            double y = BOTTOM - ((yValues.get(i) - minVal) / range) * chartH;
            line.getPoints().addAll(x, y);
        }
    }
}