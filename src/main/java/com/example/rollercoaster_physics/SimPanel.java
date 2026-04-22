

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

// left control panel — sliders, material buttons, live energy bars, PLAY/RESET
public class SimPanel {

    private Cart      cart;
    private Track     track;
    private LevelTest level;   // accepts LevelTestAdapter2/3 for Levels 2 and 3
    private boolean   running = false;

    private static final double BAR_W = 218; // energy bar width in pixels
    private static final double BAR_H = 8;   // energy bar height in pixels

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

        Label title = makeLabel("PARAMETERS",
                Color.rgb(147, 227, 227), Font.font("Courier New", FontWeight.BOLD, 13));
        box.getChildren().add(title);
        box.getChildren().add(makeLine());

        // mass slider — range 1–20 kg
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

        // velocity slider — range 0–25 m/s
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
            // Level 2 stores velocity as px/s; Levels 1 and 3 use m/s
            if (track.getSlopeAngle() == 0) {
                cart.getState().velocity.x = (float)(v * 2.5); // convert m/s to px/s for Level 2
            } else {
                cart.getState().velocity.x = (float) v;
            }
        });
        box.getChildren().addAll(velLabel, velSlider);

        // material radio buttons — each sets a different friction coefficient
        Label matLabel = makeLabel("Track material:",
                Color.rgb(147, 227, 227), Font.font("Courier New", FontWeight.BOLD, 12));
        ToggleGroup group = new ToggleGroup();
        RadioButton metalBtn  = makeRadio("Metal   (μ = 0.05)", group);
        RadioButton woodBtn   = makeRadio("Wood    (μ = 0.40)", group);
        RadioButton rubberBtn = makeRadio("Rubber  (μ = 0.75)", group);
        metalBtn.setSelected(true); // default
        metalBtn .setOnAction(e -> track.setFrictionCoeff(0.05f));
        woodBtn  .setOnAction(e -> track.setFrictionCoeff(0.40f));
        rubberBtn.setOnAction(e -> track.setFrictionCoeff(0.75f));
        box.getChildren().addAll(matLabel, metalBtn, woodBtn, rubberBtn);
        box.getChildren().add(makeLine());

        Label statsTitle = makeLabel("LIVE STATS",
                Color.rgb(147, 227, 227), Font.font("Courier New", FontWeight.BOLD, 13));
        box.getChildren().add(statsTitle);

        Label keLabel = makeLabel("KE: 0.00 J", Color.rgb(173, 216, 230), Font.font("Courier New", 11));
        Canvas keCanvas = new Canvas(BAR_W, BAR_H); // blue KE bar

        Label peLabel = makeLabel("PE: 0.00 J", Color.rgb(173, 216, 230), Font.font("Courier New", 11));
        Canvas peCanvas = new Canvas(BAR_W, BAR_H); // green PE bar

        Label teLabel = makeLabel("TE: 0.00 J",    Color.rgb(173, 216, 230), Font.font("Courier New", 11));
        Label aLabel  = makeLabel("a:  0.00 m/s²", Color.rgb(173, 216, 230), Font.font("Courier New", 11));

        box.getChildren().addAll(keLabel, keCanvas, peLabel, peCanvas, teLabel, aLabel);
        box.getChildren().add(makeLine());

        drawBar(keCanvas, 0, Color.rgb(30, 144, 255)); // empty bars on start
        drawBar(peCanvas, 0, Color.rgb(80, 200, 120));

        double[] maxSeen = { 1.0 }; // tracks peak TE so bars scale correctly — array so lambda can write it

        new AnimationTimer() {
            public void handle(long now) {
                double ke, pe, te;
                double a = cart.getState().acceleration.x;

                MotionData md = level.getMotionData();
                if (md.size() > 0) {
                    int last = md.size() - 1; // most recent frame
                    ke = md.getKineticEnergies().get(last);
                    pe = md.getPotentialEnergies().get(last);
                    te = ke + pe;
                } else {
                    // simulation not started yet — compute from current state
                    double v = cart.getState().velocity.x;
                    // Level 2 stores velocity as px/s — convert back to m/s for KE
                    double vMs = (track.getSlopeAngle() == 0) ? v / 2.5 : v;
                    double m = cart.getMass();
                    double h = getHeight();
                    ke = 0.5 * m * vMs * vMs; // KE = ½mv²
                    pe = m * 9.81 * h;    // PE = mgh
                    te = ke + pe;
                }

                keLabel.setText("KE: " + String.format("%.2f", ke) + " J");
                peLabel.setText("PE: " + String.format("%.2f", pe) + " J");
                teLabel.setText("TE: " + String.format("%.2f", te) + " J");
                aLabel .setText("a:  " + String.format("%.2f", a)  + " m/s²");

                if (te > maxSeen[0]) maxSeen[0] = te; // grow max, never shrink

                drawBar(keCanvas, ke / maxSeen[0], Color.rgb(30, 144, 255));
                drawBar(peCanvas, pe / maxSeen[0], Color.rgb(80, 200, 120));
            }
        }.start();

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

        Button resetBtn = makeButton("RESET", Color.rgb(60, 80, 140));
        resetBtn.setOnAction(e -> {
            level.reset();
            running = false;
            playBtn.setText("PLAY");
            maxSeen[0] = 1.0; // reset bar scale
            drawBar(keCanvas, 0, Color.rgb(30, 144, 255));
            drawBar(peCanvas, 0, Color.rgb(80, 200, 120));
        });

        box.getChildren().addAll(playBtn, resetBtn);
        return box;
    }

    // draws a rounded bar — ratio 0.0 = empty, 1.0 = full
    private void drawBar(Canvas canvas, double ratio, Color fillColor) {
        GraphicsContext g = canvas.getGraphicsContext2D();

        g.setFill(Color.rgb(30, 50, 100)); // dark background track
        g.fillRoundRect(0, 0, BAR_W, BAR_H, 4, 4);

        double fillWidth = Math.max(0, Math.min(ratio, 1.0)) * BAR_W; // clamp 0–BAR_W
        if (fillWidth > 2) {
            g.setFill(fillColor);
            g.fillRoundRect(0, 0, fillWidth, BAR_H, 4, 4);
        }
    }

    // returns ball height above track bottom — branches on level type via slopeAngle
    private double getHeight() {

        if (track.getSlopeAngle() == 0) {
            // Level 2 — parabola: walk arc to find screen y, measure from valley bottom
            double trackXStart  = 60;
            double trackXEnd    = 790;
            double trackMidX    = (trackXStart + trackXEnd) / 2.0;
            double trackBottomY = 340;
            double trackLeftY   = 90;
            double A = (trackLeftY - trackBottomY)
                    / ((trackMidX - trackXStart) * (trackMidX - trackXStart));
            double pixelsPerMeter = 2.5;

            // position.x is stored as screen X for Level 2 — use it directly
            double x      = Math.min(Math.max(cart.getState().position.x, trackXStart), trackXEnd);
            double screenY = trackBottomY + A * (x - trackMidX) * (x - trackMidX);
            double heightPx = trackBottomY - screenY; // positive above valley
            if (heightPx < 0) heightPx = 0;
            return heightPx / pixelsPerMeter;

        } else if (track.getSlopeAngle() < 0) {
            // Level 3 — loop: compute screen y across 3 segments, measure from EXIT_Y
            double loopRadius  = 90;
            double loopCenterX = 400;
            double loopCenterY = 260;
            double slopeEndX   = loopCenterX - loopRadius; // 310
            double slopeEndY   = loopCenterY + loopRadius; // 350
            double slopeStartX = 60;
            double slopeStartY = 140;
            double exitY       = slopeEndY; // ground level = 350
            double ppm         = 35;

            double slopeLen = Math.sqrt(
                    (slopeEndX - slopeStartX) * (slopeEndX - slopeStartX) +
                            (slopeEndY - slopeStartY) * (slopeEndY - slopeStartY)); // Pythagoras
            double loopCirc = 2 * Math.PI * loopRadius;

            double px = cart.getState().position.x * ppm;
            double screenY;
            if (px <= slopeLen) {
                double t = px / slopeLen; // 0=top, 1=bottom
                screenY = slopeStartY + t * (slopeEndY - slopeStartY);
            } else {
                px -= slopeLen;
                if (px <= loopCirc) {
                    double angle = (Math.PI / 2) - (px / loopRadius); // starts at bottom
                    screenY = loopCenterY + loopRadius * Math.sin(angle);
                } else {
                    screenY = exitY; // flat exit
                }
            }
            double heightPx = exitY - screenY; // positive above ground
            if (heightPx < 0) heightPx = 0;
            return heightPx / ppm;

        } else {
            // Level 1 — straight ramp: height = remaining distance * sin(angle)
            double slopeRad     = Math.toRadians(25);
            double rampLengthPx = 950 - 60 - 80;
            double totalMeters  = rampLengthPx / 35.0;
            double remaining    = totalMeters - cart.getState().position.x;
            if (remaining < 0) remaining = 0;
            return remaining * Math.sin(slopeRad); // vertical component
        }
    }

    // 1px horizontal divider line
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