

package com.example.rollercoaster_physics;

import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class SimPanel {

    private Cart cart;
    private Track track;
    private LevelTest level;

    private boolean running = false;

    // constructor
    public SimPanel(Cart cart, Track track, LevelTest level) {
        this.cart = cart;
        this.track = track;
        this.level = level;
    }

    // builds and returns the left side panel
    public VBox createPanel() {

        VBox box = new VBox(10);
        box.setPadding(new Insets(16));
        box.setPrefWidth(250);
        box.setBackground(new Background(new BackgroundFill(
                Color.rgb(13, 27, 62), CornerRadii.EMPTY, Insets.EMPTY)));

        Label title = makeLabel("PARAMETERS",
                Color.rgb(147, 227, 227),
                Font.font("Courier New", FontWeight.BOLD, 13));
        box.getChildren().add(title);
        box.getChildren().add(makeLine());

        // mass slider
        Label massLabel = makeLabel(
                "Mass: " + String.format("%.1f", cart.getMass()) + " kg",
                Color.rgb(173, 216, 230),
                Font.font("Courier New", 12));

        Slider massSlider = new Slider(1, 20, cart.getMass());
        massSlider.setShowTickMarks(true);
        massSlider.setMajorTickUnit(5);

        massSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double m = newVal.doubleValue();
            massLabel.setText("Mass: " + String.format("%.1f", m) + " kg");
            cart.setMass(m);
        });

        box.getChildren().addAll(massLabel, massSlider);

        // initial velocity slider
        Label velLabel = makeLabel(
                "Init. velocity: " + String.format("%.1f", cart.getVelocity()) + " m/s",
                Color.rgb(173, 216, 230),
                Font.font("Courier New", 12));

        Slider velSlider = new Slider(0, 25, cart.getVelocity());
        velSlider.setShowTickMarks(true);
        velSlider.setMajorTickUnit(5);

        velSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double v = newVal.doubleValue();
            velLabel.setText("Init. velocity: " + String.format("%.1f", v) + " m/s");
            cart.setVelocity(v);
            cart.getState().velocity.x = (float) v;
        });

        box.getChildren().addAll(velLabel, velSlider);

        // track material radio buttons
        Label matLabel = makeLabel("Track material:",
                Color.rgb(147, 227, 227),
                Font.font("Courier New", FontWeight.BOLD, 12));

        ToggleGroup group = new ToggleGroup();

        RadioButton smoothBtn = makeRadio("Smooth  (mu = 0.10)", group);
        RadioButton woodBtn = makeRadio("Wood    (mu = 0.30)", group);
        RadioButton roughBtn = makeRadio("Rough   (mu = 0.55)", group);
        smoothBtn.setSelected(true);

        smoothBtn.setOnAction(e -> track.setFrictionCoeff(0.10f));
        woodBtn.setOnAction(e -> track.setFrictionCoeff(0.30f));
        roughBtn.setOnAction(e -> track.setFrictionCoeff(0.55f));

        box.getChildren().addAll(matLabel, smoothBtn, woodBtn, roughBtn);
        box.getChildren().add(makeLine());

        // play / pause button
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

        // reset button
        Button resetBtn = makeButton("RESET", Color.rgb(60, 80, 140));

        resetBtn.setOnAction(e -> {
            level.reset();
            running = false;
            playBtn.setText("PLAY");
        });

        box.getChildren().addAll(playBtn, resetBtn);

        return box;
    }

    // thin horizontal line used as a divider
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