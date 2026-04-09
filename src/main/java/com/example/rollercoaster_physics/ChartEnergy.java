package com.example.rollercoaster_physics;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class ChartEnergy {

    private static final double W = 390;
    private static final double H = 150;

    private static final double LEFT   = 42;
    private static final double BOTTOM = H - 22;
    private static final double RIGHT  = W - 8;
    private static final double TOP    = 20;

    private Pane     pane;
    private Polyline keLine;
    private Polyline peLine;
    private Text maxLabel;

    public ChartEnergy() {

        pane = new Pane();
        pane.setPrefSize(W, H);

        Rectangle bg = new Rectangle(0, 0, W, H);
        bg.setFill(Color.WHITE);
        bg.setStroke(Color.rgb(180, 180, 200));
        bg.setStrokeWidth(1);

        Text title = new Text(6, 13, "Energy (J) vs Time (s)");
        title.setFill(Color.rgb(30, 50, 100));
        title.setFont(Font.font("Courier New", 11));

        // legend
        Rectangle keBox = new Rectangle(W - 70, 4, 10, 8);
        keBox.setFill(Color.rgb(30, 100, 220));
        Text keTag = new Text(W - 56, 12, "KE");
        keTag.setFill(Color.rgb(30, 50, 100));
        keTag.setFont(Font.font("Courier New", 9));

        Rectangle peBox = new Rectangle(W - 32, 4, 10, 8);
        peBox.setFill(Color.rgb(0, 160, 60));
        Text peTag = new Text(W - 18, 12, "PE");
        peTag.setFill(Color.rgb(30, 50, 100));
        peTag.setFont(Font.font("Courier New", 9));

        Line yAxis = new Line(LEFT, TOP, LEFT, BOTTOM);
        yAxis.setStroke(Color.rgb(100, 100, 120));

        Line xAxis = new Line(LEFT, BOTTOM, RIGHT, BOTTOM);
        xAxis.setStroke(Color.rgb(100, 100, 120));

        maxLabel = new Text(2, TOP + 6, "");
        maxLabel.setFill(Color.rgb(80, 80, 100));
        maxLabel.setFont(Font.font("Courier New", 9));

        Text minLabel = new Text(2, BOTTOM, "0");
        minLabel.setFill(Color.rgb(80, 80, 100));
        minLabel.setFont(Font.font("Courier New", 9));

        keLine = new Polyline();
        keLine.setStroke(Color.rgb(30, 100, 220));
        keLine.setStrokeWidth(2);
        keLine.setFill(null);

        peLine = new Polyline();
        peLine.setStroke(Color.rgb(0, 160, 60));
        peLine.setStrokeWidth(2);
        peLine.setFill(null);

        pane.getChildren().addAll(bg, title,
                keBox, keTag, peBox, peTag,
                yAxis, xAxis,
                maxLabel, minLabel,
                keLine, peLine);
    }

    public Pane getPane() {
        return pane;
    }

    public void update(MotionData data) {

        keLine.getPoints().clear();
        peLine.getPoints().clear();

        if (data.size() < 2) {
            maxLabel.setText("");
            return;
        }

        // find the max across both KE and PE to use the same scale
        double maxVal = 1;
        for (int i = 0; i < data.size(); i++) {
            if (data.getKineticEnergies().get(i)   > maxVal) { maxVal = data.getKineticEnergies().get(i); }
            if (data.getPotentialEnergies().get(i)  > maxVal) { maxVal = data.getPotentialEnergies().get(i); }
        }

        double chartW = RIGHT  - LEFT;
        double chartH = BOTTOM - TOP;

        maxLabel.setText(String.format("%.1f", maxVal));

        for (int i = 0; i < data.size(); i++) {
            double x  = LEFT + (double) i / (data.size() - 1) * chartW;
            double yke = BOTTOM - (data.getKineticEnergies().get(i)   / maxVal) * chartH;
            double ype = BOTTOM - (data.getPotentialEnergies().get(i)  / maxVal) * chartH;
            keLine.getPoints().addAll(x, yke);
            peLine.getPoints().addAll(x, ype);
        }
    }
}
