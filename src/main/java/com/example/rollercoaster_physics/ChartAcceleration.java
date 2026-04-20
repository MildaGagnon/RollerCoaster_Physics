package com.example.rollercoaster_physics;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
// live acceleration vs time graph — orange curve

public class ChartAcceleration {

        // chart area boundaries in pixels
        private static final double W      = 175;
        private static final double H      = 100;
        private static final double LEFT   = 28;    // left margin for y-axis labels
        private static final double BOTTOM = H - 8;
        private static final double RIGHT  = W - 6;
        private static final double TOP    = 14;    // below title text

        private Pane     pane;
        private Polyline line;       // acceleration curve, redrawn every frame
        private Text     maxLabel;   // top y-axis value
        private Text     minLabel;   // bottom y-axis value
        private Line     zeroLine;   // dashed a=0 reference, visible only when acceleration goes negative

        public ChartAcceleration() {
            pane = new Pane();
            pane.setPrefSize(W, H);

            Rectangle bg = new Rectangle(0, 0, W, H);   // white chart background
            bg.setFill(Color.WHITE);
            bg.setStroke(Color.rgb(180, 180, 200));
            bg.setStrokeWidth(0.5);

            Text title = new Text(5, 12, "Acceleration (m/s^2) vs Time (s)");
            title.setFill(Color.rgb(30, 50, 100));
            title.setFont(Font.font("Courier New", 10));

            Line yAxis = new Line(LEFT, TOP, LEFT, BOTTOM);    // vertical axis
            yAxis.setStroke(Color.rgb(100, 100, 120));
            yAxis.setStrokeWidth(0.8);

            Line xAxis = new Line(LEFT, BOTTOM, RIGHT, BOTTOM); // horizontal time axis
            xAxis.setStroke(Color.rgb(100, 100, 120));
            xAxis.setStrokeWidth(0.8);

            zeroLine = new Line(LEFT, BOTTOM, RIGHT, BOTTOM);  // starts hidden at bottom
            zeroLine.setStroke(Color.rgb(180, 180, 200));
            zeroLine.setStrokeWidth(0.8);
            zeroLine.getStrokeDashArray().addAll(4.0, 3.0);    // dashed pattern
            zeroLine.setVisible(false);

            maxLabel = new Text(2, TOP + 6, "");    // top of y-axis, updated each frame
            maxLabel.setFill(Color.rgb(80, 80, 100));
            maxLabel.setFont(Font.font("Courier New", 8));

            minLabel = new Text(2, BOTTOM, "0");    // bottom of y-axis
            minLabel.setFill(Color.rgb(80, 80, 100));
            minLabel.setFont(Font.font("Courier New", 8));

            line = new Polyline();                  // orange acceleration curve
            line.setStroke(Color.rgb(220, 130, 0));
            line.setStrokeWidth(1.5);
            line.setFill(null);

            pane.getChildren().addAll(bg, title, yAxis, xAxis, zeroLine,
                    maxLabel, minLabel, line);
        }

        public Pane getPane() { return pane; }

        // redraws the curve each frame using all stored acceleration values
        public void update(MotionData data) {
            line.getPoints().clear();
            ArrayList<Double> yValues = data.getAccelerations();
            if (yValues.size() < 2) {
                maxLabel.setText(""); minLabel.setText("0");
                zeroLine.setVisible(false); return;
            }

            // scan for min and max in the current data window
            double maxVal = yValues.get(0);
            double minVal = yValues.get(0);
            for (double v : yValues) {
                if (v > maxVal) maxVal = v;
                if (v < minVal) minVal = v;
            }

            // clamp range to always include 0 so axis never floats
            maxVal = Math.max(maxVal, 0);
            minVal = Math.min(minVal, 0);
            double range = maxVal - minVal;
            if (range == 0) range = 1; // avoid division by zero when all values are equal

            maxLabel.setText(String.format("%.1f", maxVal));
            minLabel.setText(String.format("%.1f", minVal));

            double chartW = RIGHT - LEFT;  // usable plot width
            double chartH = BOTTOM - TOP;  // usable plot height

            // show zero line only when negative values exist
            boolean hasNegative = minVal < -0.05;
            zeroLine.setVisible(hasNegative);
            if (hasNegative) {
                // position zero line proportionally between min and max
                double zeroY = BOTTOM - ((0 - minVal) / range) * chartH;
                zeroLine.setStartY(zeroY);
                zeroLine.setEndY(zeroY);
            }

            // map each value to pixel coordinates and plot
            for (int i = 0; i < yValues.size(); i++) {
                double x = LEFT + (double) i / (yValues.size() - 1) * chartW;
                double y = BOTTOM - ((yValues.get(i) - minVal) / range) * chartH;
                line.getPoints().addAll(x, y);
            }
        }
    }