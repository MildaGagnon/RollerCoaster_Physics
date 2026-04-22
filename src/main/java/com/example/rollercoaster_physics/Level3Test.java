package com.example.rollercoaster_physics;

import javafx.animation.AnimationTimer;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

// Level 3 — slope into a full vertical loop
// ball needs v² >= g*r at the top to complete the loop
public class Level3Test {

    private static final double PANE_W = 950;
    private static final double PANE_H = 580;

    // track constants — must match PhysicsEngine and SimPanel exactly
    private static final double LOOP_RADIUS   = 90;
    private static final double LOOP_CENTER_X = 400;
    private static final double LOOP_CENTER_Y = 260;
    private static final double LOOP_BOTTOM_Y = LOOP_CENTER_Y + LOOP_RADIUS; // ground level = 350

    private static final double SLOPE_START_X = 60;
    private static final double SLOPE_START_Y = 140;
    private static final double SLOPE_END_X   = LOOP_CENTER_X - LOOP_RADIUS; // 310 — left tangent
    private static final double SLOPE_END_Y   = LOOP_BOTTOM_Y;               // 350

    private static final double EXIT_START_X  = LOOP_CENTER_X + LOOP_RADIUS; // right tangent point where loop meets flat exit
    private static final double EXIT_END_X    = 750;
    private static final double EXIT_Y        = LOOP_BOTTOM_Y; // 350 — flat ground

    private static final double PIXELS_PER_METER = 35; // 1m = 35px
    private static final double BALL_RADIUS       = 14;

    private static final int LOOP_POINTS = 120; // polyline segments for smooth loop circle

    // arrow lengths: BASE + value * SCALE, capped at ARROW_MAX
    private static final double GRAVITY_BASE      = 18;
    private static final double GRAVITY_SCALE     = 0.8;
    private static final double NORMAL_BASE       = 18;
    private static final double NORMAL_SCALE      = 0.8;
    private static final double FRICTION_BASE     = 35;
    private static final double FRICTION_SCALE    = 3.0;
    private static final double VELOCITY_BASE     = 16;
    private static final double VELOCITY_SCALE    = 3.0;
    private static final double ARROW_MAX         = 55;

    // segment lengths — computed once at startup
    private static final double SLOPE_LEN_PX; // straight-line slope length
    private static final double LOOP_CIRC_PX; // full loop circumference = 2πr
    private static final double EXIT_LEN_PX;
    private static final double TOTAL_LEN_M;  // total track in metres

    static {
        SLOPE_LEN_PX = Math.sqrt(
                (SLOPE_END_X - SLOPE_START_X) * (SLOPE_END_X - SLOPE_START_X) +
                        (SLOPE_END_Y - SLOPE_START_Y) * (SLOPE_END_Y - SLOPE_START_Y)); // Pythagoras
        LOOP_CIRC_PX = 2 * Math.PI * LOOP_RADIUS;
        EXIT_LEN_PX  = EXIT_END_X - EXIT_START_X;
        TOTAL_LEN_M  = (SLOPE_LEN_PX + LOOP_CIRC_PX + EXIT_LEN_PX) / PIXELS_PER_METER;
    }

    private Cart          cart;
    private Track         track;
    private PhysicsEngine physics;
    private MotionData    motionData;
    private Pane          pane;

    private Circle  ball;
    private Circle  ballShine; // highlight dot

    private Line    velocityLine, gravityLine, normalLine, frictionLine, centripetalLine;
    private Polygon velocityHead, gravityHead, normalHead, frictionHead, centripetalHead;
    private Text    velocityLabel, gravityLabel, normalLabel, frictionLabel, centripetalLabel;

    private Rectangle infoBox;
    private Text      infoTitle, keText, peText, teText, vText, tText;
    private Text      fcText; // centripetal force — purple

    private Line finishLine;
    private Text finishLabel;
    private Text fallWarning; // shown when ball fails loop

    // charts received from RollerCoasterUI — shared, never created here
    private ChartPosition     chartPos;
    private ChartVelocity     chartVel;
    private ChartAcceleration chartAcc;
    private ChartEnergy       chartEnergy;

    private AnimationTimer timer;
    private long   lastTime; // nanoseconds
    private double simTime;  // seconds
    private boolean ballFell = false; // true once ball has failed the loop

    public Level3Test(Cart cart, Track track,
                      ChartPosition chartPos, ChartVelocity chartVel,
                      ChartAcceleration chartAcc, ChartEnergy chartEnergy) {
        this.cart        = cart;
        this.track       = track;
        this.chartPos    = chartPos;
        this.chartVel    = chartVel;
        this.chartAcc    = chartAcc;
        this.chartEnergy = chartEnergy;

        Level3.setup(track); // slopeAngle=-1 signals loop track, friction=0.05

        physics    = new PhysicsEngine(cart, track);
        motionData = new MotionData();
        simTime    = 0;
        lastTime   = 0;
        buildPane();

        timer = new AnimationTimer() {
            public void handle(long now) {
                if (lastTime == 0) { lastTime = now; return; } // init on first frame

                float dt = (float)((now - lastTime) / 1_000_000_000.0); // ns → seconds
                if (dt > 0.04f) dt = 0.04f; // cap to avoid physics jumps
                lastTime = now;

                physics.update(dt);
                simTime += dt;

                // centripetal check — v² must be >= g*r at top to stay on track
                double loopPx = cart.getState().position.x * PIXELS_PER_METER - SLOPE_LEN_PX;
                if (!ballFell && loopPx > 0 && loopPx <= LOOP_CIRC_PX) {
                    double angleInLoop = loopPx / LOOP_RADIUS; // radians into loop (PI = top)
                    boolean nearTop = angleInLoop > Math.PI * 0.65
                            && angleInLoop < Math.PI * 1.35;
                    double v = cart.getState().velocity.x;
                    double r = LOOP_RADIUS / PIXELS_PER_METER; // metres
                    if (nearTop && v * v < 9.81 * r) { // v² < g*r → too slow at top
                        ballFell = true;
                        double rollbackSpeed = Math.max(Math.abs(v), Math.sqrt(9.81 * r)) + 1.0; // +1 boost to exit loop
                        cart.getState().velocity.x = -(float) rollbackSpeed; // reverse
                    }
                    if (Math.abs(v) < 0.05f) { // stopped mid-loop due to high friction
                        ballFell = true;
                        cart.getState().velocity.x = -(float)(Math.sqrt(9.81 * r) * 0.6);
                    }
                }

                if (cart.getState().position.x >= TOTAL_LEN_M) {
                    cart.getState().position.x     = (float) TOTAL_LEN_M;
                    cart.getState().velocity.x     = 0;
                    cart.getState().acceleration.x = 0;
                    stop();
                }
                if (cart.getState().position.x < 0) {
                    cart.getState().position.x = 0;
                    cart.getState().velocity.x = 0;
                    cart.getState().acceleration.x = 0;
                    stop();
                }
                // stop on flat exit when barely moving
                double posPx = cart.getState().position.x * PIXELS_PER_METER;
                boolean onExit = posPx > SLOPE_LEN_PX + LOOP_CIRC_PX;
                if (onExit && Math.abs(cart.getState().velocity.x) < 0.01f) {
                    cart.getState().velocity.x     = 0;
                    cart.getState().acceleration.x = 0;
                    stop();
                }

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
                chartEnergy.update(motionData);
                update();
            }
        };
    }

    // maps arc metres to screen (x,y) across 3 segments: slope → loop → flat exit
    private double[] positionOnTrack(double metres) {
        double px = metres * PIXELS_PER_METER;
        if (px <= SLOPE_LEN_PX) {
            double t = px / SLOPE_LEN_PX; // 0=top of slope, 1=bottom
            return new double[]{
                    SLOPE_START_X + t * (SLOPE_END_X - SLOPE_START_X),
                    SLOPE_START_Y + t * (SLOPE_END_Y - SLOPE_START_Y)
            };
        }
        px -= SLOPE_LEN_PX;
        if (px <= LOOP_CIRC_PX) {
            double angle = (Math.PI / 2) - (px / LOOP_RADIUS); // starts at bottom (PI/2), counter-clockwise
            return new double[]{
                    LOOP_CENTER_X + LOOP_RADIUS * Math.cos(angle),
                    LOOP_CENTER_Y + LOOP_RADIUS * Math.sin(angle)
            };
        }
        px -= LOOP_CIRC_PX;
        double t = Math.min(px / EXIT_LEN_PX, 1.0);
        return new double[]{ EXIT_START_X + t * (EXIT_END_X - EXIT_START_X), EXIT_Y };
    }

    // tangent angle at current position — used to orient arrows and offset ball
    private double angleOnTrack(double metres) {
        double px = metres * PIXELS_PER_METER;
        if (px <= SLOPE_LEN_PX)
            return Math.atan2(SLOPE_END_Y - SLOPE_START_Y, SLOPE_END_X - SLOPE_START_X);
        px -= SLOPE_LEN_PX;
        if (px <= LOOP_CIRC_PX) {
            double angle = (Math.PI / 2) - (px / LOOP_RADIUS);
            return angle - Math.PI / 2; // tangent = radial - 90°
        }
        return 0; // flat exit
    }

    // radial angle from loop centre to ball — used to aim centripetal arrow
    private double loopRadialAngle(double metres) {
        double px = metres * PIXELS_PER_METER - SLOPE_LEN_PX;
        return (Math.PI / 2) - (px / LOOP_RADIUS);
    }

    private boolean inLoop(double metres) {
        double px = metres * PIXELS_PER_METER - SLOPE_LEN_PX;
        return px > 0 && px <= LOOP_CIRC_PX;
    }

    // height above EXIT_Y (ground) — used for PE = mgh
    private double getHeight() {
        double[] pos    = positionOnTrack(cart.getState().position.x);
        double heightPx = EXIT_Y - pos[1]; // positive above ground
        if (heightPx < 0) heightPx = 0;
        return heightPx / PIXELS_PER_METER;
    }

    private void buildPane() {

        pane = new Pane();
        pane.setPrefSize(PANE_W, PANE_H);

        Rectangle sky = new Rectangle(0, 0, PANE_W, PANE_H); // light blue background
        sky.setFill(Color.rgb(228, 238, 255));

        Polygon ground = new Polygon( // filled area under slope and exit
                SLOPE_START_X, SLOPE_START_Y,
                SLOPE_END_X,   SLOPE_END_Y,
                EXIT_START_X,  EXIT_Y,
                EXIT_END_X,    EXIT_Y,
                EXIT_END_X,    PANE_H - 10,
                SLOPE_START_X, PANE_H - 10
        );
        ground.setFill(Color.rgb(150, 185, 215));
        ground.setStroke(null);

        Line slope = new Line(SLOPE_START_X, SLOPE_START_Y, SLOPE_END_X, SLOPE_END_Y);
        slope.setStroke(Color.rgb(40, 70, 155));
        slope.setStrokeWidth(5);

        Line slopeToLoop = new Line(SLOPE_END_X, SLOPE_END_Y, LOOP_CENTER_X, LOOP_BOTTOM_Y); // slope → loop bottom
        slopeToLoop.setStroke(Color.rgb(40, 70, 155));
        slopeToLoop.setStrokeWidth(5);

        Line loopToExit = new Line(LOOP_CENTER_X, LOOP_BOTTOM_Y, EXIT_START_X, EXIT_Y); // loop bottom → exit
        loopToExit.setStroke(Color.rgb(40, 70, 155));
        loopToExit.setStrokeWidth(5);

        Line platform = new Line(SLOPE_START_X - 40, SLOPE_START_Y, SLOPE_START_X, SLOPE_START_Y); // starting ledge
        platform.setStroke(Color.rgb(40, 70, 155));
        platform.setStrokeWidth(4);

        // loop as smooth polyline — 120 points, starts at bottom (PI/2), counter-clockwise
        Polyline loopCurve = new Polyline();
        loopCurve.setStroke(Color.rgb(40, 70, 155));
        loopCurve.setStrokeWidth(5);
        loopCurve.setFill(null);

        double angleStep = (2 * Math.PI) / LOOP_POINTS;
        for (int i = 0; i <= LOOP_POINTS; i++) {
            double angle = (Math.PI / 2) - i * angleStep;
            double x = LOOP_CENTER_X + LOOP_RADIUS * Math.cos(angle);
            double y = LOOP_CENTER_Y + LOOP_RADIUS * Math.sin(angle);
            loopCurve.getPoints().addAll(x, y);
        }

        Line radiusLine = new Line(LOOP_CENTER_X, LOOP_CENTER_Y,
                LOOP_CENTER_X + LOOP_RADIUS, LOOP_CENTER_Y); // dashed educational reference
        radiusLine.setStroke(Color.rgb(100, 100, 130));
        radiusLine.setStrokeWidth(1.5);
        radiusLine.getStrokeDashArray().addAll(5.0, 3.0);

        Text radiusLabel = new Text(LOOP_CENTER_X + 4, LOOP_CENTER_Y - 5,
                "r = " + String.format("%.1f", LOOP_RADIUS / PIXELS_PER_METER) + " m");
        radiusLabel.setFill(Color.rgb(60, 60, 100));
        radiusLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 10));

        Line exit = new Line(EXIT_START_X, EXIT_Y, EXIT_END_X, EXIT_Y); // flat exit
        exit.setStroke(Color.rgb(40, 70, 155));
        exit.setStrokeWidth(5);

        finishLine = new Line(EXIT_END_X, EXIT_Y - 30, EXIT_END_X, EXIT_Y + 20); // dashed red
        finishLine.setStroke(Color.rgb(200, 50, 50));
        finishLine.setStrokeWidth(2);
        finishLine.getStrokeDashArray().addAll(8.0, 4.0);

        finishLabel = new Text(EXIT_END_X - 20, EXIT_Y - 34, "FINISH");
        finishLabel.setFill(Color.rgb(200, 50, 50));
        finishLabel.setFont(Font.font("Courier New", 10));

        ball = new Circle(BALL_RADIUS);
        ball.setFill(Color.rgb(30, 144, 255));
        ball.setStroke(Color.rgb(15, 90, 180));
        ball.setStrokeWidth(2);

        ballShine = new Circle(BALL_RADIUS * 0.25); // small highlight
        ballShine.setFill(Color.rgb(200, 230, 255));
        ballShine.setOpacity(0.7);

        velocityLine    = new Line(); velocityLine   .setStroke(Color.rgb(0, 200, 80));   velocityLine   .setStrokeWidth(2.5); // green
        gravityLine     = new Line(); gravityLine    .setStroke(Color.rgb(220, 50, 50));  gravityLine    .setStrokeWidth(2.5); // red
        normalLine      = new Line(); normalLine     .setStroke(Color.rgb(255, 200, 0));  normalLine     .setStrokeWidth(2.5); // yellow
        frictionLine    = new Line(); frictionLine   .setStroke(Color.rgb(255, 140, 0));  frictionLine   .setStrokeWidth(2.5); // orange
        centripetalLine = new Line(); centripetalLine.setStroke(Color.rgb(200, 80, 220)); centripetalLine.setStrokeWidth(2.5); // purple, loop only

        velocityHead    = new Polygon(); velocityHead   .setFill(Color.rgb(0, 200, 80));
        gravityHead     = new Polygon(); gravityHead    .setFill(Color.rgb(220, 50, 50));
        normalHead      = new Polygon(); normalHead     .setFill(Color.rgb(255, 200, 0));
        frictionHead    = new Polygon(); frictionHead   .setFill(Color.rgb(255, 140, 0));
        centripetalHead = new Polygon(); centripetalHead.setFill(Color.rgb(200, 80, 220));

        velocityLabel    = new Text();     velocityLabel   .setFill(Color.rgb(0, 200, 80));   velocityLabel   .setFont(Font.font("Courier New", FontWeight.BOLD, 10));
        gravityLabel     = new Text("Fg"); gravityLabel    .setFill(Color.rgb(220, 50, 50));  gravityLabel    .setFont(Font.font("Courier New", FontWeight.BOLD, 10));
        normalLabel      = new Text("N");  normalLabel     .setFill(Color.rgb(255, 200, 0));  normalLabel     .setFont(Font.font("Courier New", FontWeight.BOLD, 10));
        frictionLabel    = new Text("Ff"); frictionLabel   .setFill(Color.rgb(255, 140, 0));  frictionLabel   .setFont(Font.font("Courier New", FontWeight.BOLD, 10));
        centripetalLabel = new Text("Fc"); centripetalLabel.setFill(Color.rgb(200, 80, 220)); centripetalLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 10));

        double infoY = 465;
        infoBox = new Rectangle(10, infoY, 210, 120); // live data box, bottom-left
        infoBox.setFill(Color.rgb(10, 22, 55));
        infoBox.setStroke(Color.rgb(30, 144, 255));
        infoBox.setStrokeWidth(1);

        infoTitle = new Text(18, infoY + 19, "LIVE PHYSICS — LEVEL 3");
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

        fcText = new Text(18, infoY + 105, "Fc = 0.00 N"); // centripetal force, purple
        fcText.setFill(Color.rgb(200, 80, 220));
        fcText.setFont(Font.font("Courier New", 10));

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

        // 2x2 grid positions
        double cX1 = cardX + 6;
        double cX2 = cardX + 184;
        double cY1 = cardY + 14;
        double cY2 = cardY + 118;

        chartPos.getPane().setLayoutX(cX1);    chartPos.getPane().setLayoutY(cY1);    // top-left
        chartVel.getPane().setLayoutX(cX2);    chartVel.getPane().setLayoutY(cY1);    // top-right
        chartAcc.getPane().setLayoutX(cX1);    chartAcc.getPane().setLayoutY(cY2);    // bottom-left
        chartEnergy.getPane().setLayoutX(cX2); chartEnergy.getPane().setLayoutY(cY2); // bottom-right

        fallWarning = new Text(LOOP_CENTER_X - 160, LOOP_CENTER_Y, "TOO SLOW — ROLLING BACK!");
        fallWarning.setFill(Color.rgb(230, 30, 30));
        fallWarning.setFont(Font.font("Courier New", FontWeight.BOLD, 16));
        fallWarning.setVisible(false);

        pane.getChildren().addAll(
                sky, ground,
                slope, platform, slopeToLoop, loopToExit, loopCurve, radiusLine, radiusLabel,
                exit, finishLine, finishLabel,
                velocityLine, gravityLine, normalLine, frictionLine, centripetalLine,
                velocityHead, gravityHead, normalHead, frictionHead, centripetalHead,
                velocityLabel, gravityLabel, normalLabel, frictionLabel, centripetalLabel,
                ball, ballShine,
                infoBox, infoTitle,
                keText, peText, teText, vText, tText, fcText,
                cardBg, chartTitle,
                chartPos.getPane(), chartVel.getPane(),
                chartAcc.getPane(), chartEnergy.getPane(),
                fallWarning // LAST = always on top
        );
        update();
    }

    private void update() {

        double pos    = cart.getState().position.x;
        double[] xy   = positionOnTrack(pos);
        double angle  = angleOnTrack(pos);

        double normalX = -Math.sin(angle); // perpendicular to surface
        double normalY =  Math.cos(angle);
        double ballX   = xy[0] - normalX * BALL_RADIUS; // offset so ball sits on surface
        double ballY   = xy[1] - normalY * BALL_RADIUS;

        // normal arrow direction: inward toward loop centre inside loop, away from surface outside
        double nDirX, nDirY;
        if (inLoop(pos)) {
            double toCX = LOOP_CENTER_X - ballX; // vector toward centre
            double toCY = LOOP_CENTER_Y - ballY;
            double len  = Math.sqrt(toCX * toCX + toCY * toCY); // normalise
            nDirX = toCX / len;
            nDirY = toCY / len;
        } else {
            nDirX = -normalX; // away from surface
            nDirY = -normalY;
        }

        ball.setCenterX(ballX); ball.setCenterY(ballY);
        ballShine.setCenterX(ballX - BALL_RADIUS * 0.3);
        ballShine.setCenterY(ballY - BALL_RADIUS * 0.4);

        double v    = cart.getState().velocity.x;
        double mass = cart.getMass();
        double Fg   = mass * 9.81;                              // N
        double N    = mass * 9.81 * Math.abs(Math.cos(angle));  // perpendicular to track
        double ff   = track.getFrictionCoeff() * N;             // μN
        double r    = LOOP_RADIUS / PIXELS_PER_METER;           // metres
        double Fc   = inLoop(pos) ? mass * v * v / r : 0;       // mv²/r — only in loop

        if (Math.abs(v) > 0.05) { // hidden when nearly stopped
            double len = Math.min(VELOCITY_BASE + Math.abs(v) * VELOCITY_SCALE, ARROW_MAX);
            double dir = v > 0 ? 1 : -1; // flip when rolling backward
            setArrow(velocityLine, velocityHead, velocityLabel, ballX, ballY,
                    ballX + dir * len * Math.cos(angle),
                    ballY + dir * len * Math.sin(angle));
            velocityLabel.setText("v=" + String.format("%.1f", Math.abs(v)) + "m/s");
            velocityLine.setVisible(true); velocityHead.setVisible(true); velocityLabel.setVisible(true);
        } else {
            velocityLine.setVisible(false); velocityHead.setVisible(false); velocityLabel.setVisible(false);
        }

        double fgLen = Math.min(GRAVITY_BASE + Fg * GRAVITY_SCALE, ARROW_MAX);
        setArrow(gravityLine, gravityHead, gravityLabel, ballX, ballY, ballX, ballY + fgLen); // straight down

        double nLen = Math.min(NORMAL_BASE + N * 0.3, 35); // capped smaller so Fc arrow stays visible
        setArrow(normalLine, normalHead, normalLabel, ballX, ballY,
                ballX + nDirX * nLen, ballY + nDirY * nLen);

        if (Math.abs(v) > 0.05) { // hidden when nearly stopped
            double ffLen = Math.min(FRICTION_BASE + ff * FRICTION_SCALE, ARROW_MAX);
            double dir   = v > 0 ? 1 : -1; // flip when rolling backward
            setArrow(frictionLine, frictionHead, frictionLabel, ballX, ballY,
                    ballX - dir * ffLen * Math.cos(angle), // opposes motion
                    ballY - dir * ffLen * Math.sin(angle));
            frictionLine.setVisible(true); frictionHead.setVisible(true); frictionLabel.setVisible(true);
        } else {
            frictionLine.setVisible(false); frictionHead.setVisible(false); frictionLabel.setVisible(false);
        }

        if (inLoop(pos) && Math.abs(v) > 0.05) { // centripetal — only inside loop
            double ra    = loopRadialAngle(pos);
            double fcLen = Math.min(NORMAL_BASE + Fc * 0.4, ARROW_MAX);
            setArrow(centripetalLine, centripetalHead, centripetalLabel, ballX, ballY,
                    ballX - Math.cos(ra) * fcLen, // points toward loop centre
                    ballY - Math.sin(ra) * fcLen);
            centripetalLabel.setText("Fc=" + String.format("%.0f", Fc) + "N");
            centripetalLine.setVisible(true); centripetalHead.setVisible(true); centripetalLabel.setVisible(true);
        } else {
            centripetalLine.setVisible(false); centripetalHead.setVisible(false); centripetalLabel.setVisible(false);
        }

        double h  = getHeight();
        double ke = 0.5 * mass * v * v; // KE = ½mv²
        double pe = mass * 9.81 * h;    // PE = mgh
        double te = ke + pe;
        keText.setText("KE = " + String.format("%.2f", ke) + " J");
        peText.setText("PE = " + String.format("%.2f", pe) + " J");
        teText.setText("TE = " + String.format("%.2f", te) + " J");
        vText .setText("v  = " + String.format("%.2f", v)  + " m/s");
        tText .setText("t  = " + String.format("%.2f", simTime) + " s");
        fcText.setText("Fc = " + String.format("%.2f", Fc) + " N");

        fallWarning.setVisible(ballFell); // always on top — added last in pane
    }

    // draws line + arrowhead + label for a force vector
    private void setArrow(Line line, Polygon head, Text label,
                          double x1, double y1, double x2, double y2) {
        line.setStartX(x1); line.setStartY(y1);
        line.setEndX(x2);   line.setEndY(y2);
        double angle  = Math.atan2(y2 - y1, x2 - x1); // arrow direction
        double size   = 10;
        double spread = Math.toRadians(25); // arrowhead wing angle
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
        simTime  = 0;
        ballFell = false; // clear flag so warning hides and check runs again
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