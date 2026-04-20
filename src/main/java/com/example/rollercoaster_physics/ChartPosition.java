package com.example.rollercoaster_physics;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;

// live position vs time graph — blue curve
    public class ChartPosition {

        // chart area boundaries in pixels
        private static final double W      = 175;
        private static final double H      = 100;
        private static final double LEFT   = 28;    // left margin for y-axis labels
        private static final double BOTTOM = H - 8;
        private static final double RIGHT  = W - 6;
        private static final double TOP    = 14;    // below title text

        private Pane     pane;
        private Polyline line;      // position curve, redrawn every frame
        private Text     maxLabel;  // top y-axis value, updated each frame
        private Text     minLabel;  // bottom y-axis value, always 0

        public ChartPosition() {
            pane = new Pane();
            pane.setPrefSize(W, H);

            Rectangle bg = new Rectangle(0, 0, W, H);   // white chart background
            bg.setFill(Color.WHITE);
            bg.setStroke(Color.rgb(180, 180, 200));
            bg.setStrokeWidth(0.5);

            Text title = new Text(5, 12, "Position (m) vs Time (s)");
            title.setFill(Color.rgb(30, 50, 100));
            title.setFont(Font.font("Courier New", 10));

            Line yAxis = new Line(LEFT, TOP, LEFT, BOTTOM);     // vertical axis
            yAxis.setStroke(Color.rgb(100, 100, 120));
            yAxis.setStrokeWidth(0.8);

            Line xAxis = new Line(LEFT, BOTTOM, RIGHT, BOTTOM); // horizontal time axis
            xAxis.setStroke(Color.rgb(100, 100, 120));
            xAxis.setStrokeWidth(0.8);

            maxLabel = new Text(2, TOP + 6, "");    // top of y-axis, updated each frame
            maxLabel.setFill(Color.rgb(80, 80, 100));
            maxLabel.setFont(Font.font("Courier New", 8));

            minLabel = new Text(2, BOTTOM, "0");    // fixed at 0 — position is always positive
            minLabel.setFill(Color.rgb(80, 80, 100));
            minLabel.setFont(Font.font("Courier New", 8));

            line = new Polyline();                  // blue position curve
            line.setStroke(Color.rgb(30, 100, 220));
            line.setStrokeWidth(1.5);
            line.setFill(null);

            pane.getChildren().addAll(bg, title, yAxis, xAxis, maxLabel, minLabel, line);
        }

        public Pane getPane() { return pane; }

        // redraws the curve each frame using all stored position values
        public void update(MotionData data) {
            line.getPoints().clear();
            ArrayList<Double> yValues = data.getPositions();
            if (yValues.size() < 2) { maxLabel.setText(""); minLabel.setText("0"); return; }

            // min is always 0 — position can never be negative
            double maxVal = 0;
            for (double v : yValues) if (v > maxVal) maxVal = v;
            if (maxVal == 0) maxVal = 1; // avoid division by zero at start

            maxLabel.setText(String.format("%.1f", maxVal));
            minLabel.setText("0");

            double chartW = RIGHT - LEFT;  // usable plot width
            double chartH = BOTTOM - TOP;  // usable plot height

            // map each value to pixel coordinates and plot
            for (int i = 0; i < yValues.size(); i++) {
                double x = LEFT + (double) i / (yValues.size() - 1) * chartW;
                double y = BOTTOM - (yValues.get(i) / maxVal) * chartH;
                line.getPoints().addAll(x, y);
            }
        }
    }