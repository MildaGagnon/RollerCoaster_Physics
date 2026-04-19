

package com.example.rollercoaster_physics;

import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class SimPanel {

    private Cart      cart;
    private Track     track;
    private LevelTest level;
    private boolean   running = false;

    // bar canvas dimensions
    private static final double BAR_W = 218;
    private static final double BAR_H = 8;

    public SimPanel(Cart cart, Track track, LevelTest level) {
        this.cart  = cart;
        this.track = track;
        this.level = level;
    }

    public VBox createPanel() {

        VBox box = new VBox(10);
        box.setPadding(new Insets(16));
        box.setPrefWidth(250);
        box.setBackground(new Background(new BackgroundFill(
                Color.rgb(13, 27, 62), CornerRadii.EMPTY, Insets.EMPTY)));

        // title
        Label title = makeLabel("PARAMETERS",
                Color.rgb(147, 227, 227),
                Font.font("Courier New", FontWeight.BOLD, 13));
        box.getChildren().add(title);
        box.getChildren().add(makeLine());

        // mass slider
        Label massLabel = makeLabel(
                "Mass: " + String.format("%.1f", cart.getMass()) + " kg",
                Color.rgb(173, 216, 230), Font.font("Courier New", 12));
        Slider massSlider = new Slider(1, 20, cart.getMass());
        massSlider.setShowTickMarks(true);
        massSlider.setMajorTickUnit(5);
        massSlider.valueProperty().addListener((obs, o, n) -> {
            double m = n.doubleValue();
            massLabel.setText("Mass: " + String.format("%.1f", m) + " kg");
            cart.setMass(m);
        });
        box.getChildren().addAll(massLabel, massSlider);

        // velocity slider
        Label velLabel = makeLabel(
                "Init. velocity: " + String.format("%.1f", cart.getVelocity()) + " m/s",
                Color.rgb(173, 216, 230), Font.font("Courier New", 12));
        Slider velSlider = new Slider(0, 25, cart.getVelocity());
        velSlider.setShowTickMarks(true);
        velSlider.setMajorTickUnit(5);
        velSlider.valueProperty().addListener((obs, o, n) -> {
            double v = n.doubleValue();
            velLabel.setText("Init. velocity: " + String.format("%.1f", v) + " m/s");
            cart.setVelocity(v);
            cart.getState().velocity.x = (float) v;
        });
        box.getChildren().addAll(velLabel, velSlider);

        // track material
        Label matLabel = makeLabel("Track material:",
                Color.rgb(147, 227, 227),
                Font.font("Courier New", FontWeight.BOLD, 12));
        ToggleGroup group = new ToggleGroup();
        RadioButton metalBtn  = makeRadio("Metal   (μ = 0.05)", group);
        RadioButton woodBtn   = makeRadio("Wood    (μ = 0.40)", group);
        RadioButton rubberBtn = makeRadio("Rubber  (μ = 0.75)", group);
        metalBtn.setSelected(true);
        metalBtn .setOnAction(e -> track.setFrictionCoeff(0.05f));
        woodBtn  .setOnAction(e -> track.setFrictionCoeff(0.40f));
        rubberBtn.setOnAction(e -> track.setFrictionCoeff(0.75f));
        box.getChildren().addAll(matLabel, metalBtn, woodBtn, rubberBtn);
        box.getChildren().add(makeLine());

        // LIVE STATS
        Label statsTitle = makeLabel("LIVE STATS",
                Color.rgb(147, 227, 227),
                Font.font("Courier New", FontWeight.BOLD, 13));
        box.getChildren().add(statsTitle);

        // KE label + canvas bar
        Label keLabel = makeLabel("KE: 0.00 J",
                Color.rgb(173, 216, 230), Font.font("Courier New", 11));
        Canvas keCanvas = new Canvas(BAR_W, BAR_H);

        // PE label + canvas bar
        Label peLabel = makeLabel("PE: 0.00 J",
                Color.rgb(173, 216, 230), Font.font("Courier New", 11));
        Canvas peCanvas = new Canvas(BAR_W, BAR_H);

        // TE and acceleration labels
        Label teLabel = makeLabel("TE: 0.00 J",
                Color.rgb(173, 216, 230), Font.font("Courier New", 11));
        Label aLabel  = makeLabel("a:  0.00 m/s²",
                Color.rgb(173, 216, 230), Font.font("Courier New", 11));

        box.getChildren().addAll(keLabel, keCanvas, peLabel, peCanvas, teLabel, aLabel);
        box.getChildren().add(makeLine());

        // draw empty bars at the start
        drawBar(keCanvas, 0, Color.rgb(30, 144, 255));
        drawBar(peCanvas, 0, Color.rgb(80, 200, 120));

        // maxSeen array — tracks highest TE ever seen so bars scale correctly
        double[] maxSeen = { 1.0 };

        // AnimationTimer — runs every frame, updates labels AND redraws bars
        new AnimationTimer() {
            public void handle(long now) {
                double ke, pe, te;
                double a = cart.getState().acceleration.x;

                // read KE and PE directly from MotionData — same source as live data box
                // this guarantees left panel always matches exactly
                MotionData md = level.getMotionData();
                if (md.size() > 0) {
                    int last = md.size() - 1;
                    ke = md.getKineticEnergies().get(last);
                    pe = md.getPotentialEnergies().get(last);
                    te = ke + pe;
                } else {
                    // before simulation starts — compute from current state
                    double v = cart.getState().velocity.x;
                    double m = cart.getMass();
                    double h = getHeight();
                    ke = 0.5 * m * v * v;
                    pe = m * 9.81 * h;
                    te = ke + pe;
                }

                // update text labels
                keLabel.setText("KE: " + String.format("%.2f", ke) + " J");
                peLabel.setText("PE: " + String.format("%.2f", pe) + " J");
                teLabel.setText("TE: " + String.format("%.2f", te) + " J");
                aLabel .setText("a:  " + String.format("%.2f", a)  + " m/s²");

                // update maxSeen so bars never overflow
                if (te > maxSeen[0]) {
                    maxSeen[0] = te;
                }

                // draw bars — width proportional to value
                // when KE is 0 the bar is empty
                // when KE equals maxSeen the bar is full
                double ratio_ke = ke / maxSeen[0];
                double ratio_pe = pe / maxSeen[0];

                drawBar(keCanvas, ratio_ke, Color.rgb(30, 144, 255));
                drawBar(peCanvas, ratio_pe, Color.rgb(80, 200, 120));
            }
        }.start();

        // play / pause
        Button playBtn = makeButton("PLAY", Color.rgb(30, 144, 255));
        playBtn.setOnAction(e -> {
            if (running == false) {
                level.start();
                running = true;
                playBtn.setText("PAUSE");
            } else {
                level.stop();
                running = false;
                playBtn.setText("PLAY");
            }
        });

        // reset
        Button resetBtn = makeButton("RESET", Color.rgb(60, 80, 140));
        resetBtn.setOnAction(e -> {
            level.reset();
            running = false;
            playBtn.setText("PLAY");
            maxSeen[0] = 1.0;
            drawBar(keCanvas, 0, Color.rgb(30, 144, 255));
            drawBar(peCanvas, 0, Color.rgb(80, 200, 120));
        });

        box.getChildren().addAll(playBtn, resetBtn);
        return box;
    }

    // draws a bar on a Canvas
    // ratio = 0.0 means empty, ratio = 1.0 means completely full
    private void drawBar(Canvas canvas, double ratio, Color fillColor) {
        GraphicsContext g = canvas.getGraphicsContext2D();

        // dark background track
        g.setFill(Color.rgb(30, 50, 100));
        g.fillRoundRect(0, 0, BAR_W, BAR_H, 4, 4);

        // colored fill — clamped between 0 and full width
        double fillWidth = Math.max(0, Math.min(ratio, 1.0)) * BAR_W;
        if (fillWidth > 2) {
            g.setFill(fillColor);
            g.fillRoundRect(0, 0, fillWidth, BAR_H, 4, 4);
        }
    }

    // height of ball above the bottom of the track
    // works for both Level 1 (straight ramp) and Level 2 (parabola)
    private double getHeight() {

        // Level 2 uses slopeAngle = 0, Level 1 uses slopeAngle = 25
        if (track.getSlopeAngle() == 0) {
            // Level 2 — parabola
            double trackXStart  = 60;
            double trackXEnd    = 790;
            double trackMidX    = (trackXStart + trackXEnd) / 2.0;
            double trackBottomY = 340;
            double trackLeftY   = 90;
            double A = (trackLeftY - trackBottomY)
                    / ((trackMidX - trackXStart) * (trackMidX - trackXStart));
            double pixelsPerMeter = 2.5;

            // walk along arc to find screen x
            double pixels = cart.getState().position.x * pixelsPerMeter;
            double x = trackXStart;
            double accumulated = 0;
            double step = 0.5;
            while (x < trackXEnd) {
                double y1     = trackBottomY + A * (x - trackMidX) * (x - trackMidX);
                double y2     = trackBottomY + A * (x + step - trackMidX) * (x + step - trackMidX);
                double segLen = Math.sqrt(step * step + (y2 - y1) * (y2 - y1));
                if (accumulated + segLen >= pixels) break;
                accumulated += segLen;
                x += step;
            }
            double screenY  = trackBottomY + A * (x - trackMidX) * (x - trackMidX);
            double heightPx = trackBottomY - screenY;
            if (heightPx < 0) heightPx = 0;
            return heightPx / pixelsPerMeter;

        } else if (track.getSlopeAngle() < 0) {
            // Level 3 — loop track — constants must match Level3Test.java EXACTLY
            double loopRadius  = 90;
            double loopCenterX = 400;
            double loopCenterY = 260;
            double slopeEndX   = loopCenterX - loopRadius; // 310
            double slopeEndY   = loopCenterY + loopRadius; // 350
            double slopeStartX = 60;
            double slopeStartY = 140;
            double exitY       = slopeEndY;
            double ppm         = 35;

            double slopeLen = Math.sqrt(
                    (slopeEndX - slopeStartX) * (slopeEndX - slopeStartX) +
                            (slopeEndY - slopeStartY) * (slopeEndY - slopeStartY));
            double loopCirc = 2 * Math.PI * loopRadius;

            double px = cart.getState().position.x * ppm;
            double screenY;
            if (px <= slopeLen) {
                double t = px / slopeLen;
                screenY = slopeStartY + t * (slopeEndY - slopeStartY);
            } else {
                px -= slopeLen;
                if (px <= loopCirc) {
                    double angle = (Math.PI / 2) - (px / loopRadius);
                    screenY = loopCenterY + loopRadius * Math.sin(angle);
                } else {
                    screenY = exitY;
                }
            }
            double heightPx = exitY - screenY;
            if (heightPx < 0) heightPx = 0;
            return heightPx / ppm;

        } else {
            // Level 1 — straight ramp at 25 degrees
            double slopeRad    = Math.toRadians(25);
            double rampLengthPx = 950 - 60 - 80;
            double totalMeters  = rampLengthPx / 35.0;
            double remaining    = totalMeters - cart.getState().position.x;
            if (remaining < 0) remaining = 0;
            return remaining * Math.sin(slopeRad);
        }
    }

    private Region makeLine() {
        Region line = new Region();
        line.setPrefHeight(1);
        line.setMaxWidth(Double.MAX_VALUE);
        line.setBackground(new Background(new BackgroundFill(
                Color.rgb(40, 70, 130), CornerRadii.EMPTY, Insets.EMPTY)));
        return line;
    }

    private Label makeLabel(String text, Color color, Font font) {
        Label label = new Label(text);
        label.setTextFill(color);
        label.setFont(font);
        return label;
    }

    private RadioButton makeRadio(String text, ToggleGroup group) {
        RadioButton rb = new RadioButton(text);
        rb.setToggleGroup(group);
        rb.setTextFill(Color.rgb(173, 216, 230));
        rb.setFont(Font.font("Courier New", 12));
        return rb;
    }

    private Button makeButton(String text, Color borderColor) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
        btn.setTextFill(Color.WHITE);
        btn.setBackground(new Background(new BackgroundFill(
                Color.rgb(18, 42, 90), new CornerRadii(6), Insets.EMPTY)));
        btn.setBorder(new Border(new BorderStroke(
                borderColor, BorderStrokeStyle.SOLID,
                new CornerRadii(6), new BorderWidths(1))));
        btn.setPadding(new Insets(9, 16, 9, 16));
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setCursor(Cursor.HAND);
        return btn;
    }
}