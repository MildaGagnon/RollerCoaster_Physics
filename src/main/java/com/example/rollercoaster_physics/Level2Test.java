package com.example.rollercoaster_physics;

import javafx.animation.AnimationTimer;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class Level2Test {


    private static final double PANE_W = 950;
    private static final double PANE_H = 580;

    // ── parabola track — centered in 950px pane ──────────────────
    private static final double TRACK_X_START  = 60;
    private static final double TRACK_X_END    = 790;
    private static final double TRACK_MID_X    = (TRACK_X_START + TRACK_X_END) / 2.0;
    private static final double TRACK_BOTTOM_Y = 340;
    private static final double TRACK_LEFT_Y   = 90;
    private static final double A = (TRACK_LEFT_Y - TRACK_BOTTOM_Y)
            / ((TRACK_MID_X - TRACK_X_START) * (TRACK_MID_X - TRACK_X_START));
    private static final int    TRACK_POINTS   = 120;

    private static final double PIXELS_PER_METER = 2.5;
    private static final double BALL_RADIUS      = 16;

    // ── arrow constants ──────────────────────────────────────────
    private static final double GRAVITY_BASE   = 18;
    private static final double GRAVITY_SCALE  = 0.8;
    private static final double NORMAL_BASE    = 18;
    private static final double NORMAL_SCALE   = 0.8;
    private static final double FRICTION_BASE  = 35;
    private static final double FRICTION_SCALE = 3.0;
    private static final double VELOCITY_BASE  = 16;
    private static final double VELOCITY_SCALE = 3.0;
    private static final double ARROW_MAX      = 55;

    // ── objects ──────────────────────────────────────────────────
    private Cart          cart;
    private Track         track;
    private PhysicsEngine physics;
    private MotionData    motionData;
    private Pane          pane;

    private Circle  ball;
    private Circle  ballShine;

    private Line    velocityLine, gravityLine, normalLine, frictionLine;
    private Polygon velocityHead, gravityHead, normalHead, frictionHead;
    private Text    velocityLabel, gravityLabel, normalLabel, frictionLabel;

    private Rectangle infoBox;
    private Text      infoTitle, keText, peText, teText, vText, tText;

    private Line finishLine;
    private Text finishLabel;

    // charts passed in from RollerCoasterUI — shared, never created here
    private ChartPosition     chartPos;
    private ChartVelocity     chartVel;
    private ChartAcceleration chartAcc;
    private ChartEnergy       chartEnergy;

    private AnimationTimer timer;
    private long   lastTime;
    private double simTime;
    private double trackLengthMeters;

    // constructor — receives charts from outside
    public Level2Test(Cart cart, Track track,
                      ChartPosition chartPos, ChartVelocity chartVel,
                      ChartAcceleration chartAcc, ChartEnergy chartEnergy) {
        this.cart        = cart;
        this.track       = track;
        this.chartPos    = chartPos;
        this.chartVel    = chartVel;
        this.chartAcc    = chartAcc;
        this.chartEnergy = chartEnergy;

        Level2.setup(track);

        physics    = new PhysicsEngine(cart, track);
        motionData = new MotionData();

        trackLengthMeters = arcLengthPixels() / PIXELS_PER_METER;

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

                if (cart.getState().position.x >= trackLengthMeters) {
                    // ball reached finish — stop
                    cart.getState().position.x     = (float) trackLengthMeters;
                    cart.getState().velocity.x     = 0;
                    cart.getState().acceleration.x = 0;
                    stop();
                }
                // negative velocity = rolling back down — PhysicsEngine handles it

                double v   = cart.getState().velocity.x;
                double a   = cart.getState().acceleration.x;
                double pos = cart.getState().position.x;
                double h   = getHeight();
                double ke  = 0.5 * cart.getMass() * v * v;
                double pe  = cart.getMass() * 9.81 * h;
                motionData.addPoint(simTime, pos, v, a, ke, pe);

                // update the shared charts
                chartPos.update(motionData);
                chartVel.update(motionData);
                chartAcc.update(motionData);
                chartEnergy.update(motionData);

                update();
            }
        };
    }

    // ── parabola y at screen x ───────────────────────────────────
    private double trackY(double screenX) {
        double dx = screenX - TRACK_MID_X;
        return TRACK_BOTTOM_Y + A * dx * dx;
    }

    // ── slope angle at screen x ──────────────────────────────────
    private double trackAngle(double screenX) {
        double dy_dx = 2.0 * A * (screenX - TRACK_MID_X);
        return Math.atan2(dy_dx, 1.0);
    }

    // ── convert arc distance (metres) to screen x ────────────────
    private double arcDistToScreenX(double meters) {
        double pixels = meters * PIXELS_PER_METER;
        double x = TRACK_X_START;
        double accumulated = 0;
        double step = 0.5;
        while (x < TRACK_X_END) {
            double y1 = trackY(x);
            double y2 = trackY(x + step);
            double segLen = Math.sqrt(step * step + (y2 - y1) * (y2 - y1));
            if (accumulated + segLen >= pixels) return x;
            accumulated += segLen;
            x += step;
        }
        return TRACK_X_END;
    }

    // ── total arc length in pixels ───────────────────────────────
    private double arcLengthPixels() {
        double total = 0;
        double step  = 0.5;
        for (double x = TRACK_X_START; x < TRACK_X_END; x += step) {
            double y1 = trackY(x);
            double y2 = trackY(x + step);
            total += Math.sqrt(step * step + (y2 - y1) * (y2 - y1));
        }
        return total;
    }

    // ── height above the bottom of the parabola ──────────────────
    private double getHeight() {
        double screenX = arcDistToScreenX(cart.getState().position.x);
        double screenY = trackY(screenX);
        double heightPx = TRACK_BOTTOM_Y - screenY;
        if (heightPx < 0) heightPx = 0;
        return heightPx / PIXELS_PER_METER;
    }

    private void buildPane() {

        pane = new Pane();
        pane.setPrefSize(PANE_W, PANE_H);

        Rectangle sky = new Rectangle(0, 0, PANE_W, PANE_H);
        sky.setFill(Color.rgb(228, 238, 255));

        // ground polygon under parabola
        Polygon ground = new Polygon();
        ground.setFill(Color.rgb(150, 185, 215));
        ground.setStroke(null);

        // parabola curve line
        Polyline trackCurve = new Polyline();
        trackCurve.setStroke(Color.rgb(40, 70, 155));
        trackCurve.setStrokeWidth(5);
        trackCurve.setFill(null);

        double step = (TRACK_X_END - TRACK_X_START) / TRACK_POINTS;
        for (int i = 0; i <= TRACK_POINTS; i++) {
            double x = TRACK_X_START + i * step;
            double y = trackY(x);
            trackCurve.getPoints().addAll(x, y);
            ground.getPoints().addAll(x, y);
        }
        ground.getPoints().addAll(TRACK_X_END, PANE_H - 10);
        ground.getPoints().addAll(TRACK_X_START, PANE_H - 10);

        // "PARABOLA" label at bottom of curve
        Text trackLabel = new Text(TRACK_MID_X - 30, TRACK_BOTTOM_Y + 18, "PARABOLA TRACK");
        trackLabel.setFill(Color.rgb(40, 70, 155));
        trackLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 10));

        // finish line at right end
        double finX = TRACK_X_END;
        double finY = trackY(finX);
        finishLine = new Line(finX, finY - 30, finX, finY + 20);
        finishLine.setStroke(Color.rgb(200, 50, 50));
        finishLine.setStrokeWidth(2);
        finishLine.getStrokeDashArray().addAll(8.0, 4.0);

        finishLabel = new Text(finX - 28, finY - 34, "FINISH");
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

        // arrows
        velocityLine = new Line(); velocityLine.setStroke(Color.rgb(0, 200, 80));   velocityLine.setStrokeWidth(2.5);
        gravityLine  = new Line(); gravityLine .setStroke(Color.rgb(220, 50, 50));  gravityLine .setStrokeWidth(2.5);
        normalLine   = new Line(); normalLine  .setStroke(Color.rgb(255, 200, 0));  normalLine  .setStrokeWidth(2.5);
        frictionLine = new Line(); frictionLine.setStroke(Color.rgb(255, 140, 0));  frictionLine.setStrokeWidth(2.5);

        velocityHead = new Polygon(); velocityHead.setFill(Color.rgb(0, 200, 80));
        gravityHead  = new Polygon(); gravityHead .setFill(Color.rgb(220, 50, 50));
        normalHead   = new Polygon(); normalHead  .setFill(Color.rgb(255, 200, 0));
        frictionHead = new Polygon(); frictionHead.setFill(Color.rgb(255, 140, 0));

        velocityLabel = new Text();     velocityLabel.setFill(Color.rgb(0, 200, 80));   velocityLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 10));
        gravityLabel  = new Text("Fg"); gravityLabel .setFill(Color.rgb(220, 50, 50));  gravityLabel .setFont(Font.font("Courier New", FontWeight.BOLD, 10));
        normalLabel   = new Text("N");  normalLabel  .setFill(Color.rgb(255, 200, 0));  normalLabel  .setFont(Font.font("Courier New", FontWeight.BOLD, 10));
        frictionLabel = new Text("Ff"); frictionLabel.setFill(Color.rgb(255, 140, 0));  frictionLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 10));

        // info box — exactly same position as LevelTest
        double infoY = 465;
        infoBox = new Rectangle(10, infoY, 195, 105);
        infoBox.setFill(Color.rgb(10, 22, 55));
        infoBox.setStroke(Color.rgb(30, 144, 255));
        infoBox.setStrokeWidth(1);

        infoTitle = new Text(18, infoY + 19, "LIVE PHYSICS — LEVEL 2");
        infoTitle.setFill(Color.rgb(147, 227, 227));
        infoTitle.setFont(Font.font("Courier New", 10));

        keText = new Text(18, infoY + 35, "KE = 0.00 J");
        peText = new Text(18, infoY + 49, "PE = 0.00 J");
        teText = new Text(18, infoY + 63, "TE = 0.00 J");
        vText  = new Text(18, infoY + 77, "v  = 0.00 m/s");
        tText  = new Text(18, infoY + 91, "t  = 0.00 s");

        for (Text tx : new Text[]{ keText, peText, teText, vText, tText }) {
            tx.setFill(Color.rgb(173, 216, 230));
            tx.setFont(Font.font("Courier New", 10));
        }

        // chart card — exact copy of LevelTest layout
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

        // 2x2 grid — exact same positions as LevelTest
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
                sky, ground, trackCurve, trackLabel,
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

    private void update() {

        double screenX = arcDistToScreenX(cart.getState().position.x);
        double screenY = trackY(screenX);
        double angle   = trackAngle(screenX);
        double normalX = -Math.sin(angle);
        double normalY =  Math.cos(angle);

        double ballX = screenX - normalX * BALL_RADIUS;
        double ballY = screenY - normalY * BALL_RADIUS;

        ball.setCenterX(ballX); ball.setCenterY(ballY);
        ballShine.setCenterX(ballX - BALL_RADIUS * 0.3);
        ballShine.setCenterY(ballY - BALL_RADIUS * 0.4);

        double v    = cart.getState().velocity.x;
        double mass = cart.getMass();
        double Fg   = mass * 9.81;
        double N    = mass * 9.81 * Math.cos(angle);
        double ff   = track.getFrictionCoeff() * N;

        if (Math.abs(v) > 0.05) {
            double len = Math.min(VELOCITY_BASE + Math.abs(v) * VELOCITY_SCALE, ARROW_MAX);
            double dir = v > 0 ? 1 : -1;
            setArrow(velocityLine, velocityHead, velocityLabel, ballX, ballY,
                    ballX + dir * len * Math.cos(angle), ballY + dir * len * Math.sin(angle));
            velocityLabel.setText("v=" + String.format("%.1f", v) + "m/s");
            velocityLine.setVisible(true); velocityHead.setVisible(true); velocityLabel.setVisible(true);
        } else {
            velocityLine.setVisible(false); velocityHead.setVisible(false); velocityLabel.setVisible(false);
        }

        double fgLen = Math.min(GRAVITY_BASE + Fg * GRAVITY_SCALE, ARROW_MAX);
        setArrow(gravityLine, gravityHead, gravityLabel, ballX, ballY, ballX, ballY + fgLen);

        double nLen = Math.min(NORMAL_BASE + N * NORMAL_SCALE, ARROW_MAX);
        setArrow(normalLine, normalHead, normalLabel, ballX, ballY,
                ballX - normalX * nLen, ballY - normalY * nLen);

        if (Math.abs(v) > 0.05) {
            double ffLen = Math.min(FRICTION_BASE + ff * FRICTION_SCALE, ARROW_MAX);
            double dir = v > 0 ? 1 : -1;
            setArrow(frictionLine, frictionHead, frictionLabel, ballX, ballY,
                    ballX - dir * ffLen * Math.cos(angle), ballY - dir * ffLen * Math.sin(angle));
            frictionLine.setVisible(true); frictionHead.setVisible(true); frictionLabel.setVisible(true);
        } else {
            frictionLine.setVisible(false); frictionHead.setVisible(false); frictionLabel.setVisible(false);
        }

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
        head.getPoints().setAll(x2, y2,
                x2 - size * Math.cos(angle - spread), y2 - size * Math.sin(angle - spread),
                x2 - size * Math.cos(angle + spread), y2 - size * Math.sin(angle + spread));
        label.setX(x2 + 4); label.setY(y2 - 2);
    }

    public void start()  { lastTime = 0; timer.start(); }
    public void stop()   { timer.stop(); lastTime = 0; }

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

    public Pane getView()             { return pane; }
    public MotionData getMotionData() { return motionData; }
}

