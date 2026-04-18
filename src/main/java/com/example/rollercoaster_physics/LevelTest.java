package com.example.rollercoaster_physics;

import javafx.animation.AnimationTimer;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class LevelTest {

    // ── ramp constants ──────────────────────────────────────────
    private static final double SLOPE_DEGREES    = 25;
    private static final double SLOPE_RADIANS    = Math.toRadians(SLOPE_DEGREES);
    private static final double PIXELS_PER_METER = 35;
    private static final double BALL_RADIUS      = 18;
    private static final double PANE_W           = 950;
    private static final double PANE_H           = 580;
    private static final double RAMP_START_X     = 60;
    private static final double RAMP_START_Y     = 75;

    // ── arrow constants — scale with physics, capped so they stay small ──
    private static final double GRAVITY_BASE   = 18;
    private static final double GRAVITY_SCALE  = 0.8;
    private static final double NORMAL_BASE    = 18;
    private static final double NORMAL_SCALE   = 0.8;
    private static final double FRICTION_BASE  = 35;
    private static final double FRICTION_SCALE = 3;
    private static final double VELOCITY_BASE  = 16;
    private static final double VELOCITY_SCALE = 3.0;
    private static final double ARROW_MAX      = 80;

    // ── objects ─────────────────────────────────────────────────
    private Cart          cart;
    private Track         track;
    private PhysicsEngine physics;
    private MotionData    motionData;

    // ── pane ────────────────────────────────────────────────────
    private Pane pane;

    // ── ball ────────────────────────────────────────────────────
    private Circle ball;
    private Circle ballShine;

    // ── force arrow lines ────────────────────────────────────────
    private Line velocityLine;
    private Line gravityLine;
    private Line normalLine;
    private Line frictionLine;

    // ── arrowheads ───────────────────────────────────────────────
    private Polygon velocityHead;
    private Polygon gravityHead;
    private Polygon normalHead;
    private Polygon frictionHead;

    // ── arrow labels ─────────────────────────────────────────────
    private Text velocityLabel;
    private Text gravityLabel;
    private Text normalLabel;
    private Text frictionLabel;

    // ── info box ─────────────────────────────────────────────────
    private Rectangle infoBox;
    private Text      infoTitle;
    private Text      keText;
    private Text      peText;
    private Text      teText;
    private Text      vText;
    private Text      tText;

    // ── finish line ──────────────────────────────────────────────
    private Line finishLine;
    private Text finishLabel;

    // ── charts ───────────────────────────────────────────────────
    private ChartPosition     chartPos;
    private ChartVelocity     chartVel;
    private ChartAcceleration chartAcc;
    private ChartEnergy       chartEnergy;

    // ── timer ────────────────────────────────────────────────────
    private AnimationTimer timer;
    private long   lastTime;
    private double simTime;

    // ── ramp geometry ────────────────────────────────────────────
    private double rampEndX;
    private double rampEndY;
    private double rampLengthPx;

    // ── constructor ──────────────────────────────────────────────
    public LevelTest(Cart cart, Track track, ChartPosition chartPosition, ChartVelocity chartVelocity, ChartAcceleration chartAcceleration, ChartEnergy chartEnergy) {
        this.cart  = cart;
        this.track = track;

        Level1.setup(track);

        physics    = new PhysicsEngine(cart, track);
        motionData = new MotionData();

        rampLengthPx = PANE_W - RAMP_START_X - 80;
        rampEndX = RAMP_START_X + rampLengthPx * Math.cos(SLOPE_RADIANS);
        rampEndY = RAMP_START_Y + rampLengthPx * Math.sin(SLOPE_RADIANS);

        simTime  = 0;
        lastTime = 0;

        buildPane();

        timer = new AnimationTimer() {
            public void handle(long now) {
                if (lastTime == 0) { lastTime = now; return; }

                float dt = (float)((now - lastTime) / 1_000_000_000.0);
                if (dt > 0.04f) { dt = 0.04f; }
                lastTime = now;

                physics.update(dt);
                simTime = simTime + dt;

                double maxPos = rampLengthPx / PIXELS_PER_METER;
                if (cart.getState().position.x >= maxPos) {
                    cart.getState().position.x     = (float) maxPos;
                    cart.getState().velocity.x     = 0;
                    cart.getState().acceleration.x = 0;
                    stop();
                }

                double v   = cart.getState().velocity.x;
                double a   = cart.getState().acceleration.x;
                double pos = cart.getState().position.x;
                double h   = getHeight();
                double ke  = 0.5 * cart.getMass() * v * v;
                double pe  = cart.getMass() * 9.81 * h;
                motionData.addPoint(simTime, pos, v, a, ke, pe);

                chartPos.update(motionData);
                chartVel.update(motionData);
                chartAcc.update(motionData);
                LevelTest.this.chartEnergy.update(motionData);

                update();
            }
        };
    }

    // ── builds all shapes ────────────────────────────────────────
    private void buildPane() {

        pane = new Pane();
        pane.setPrefSize(PANE_W, PANE_H);

        // background
        Rectangle sky = new Rectangle(0, 0, PANE_W, PANE_H);
        sky.setFill(Color.rgb(228, 238, 255));

        // hill
        Polygon hill = new Polygon(
                RAMP_START_X, RAMP_START_Y,
                rampEndX,     rampEndY,
                rampEndX,     PANE_H - 15,
                RAMP_START_X, PANE_H - 15
        );
        hill.setFill(Color.rgb(150, 185, 215));
        hill.setStroke(null);

        // ramp
        Line ramp = new Line(RAMP_START_X, RAMP_START_Y, rampEndX, rampEndY);
        ramp.setStroke(Color.rgb(40, 70, 155));
        ramp.setStrokeWidth(5);

        Line platform = new Line(RAMP_START_X - 50, RAMP_START_Y, RAMP_START_X, RAMP_START_Y);
        platform.setStroke(Color.rgb(40, 70, 155));
        platform.setStrokeWidth(4);

        Text angleLabel = new Text(rampEndX - 40, rampEndY - 4, SLOPE_DEGREES + "°");
        angleLabel.setFill(Color.rgb(55, 55, 65));
        angleLabel.setFont(Font.font("Courier New", 11));

        Line angleLine = new Line(rampEndX - 60, rampEndY, rampEndX + 5, rampEndY);
        angleLine.setStroke(Color.rgb(80, 80, 90));
        angleLine.setStrokeWidth(1);

        finishLine = new Line(rampEndX, rampEndY - 30, rampEndX, rampEndY + 20);
        finishLine.setStroke(Color.rgb(200, 50, 50));
        finishLine.setStrokeWidth(2);
        finishLine.getStrokeDashArray().addAll(8.0, 4.0);

        finishLabel = new Text(rampEndX - 20, rampEndY - 36, "FINISH");
        finishLabel.setFill(Color.rgb(200, 50, 50));
        finishLabel.setFont(Font.font("Courier New", 10));

        // ball
        ball = new Circle(BALL_RADIUS);
        ball.setFill(Color.rgb(30, 144, 255));
        ball.setStroke(Color.rgb(15, 90, 180));
        ball.setStrokeWidth(2);

        ballShine = new Circle(BALL_RADIUS * 0.25);
        ballShine.setFill(Color.rgb(200, 230, 255));
        ballShine.setOpacity(0.7);

        // arrow lines
        velocityLine = new Line();
        velocityLine.setStroke(Color.rgb(0, 200, 80));
        velocityLine.setStrokeWidth(2.5);

        gravityLine = new Line();
        gravityLine.setStroke(Color.rgb(220, 50, 50));
        gravityLine.setStrokeWidth(2.5);

        normalLine = new Line();
        normalLine.setStroke(Color.rgb(255, 200, 0));
        normalLine.setStrokeWidth(2.5);

        frictionLine = new Line();
        frictionLine.setStroke(Color.rgb(255, 140, 0));
        frictionLine.setStrokeWidth(2.5);

        // arrowheads
        velocityHead = new Polygon(); velocityHead.setFill(Color.rgb(0, 200, 80));
        gravityHead  = new Polygon(); gravityHead .setFill(Color.rgb(220, 50, 50));
        normalHead   = new Polygon(); normalHead  .setFill(Color.rgb(255, 200, 0));
        frictionHead = new Polygon(); frictionHead.setFill(Color.rgb(255, 140, 0));

        // arrow labels
        velocityLabel = new Text();
        velocityLabel.setFill(Color.rgb(0, 200, 80));
        velocityLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 10));

        gravityLabel = new Text("Fg");
        gravityLabel.setFill(Color.rgb(220, 50, 50));
        gravityLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 10));

        normalLabel = new Text("N");
        normalLabel.setFill(Color.rgb(255, 200, 0));
        normalLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 10));

        frictionLabel = new Text("Ff");
        frictionLabel.setFill(Color.rgb(255, 140, 0));
        frictionLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 10));

        // info box — top left, small
        infoBox = new Rectangle(10, 465, 195, 105);
        infoBox.setFill(Color.rgb(10, 22, 55));
        infoBox.setStroke(Color.rgb(30, 144, 255));
        infoBox.setStrokeWidth(1);

        infoTitle = new Text(18, 484, "LIVE PHYSICS — LEVEL 1");
        infoTitle.setFill(Color.rgb(147, 227, 227));
        infoTitle.setFont(Font.font("Courier New", 10));

        keText = new Text(18, 500, "KE = 0.00 J");
        peText = new Text(18, 514, "PE = 0.00 J");
        teText = new Text(18, 528, "TE = 0.00 J");
        vText  = new Text(18, 542, "v  = 0.00 m/s");
        tText  = new Text(18, 556, "t  = 0.00 s");

        for (Text tx : new Text[]{ keText, peText, teText, vText, tText }) {
            tx.setFill(Color.rgb(173, 216, 230));
            tx.setFont(Font.font("Courier New", 10));
        }

        // ── chart card — small 2x2 grid, anchored to very bottom ────
        // card starts at y=450 so it sits at the absolute bottom of the 580px pane
        double cardX = 290;
        double cardY = 455;
        double cardW = 370;
        double cardH = 225;

        Rectangle cardBg = new Rectangle(cardX, cardY, cardW, cardH);
        cardBg.setFill(Color.rgb(10, 22, 55));
        cardBg.setStroke(Color.rgb(30, 80, 160));
        cardBg.setStrokeWidth(1);
        cardBg.setArcWidth(8);
        cardBg.setArcHeight(8);

        Text chartTitle = new Text(cardX + 10, cardY + 11,
                "LIVE GRAPHS  —  Position · Velocity · Acceleration · Energy");
        chartTitle.setFill(Color.rgb(147, 227, 227));
        chartTitle.setFont(Font.font("Courier New", FontWeight.BOLD, 9));

        chartPos    = new ChartPosition();
        chartVel    = new ChartVelocity();
        chartAcc    = new ChartAcceleration();
        chartEnergy = new ChartEnergy();

        // 2 x 2 grid inside the card
        double cX1 = cardX + 6;
        double cX2 = cardX + 184;
        double cY1 = cardY + 14;
        double cY2 = cardY + 118;

        chartPos.getPane().setLayoutX(cX1);
        chartPos.getPane().setLayoutY(cY1);

        chartVel.getPane().setLayoutX(cX2);
        chartVel.getPane().setLayoutY(cY1);

        chartAcc.getPane().setLayoutX(cX1);
        chartAcc.getPane().setLayoutY(cY2);

        chartEnergy.getPane().setLayoutX(cX2);
        chartEnergy.getPane().setLayoutY(cY2);

        pane.getChildren().addAll(
                sky, hill, ramp, platform,
                angleLine, angleLabel,
                finishLine, finishLabel,
                velocityLine, gravityLine, normalLine, frictionLine,
                velocityHead, gravityHead, normalHead, frictionHead,
                velocityLabel, gravityLabel, normalLabel, frictionLabel,
                ball, ballShine,
                infoBox, infoTitle,
                keText, peText, teText, vText, tText,
                cardBg, chartTitle,
                chartPos.getPane(), chartVel.getPane(),
                chartAcc.getPane(), chartEnergy.getPane()
        );

        update();
    }

    // ── updates all shapes every frame ───────────────────────────
    private void update() {

        double distancePx = cart.getState().position.x * PIXELS_PER_METER;
        double rampX = RAMP_START_X + distancePx * Math.cos(SLOPE_RADIANS);
        double rampY = RAMP_START_Y + distancePx * Math.sin(SLOPE_RADIANS);

        double offsetX = -Math.sin(SLOPE_RADIANS);
        double offsetY =  Math.cos(SLOPE_RADIANS);
        double ballX   = rampX - offsetX * BALL_RADIUS;
        double ballY   = rampY - offsetY * BALL_RADIUS;

        ball.setCenterX(ballX);
        ball.setCenterY(ballY);
        ballShine.setCenterX(ballX - BALL_RADIUS * 0.3);
        ballShine.setCenterY(ballY - BALL_RADIUS * 0.4);

        double v    = cart.getState().velocity.x;
        double mass = cart.getMass();

        double Fg = mass * 9.81;
        double N  = mass * 9.81 * Math.cos(SLOPE_RADIANS);
        double ff = track.getFrictionCoeff() * N;

        // velocity arrow
        if (Math.abs(v) > 0.05) {
            double len = Math.min(VELOCITY_BASE + Math.abs(v) * VELOCITY_SCALE, ARROW_MAX);
            setArrow(velocityLine, velocityHead, velocityLabel,
                    ballX, ballY,
                    ballX + len * Math.cos(SLOPE_RADIANS),
                    ballY + len * Math.sin(SLOPE_RADIANS));
            velocityLabel.setText("v=" + String.format("%.1f", v) + "m/s");
            velocityLine.setVisible(true);
            velocityHead.setVisible(true);
            velocityLabel.setVisible(true);
        } else {
            velocityLine.setVisible(false);
            velocityHead.setVisible(false);
            velocityLabel.setVisible(false);
        }

        // gravity arrow
        double fgLen = Math.min(GRAVITY_BASE + Fg * GRAVITY_SCALE, ARROW_MAX);
        setArrow(gravityLine, gravityHead, gravityLabel,
                ballX, ballY, ballX, ballY + fgLen);

        // normal arrow
        double nLen = Math.min(NORMAL_BASE + N * NORMAL_SCALE, ARROW_MAX);
        setArrow(normalLine, normalHead, normalLabel,
                ballX, ballY,
                ballX - offsetX * nLen,
                ballY - offsetY * nLen);

        // friction arrow
        if (Math.abs(v) > 0.05) {
            double ffLen = Math.min(FRICTION_BASE + ff * FRICTION_SCALE, ARROW_MAX);
            setArrow(frictionLine, frictionHead, frictionLabel,
                    ballX, ballY,
                    ballX - ffLen * Math.cos(SLOPE_RADIANS),
                    ballY - ffLen * Math.sin(SLOPE_RADIANS));
            frictionLine.setVisible(true);
            frictionHead.setVisible(true);
            frictionLabel.setVisible(true);
        } else {
            frictionLine.setVisible(false);
            frictionHead.setVisible(false);
            frictionLabel.setVisible(false);
        }

        // info box
        double h  = getHeight();
        double ke = 0.5 * mass * v * v;
        double pe = mass * 9.81 * h;
        double te = ke + pe;

        keText.setText("KE = " + String.format("%.2f", ke) + " J");
        peText.setText("PE = " + String.format("%.2f", pe) + " J");
        teText.setText("TE = " + String.format("%.2f", te) + " J");
        vText .setText("v  = " + String.format("%.2f", v)  + " m/s");
        tText .setText("t  = " + String.format("%.2f", simTime) + " s");
    }

    private void setArrow(Line line, Polygon head, Text label,
                          double x1, double y1, double x2, double y2) {
        line.setStartX(x1); line.setStartY(y1);
        line.setEndX(x2);   line.setEndY(y2);

        double angle  = Math.atan2(y2 - y1, x2 - x1);
        double size   = 10;
        double spread = Math.toRadians(25);

        head.getPoints().setAll(
                x2, y2,
                x2 - size * Math.cos(angle - spread),
                y2 - size * Math.sin(angle - spread),
                x2 - size * Math.cos(angle + spread),
                y2 - size * Math.sin(angle + spread)
        );

        label.setX(x2 + 4);
        label.setY(y2 - 2);
    }

    private double getHeight() {
        double totalMeters = rampLengthPx / PIXELS_PER_METER;
        double remaining   = totalMeters - cart.getState().position.x;
        if (remaining < 0) { remaining = 0; }
        return remaining * Math.sin(SLOPE_RADIANS);
    }

    public void start() {
        lastTime = 0;
        timer.start();
    }

    public void stop() {
        timer.stop();
        lastTime = 0;
    }

    public void reset() {
        stop();
        cart.getState().position.x     = 0;
        cart.getState().velocity.x     = (float) cart.getVelocity();
        cart.getState().acceleration.x = 0;
        simTime = 0;
        motionData.clear();
        chartPos.update(motionData);
        chartVel.update(motionData);
        chartAcc.update(motionData);
        chartEnergy.update(motionData);
        update();
    }

    public Pane getView() {
        return pane;
    }
    public MotionData getMotionData() {
        return motionData;
    }
}