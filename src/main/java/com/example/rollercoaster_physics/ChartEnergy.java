package com.example.rollercoaster_physics;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;

// live energy vs time graph — KE (blue) and PE (green) on the same axis
public class ChartEnergy {

    // chart area boundaries in pixels
    private static final double W      = 175;
    private static final double H      = 100;
    private static final double LEFT   = 28;    // left margin for y-axis labels
    private static final double BOTTOM = H - 8;
    private static final double RIGHT  = W - 6;
    private static final double TOP    = 14;    // below title text

    private Pane     pane;
    private Polyline keLine;    // kinetic energy curve — blue
    private Polyline peLine;    // potential energy curve — green
    private Text     maxLabel;  // top y-axis value, shared by both curves

    public ChartEnergy() {
        pane = new Pane();
        pane.setPrefSize(W, H);

        Rectangle bg = new Rectangle(0, 0, W, H);   // white chart background
        bg.setFill(Color.WHITE);
        bg.setStroke(Color.rgb(180, 180, 200));
        bg.setStrokeWidth(0.5);

        Text title = new Text(5, 12, "Energy (J) vs Time (s)");
        title.setFill(Color.rgb(30, 50, 100));
        title.setFont(Font.font("Courier New", 10));

        // legend boxes
        Rectangle keBox = new Rectangle(W - 55, 14, 8, 7); // KE colour swatch
        keBox.setFill(Color.rgb(30, 100, 220));
        Text keTag = new Text(W - 44, 21, "KE");
        keTag.setFill(Color.rgb(30, 50, 100));
        keTag.setFont(Font.font("Courier New", 8));

        Rectangle peBox = new Rectangle(W - 22, 14, 8, 7); // PE colour swatch
        peBox.setFill(Color.rgb(0, 160, 60));
        Text peTag = new Text(W - 11, 21, "PE");
        peTag.setFill(Color.rgb(30, 50, 100));
        peTag.setFont(Font.font("Courier New", 8));

        Line yAxis = new Line(LEFT, TOP, LEFT, BOTTOM);     // vertical axis
        yAxis.setStroke(Color.rgb(100, 100, 120));
        yAxis.setStrokeWidth(0.8);

        Line xAxis = new Line(LEFT, BOTTOM, RIGHT, BOTTOM); // horizontal time axis
        xAxis.setStroke(Color.rgb(100, 100, 120));
        xAxis.setStrokeWidth(0.8);

        maxLabel = new Text(2, TOP + 6, "");    // top of y-axis, updated each frame
        maxLabel.setFill(Color.rgb(80, 80, 100));
        maxLabel.setFont(Font.font("Courier New", 8));

        Text minLabel = new Text(2, BOTTOM, "0"); // fixed at 0 — energy is always positive
        minLabel.setFill(Color.rgb(80, 80, 100));
        minLabel.setFont(Font.font("Courier New", 8));

        keLine = new Polyline();                // blue KE curve
        keLine.setStroke(Color.rgb(30, 100, 220));
        keLine.setStrokeWidth(1.5);
        keLine.setFill(null);

        peLine = new Polyline();                // green PE curve
        peLine.setStroke(Color.rgb(0, 160, 60));
        peLine.setStrokeWidth(1.5);
        peLine.setFill(null);

        pane.getChildren().addAll(bg, title, keBox, keTag, peBox, peTag,
                yAxis, xAxis, maxLabel, minLabel, keLine, peLine);
    }

    public Pane getPane() { return pane; }

    // redraws both curves each frame using all stored energy values
    public void update(MotionData data) {
        keLine.getPoints().clear();
        peLine.getPoints().clear();

        ArrayList<Double> keValues = data.getKineticEnergies();
        ArrayList<Double> peValues = data.getPotentialEnergies();
        if (keValues.size() < 2) { maxLabel.setText(""); return; }

        // both curves share the same scale — use highest value across KE and PE
        double maxVal = 1; // minimum 1J to avoid division by zero at start
        for (int i = 0; i < keValues.size(); i++) {
            if (keValues.get(i) > maxVal) maxVal = keValues.get(i);
            if (peValues.get(i) > maxVal) maxVal = peValues.get(i);
        }

        maxLabel.setText(String.format("%.1f", maxVal));

        double chartW = RIGHT - LEFT;  // usable plot width
        double chartH = BOTTOM - TOP;  // usable plot height

        // map each value to pixel coordinates and plot both curves
        for (int i = 0; i < keValues.size(); i++) {
            double x   = LEFT + (double) i / (keValues.size() - 1) * chartW;
            double yke = BOTTOM - (keValues.get(i) / maxVal) * chartH;
            double ype = BOTTOM - (peValues.get(i) / maxVal) * chartH;
            keLine.getPoints().addAll(x, yke);
            peLine.getPoints().addAll(x, ype);
        }
    }
}