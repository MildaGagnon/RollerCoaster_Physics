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

// Level 1 — straight ramp simulation
// ball rolls down a fixed 25-degree incline
// with gravity and friction

public class LevelTest {

    // ── ramp constants ──────────────────────────────────────────
    private static final double SLOPE_DEGREES    = 25;                          // incline angle in degrees
    private static final double SLOPE_RADIANS    = Math.toRadians(SLOPE_DEGREES); // pre-converted for trig calls
    private static final double PIXELS_PER_METER = 35;                          // scale factor: 1m = 35px
    private static final double BALL_RADIUS      = 18;                          // visual radius of the ball
    private static final double PANE_W           = 950;                         // simulation pane width
    private static final double PANE_H           = 580;                         // simulation pane height
    private static final double RAMP_START_X     = 60;                          // x position of ramp top
    private static final double RAMP_START_Y     = 75;                          // y position of ramp top

    // ── arrow constants — scale with physics, capped so they stay small ──
    private static final double GRAVITY_BASE   = 18;   // minimum arrow length for gravity
    private static final double GRAVITY_SCALE  = 0.8;  // pixels added per Newton
    private static final double NORMAL_BASE    = 18;   // minimum arrow length for normal force
    private static final double NORMAL_SCALE   = 0.8;
    private static final double FRICTION_BASE  = 35;   // minimum arrow length for friction
    private static final double FRICTION_SCALE = 3;    // pixels added per Newton
    private static final double VELOCITY_BASE  = 16;   // minimum arrow length for velocity
    private static final double VELOCITY_SCALE = 3.0;  // pixels added per m/s
    private static final double ARROW_MAX      = 80;   // maximum arrow length in pixels — prevents overlap

    // ── objects ─────────────────────────────────────────────────
    private Cart          cart;       // holds mass, velocity, and state
    private Track         track;      // holds friction coefficient and slope angle
    private PhysicsEngine physics;    // computes forces and updates position each frame
    private MotionData    motionData; // stores position/velocity/energy history for charts

    // ── pane ────────────────────────────────────────────────────
    private Pane pane; // root JavaFX container for all shapes in this level

    // ── ball ────────────────────────────────────────────────────
    private Circle ball;      // main blue ball
    private Circle ballShine; // small white highlight dot for visual polish

    // ── force arrow lines ────────────────────────────────────────
    private Line velocityLine;  // green — shows current velocity direction and magnitude
    private Line gravityLine;   // red — always points straight down
    private Line normalLine;    // yellow — perpendicular away from ramp surface
    private Line frictionLine;  // orange — opposes direction of motion

    // ── arrowheads — triangles drawn at the tip of each arrow ────
    private Polygon velocityHead;
    private Polygon gravityHead;
    private Polygon normalHead;
    private Polygon frictionHead;

    // ── arrow labels ─────────────────────────────────────────────
    private Text velocityLabel; // shows current speed value
    private Text gravityLabel;  // "Fg"
    private Text normalLabel;   // "N"
    private Text frictionLabel; // "Ff"

    // ── info box — live physics values bottom left ────────────────
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

    // ── charts — created here for Level 1 only ───────────────────
    // Levels 2 and 3 receive charts from RollerCoasterUI instead
    private ChartPosition     chartPos;
    private ChartVelocity     chartVel;
    private ChartAcceleration chartAcc;
    private ChartEnergy       chartEnergy;

    // ── timer ────────────────────────────────────────────────────
    private AnimationTimer timer;    // JavaFX game loop — fires every frame (~60fps)
    private long   lastTime;         // timestamp of previous frame in nanoseconds
    private double simTime;          // total elapsed simulation time in seconds

    // ── ramp geometry — computed once in constructor ──────────────
    private double rampEndX;
    private double rampEndY;
    private double rampLengthPx;

    // ── constructor ──────────────────────────────────────────────
    public LevelTest(Cart cart, Track track, ChartPosition chartPosition, ChartVelocity chartVelocity,
                     ChartAcceleration chartAcceleration, ChartEnergy chartEnergy) {
        this.cart  = cart;
        this.track = track;

        // apply Level 1 settings
        Level1.setup(track);

        physics    = new PhysicsEngine(cart, track);
        motionData = new MotionData();

        // compute ramp end point from start position + length + angle
        rampLengthPx = PANE_W - RAMP_START_X - 80;
        rampEndX = RAMP_START_X + rampLengthPx * Math.cos(SLOPE_RADIANS);
        rampEndY = RAMP_START_Y + rampLengthPx * Math.sin(SLOPE_RADIANS);

        simTime  = 0;
        lastTime = 0;

        buildPane();

        timer = new AnimationTimer() {
            public void handle(long now) {
                if (lastTime == 0) { lastTime = now; return; } // skip first frame to initialise lastTime

                float dt = (float)((now - lastTime) / 1_000_000_000.0); // convert nanoseconds to seconds
                if (dt > 0.04f) { dt = 0.04f; } // cap dt to 40ms to avoid large physics jumps on lag
                lastTime = now;

                physics.update(dt); // advance physics by one time step
                simTime = simTime + dt;

                double maxPos = rampLengthPx / PIXELS_PER_METER; // finish line position in metres
                if (cart.getState().position.x >= maxPos) {
                    // ball reached the finish line — clamp and stop
                    cart.getState().position.x     = (float) maxPos;
                    cart.getState().velocity.x     = 0;
                    cart.getState().acceleration.x = 0;
                    stop();
                }

                // record this frame's physics values for the live charts
                double v   = cart.getState().velocity.x;
                double a   = cart.getState().acceleration.x;
                double pos = cart.getState().position.x;
                double h   = getHeight();
                double ke  = 0.5 * cart.getMass() * v * v; // KE = ½mv²
                double pe  = cart.getMass() * 9.81 * h;    // PE = mgh
                motionData.addPoint(simTime, pos, v, a, ke, pe);

                chartPos.update(motionData);
                chartVel.update(motionData);
                chartAcc.update(motionData);
                LevelTest.this.chartEnergy.update(motionData);

                update(); // redraw all shapes
            }
        };
    }

    // ── builds all shapes ────────────────────────────────────────
    private void buildPane() {

        pane = new Pane();
        pane.setPrefSize(PANE_W, PANE_H);

        // light blue sky background
        Rectangle sky = new Rectangle(0, 0, PANE_W, PANE_H);
        sky.setFill(Color.rgb(228, 238, 255));

        // blue filled polygon representing the slope hill body
        Polygon hill = new Polygon(
                RAMP_START_X, RAMP_START_Y,
                rampEndX,     rampEndY,
                rampEndX,     PANE_H - 15,
                RAMP_START_X, PANE_H - 15
        );
        hill.setFill(Color.rgb(150, 185, 215));
        hill.setStroke(null);

        // dark blue ramp surface line on top of the hill
        Line ramp = new Line(RAMP_START_X, RAMP_START_Y, rampEndX, rampEndY);
        ramp.setStroke(Color.rgb(40, 70, 155));
        ramp.setStrokeWidth(5);

        // short horizontal ledge at the top where ball starts
        Line platform = new Line(RAMP_START_X - 50, RAMP_START_Y, RAMP_START_X, RAMP_START_Y);
        platform.setStroke(Color.rgb(40, 70, 155));
        platform.setStrokeWidth(4);

        // angle indicator label at the bottom of the ramp
        Text angleLabel = new Text(rampEndX - 40, rampEndY - 4, SLOPE_DEGREES + "°");
        angleLabel.setFill(Color.rgb(55, 55, 65));
        angleLabel.setFont(Font.font("Courier New", 11));

        // horizontal reference line at ramp bottom for the angle indicator
        Line angleLine = new Line(rampEndX - 60, rampEndY, rampEndX + 5, rampEndY);
        angleLine.setStroke(Color.rgb(80, 80, 90));
        angleLine.setStrokeWidth(1);

        // dashed red finish line at the end of the ramp
        finishLine = new Line(rampEndX, rampEndY - 30, rampEndX, rampEndY + 20);
        finishLine.setStroke(Color.rgb(200, 50, 50));
        finishLine.setStrokeWidth(2);
        finishLine.getStrokeDashArray().addAll(8.0, 4.0); // dash pattern: 8px on, 4px off

        finishLabel = new Text(rampEndX - 20, rampEndY - 36, "FINISH");
        finishLabel.setFill(Color.rgb(200, 50, 50));
        finishLabel.setFont(Font.font("Courier New", 10));

        // main blue ball
        ball = new Circle(BALL_RADIUS);
        ball.setFill(Color.rgb(30, 144, 255));
        ball.setStroke(Color.rgb(15, 90, 180));
        ball.setStrokeWidth(2);

        // small white highlight dot offset up-left from ball centre for visual polish
        ballShine = new Circle(BALL_RADIUS * 0.25);
        ballShine.setFill(Color.rgb(200, 230, 255));
        ballShine.setOpacity(0.7);

        // force arrow lines — one per force vector
        velocityLine = new Line();
        velocityLine.setStroke(Color.rgb(0, 200, 80));    // green
        velocityLine.setStrokeWidth(2.5);

        gravityLine = new Line();
        gravityLine.setStroke(Color.rgb(220, 50, 50));    // red
        gravityLine.setStrokeWidth(2.5);

        normalLine = new Line();
        normalLine.setStroke(Color.rgb(255, 200, 0));     // yellow
        normalLine.setStrokeWidth(2.5);

        frictionLine = new Line();
        frictionLine.setStroke(Color.rgb(255, 140, 0));   // orange
        frictionLine.setStrokeWidth(2.5);

        // triangular arrowheads at the tip of each force arrow
        velocityHead = new Polygon(); velocityHead.setFill(Color.rgb(0, 200, 80));
        gravityHead  = new Polygon(); gravityHead .setFill(Color.rgb(220, 50, 50));
        normalHead   = new Polygon(); normalHead  .setFill(Color.rgb(255, 200, 0));
        frictionHead = new Polygon(); frictionHead.setFill(Color.rgb(255, 140, 0));

        // text labels beside each arrowhead
        velocityLabel = new Text();         // shows "v=X.X m/s", updated each frame
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

        // dark navy info box in the bottom-left corner
        infoBox = new Rectangle(10, 465, 195, 105);
        infoBox.setFill(Color.rgb(10, 22, 55));
        infoBox.setStroke(Color.rgb(30, 144, 255));
        infoBox.setStrokeWidth(1);

        infoTitle = new Text(18, 484, "LIVE PHYSICS — LEVEL 1");
        infoTitle.setFill(Color.rgb(147, 227, 227));
        infoTitle.setFont(Font.font("Courier New", 10));

        // live physics values — updated every frame in update()
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
        // card starts at y=455 so it sits at the absolute bottom of the 580px pane
        double cardX = 290;
        double cardY = 455;
        double cardW = 370;
        double cardH = 225;

        Rectangle cardBg = new Rectangle(cardX, cardY, cardW, cardH);
        cardBg.setFill(Color.rgb(10, 22, 55));
        cardBg.setStroke(Color.rgb(30, 80, 160));
        cardBg.setStrokeWidth(1);
        cardBg.setArcWidth(8);   // rounded corners
        cardBg.setArcHeight(8);

        Text chartTitle = new Text(cardX + 10, cardY + 11,
                "LIVE GRAPHS  —  Position · Velocity · Acceleration · Energy");
        chartTitle.setFill(Color.rgb(147, 227, 227));
        chartTitle.setFont(Font.font("Courier New", FontWeight.BOLD, 9));

        // Level 1 creates its own chart objects (Levels 2 and 3 receive them from RollerCoasterUI)
        chartPos    = new ChartPosition();
        chartVel    = new ChartVelocity();
        chartAcc    = new ChartAcceleration();
        chartEnergy = new ChartEnergy();

        // 2x2 grid layout inside the card
        double cX1 = cardX + 6;    // left column x
        double cX2 = cardX + 184;  // right column x
        double cY1 = cardY + 14;   // top row y
        double cY2 = cardY + 118;  // bottom row y

        chartPos.getPane().setLayoutX(cX1);
        chartPos.getPane().setLayoutY(cY1);

        chartVel.getPane().setLayoutX(cX2);
        chartVel.getPane().setLayoutY(cY1);

        chartAcc.getPane().setLayoutX(cX1);
        chartAcc.getPane().setLayoutY(cY2);

        chartEnergy.getPane().setLayoutX(cX2);
        chartEnergy.getPane().setLayoutY(cY2);

        // order matters — shapes added later render on top
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

        update(); // draw initial state before simulation starts
    }

    // ── updates all shapes every frame ───────────────────────────
    private void update() {

        // convert arc distance (metres) to pixel position on the ramp
        double distancePx = cart.getState().position.x * PIXELS_PER_METER;
        double rampX = RAMP_START_X + distancePx * Math.cos(SLOPE_RADIANS); // x along ramp
        double rampY = RAMP_START_Y + distancePx * Math.sin(SLOPE_RADIANS); // y along ramp

        // unit vector perpendicular to ramp surface, pointing away from surface (upward-left)
        double offsetX = -Math.sin(SLOPE_RADIANS);
        double offsetY =  Math.cos(SLOPE_RADIANS);

        // place ball centre offset from the ramp surface by BALL_RADIUS
        double ballX   = rampX - offsetX * BALL_RADIUS;
        double ballY   = rampY - offsetY * BALL_RADIUS;

        ball.setCenterX(ballX);
        ball.setCenterY(ballY);
        ballShine.setCenterX(ballX - BALL_RADIUS * 0.3); // highlight slightly up-left of centre
        ballShine.setCenterY(ballY - BALL_RADIUS * 0.4);

        double v    = cart.getState().velocity.x;
        double mass = cart.getMass();

        double Fg = mass * 9.81;                            // gravity force (N)
        double N  = mass * 9.81 * Math.cos(SLOPE_RADIANS); // normal force perpendicular to ramp
        double ff = track.getFrictionCoeff() * N;           // friction force = μN

        // velocity arrow — hidden when ball is nearly stopped (v < 0.05 m/s)
        if (Math.abs(v) > 0.05) {
            double len = Math.min(VELOCITY_BASE + Math.abs(v) * VELOCITY_SCALE, ARROW_MAX);
            setArrow(velocityLine, velocityHead, velocityLabel,
                    ballX, ballY,
                    ballX + len * Math.cos(SLOPE_RADIANS), // arrow points along the ramp
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

        // gravity arrow — always straight down regardless of ramp angle
        double fgLen = Math.min(GRAVITY_BASE + Fg * GRAVITY_SCALE, ARROW_MAX);
        setArrow(gravityLine, gravityHead, gravityLabel,
                ballX, ballY, ballX, ballY + fgLen); // ballY + fgLen = straight down in screen coords

        // normal arrow — perpendicular away from ramp surface (same direction as offsetX/offsetY)
        double nLen = Math.min(NORMAL_BASE + N * NORMAL_SCALE, ARROW_MAX);
        setArrow(normalLine, normalHead, normalLabel,
                ballX, ballY,
                ballX - offsetX * nLen, // negate offset to point away from surface
                ballY - offsetY * nLen);

        // friction arrow — hidden when nearly stopped; opposes direction of motion along ramp
        if (Math.abs(v) > 0.05) {
            double ffLen = Math.min(FRICTION_BASE + ff * FRICTION_SCALE, ARROW_MAX);
            setArrow(frictionLine, frictionHead, frictionLabel,
                    ballX, ballY,
                    ballX - ffLen * Math.cos(SLOPE_RADIANS), // points backward along ramp
                    ballY - ffLen * Math.sin(SLOPE_RADIANS));
            frictionLine.setVisible(true);
            frictionHead.setVisible(true);
            frictionLabel.setVisible(true);
        } else {
            frictionLine.setVisible(false);
            frictionHead.setVisible(false);
            frictionLabel.setVisible(false);
        }

        // update live physics info box
        double h  = getHeight();
        double ke = 0.5 * mass * v * v; // KE = ½mv²
        double pe = mass * 9.81 * h;    // PE = mgh
        double te = ke + pe;            // total energy (decreases with friction)

        keText.setText("KE = " + String.format("%.2f", ke) + " J");
        peText.setText("PE = " + String.format("%.2f", pe) + " J");
        teText.setText("TE = " + String.format("%.2f", te) + " J");
        vText .setText("v  = " + String.format("%.2f", v)  + " m/s");
        tText .setText("t  = " + String.format("%.2f", simTime) + " s");
    }

    // draws a line + triangular arrowhead + label for a force vector
    // (x1,y1) = arrow start (ball centre), (x2,y2) = arrow tip
    private void setArrow(Line line, Polygon head, Text label,
                          double x1, double y1, double x2, double y2) {
        line.setStartX(x1); line.setStartY(y1);
        line.setEndX(x2);   line.setEndY(y2);

        double angle  = Math.atan2(y2 - y1, x2 - x1); // direction angle of the arrow
        double size   = 10;                             // arrowhead side length
        double spread = Math.toRadians(25);             // half-angle between arrowhead wings

        // three vertices: tip, left wing, right wing
        head.getPoints().setAll(
                x2, y2,
                x2 - size * Math.cos(angle - spread),
                y2 - size * Math.sin(angle - spread),
                x2 - size * Math.cos(angle + spread),
                y2 - size * Math.sin(angle + spread)
        );

        label.setX(x2 + 4); // label placed just past the arrowhead tip
        label.setY(y2 - 2);
    }

    // height above ramp bottom in metres — used for PE = mgh calculation
    private double getHeight() {
        double totalMeters = rampLengthPx / PIXELS_PER_METER; // total ramp length in metres
        double remaining   = totalMeters - cart.getState().position.x; // distance left to travel
        if (remaining < 0) { remaining = 0; }
        return remaining * Math.sin(SLOPE_RADIANS); // vertical component of remaining distance
    }

    public void start() {
        lastTime = 0;   // reset so first frame initialises lastTime correctly
        timer.start();
    }

    public void stop() {
        timer.stop();
        lastTime = 0;
    }

    public void reset() {
        stop();
        cart.getState().position.x     = 0;                         // move ball back to start
        cart.getState().velocity.x     = (float) cart.getVelocity(); // restore initial velocity
        cart.getState().acceleration.x = 0;
        simTime = 0;
        motionData.clear(); // clear all chart history
        chartPos.update(motionData);    // redraw charts as empty
        chartVel.update(motionData);
        chartAcc.update(motionData);
        chartEnergy.update(motionData);
        update(); // redraw ball and arrows at starting position
    }

    public Pane getView() {
        return pane;
    }
    public MotionData getMotionData() {
        return motionData;
    }
}