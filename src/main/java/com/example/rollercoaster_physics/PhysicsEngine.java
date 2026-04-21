package com.example.rollercoaster_physics;

// computes forces and advances the cart state each frame
public class PhysicsEngine {

    private static final float G = 9.81f; // m/s²

    // Level 3 constants — must match Level3Test and SimPanel exactly
    private static final double L3_LOOP_RADIUS   = 90;
    private static final double L3_LOOP_CENTER_X = 400;
    private static final double L3_LOOP_CENTER_Y = 260;
    private static final double L3_LOOP_BOTTOM_Y = L3_LOOP_CENTER_Y + L3_LOOP_RADIUS; // 350
    private static final double L3_SLOPE_START_X = 60;
    private static final double L3_SLOPE_START_Y = 140;
    private static final double L3_SLOPE_END_X   = L3_LOOP_CENTER_X - L3_LOOP_RADIUS; // 310
    private static final double L3_SLOPE_END_Y   = L3_LOOP_BOTTOM_Y;                  // 350
    private static final double L3_EXIT_START_X  = L3_LOOP_CENTER_X + L3_LOOP_RADIUS; // 490
    private static final double L3_EXIT_END_X    = 750;
    private static final double L3_EXIT_Y        = L3_LOOP_BOTTOM_Y;
    private static final double L3_PPM           = 35; // pixels per metre

    // pre-computed segment lengths
    private static final double L3_SLOPE_LEN; // straight-line slope length in pixels
    private static final double L3_LOOP_CIRC; // full circumference = 2πr

    static {
        L3_SLOPE_LEN = Math.sqrt(
                (L3_SLOPE_END_X - L3_SLOPE_START_X) * (L3_SLOPE_END_X - L3_SLOPE_START_X) +
                        (L3_SLOPE_END_Y - L3_SLOPE_START_Y) * (L3_SLOPE_END_Y - L3_SLOPE_START_Y)); // Pythagoras
        L3_LOOP_CIRC = 2 * Math.PI * L3_LOOP_RADIUS;
    }

    private Cart  cart;
    private Track track;

    public PhysicsEngine(Cart cart, Track track) {
        this.cart  = cart;
        this.track = track;
    }

    // returns slope angle in radians at the ball's current position
    // branches on slopeAngle: >0 = Level1, <0 = Level3, 0 = Level2
    private float getCurrentAngleRad() {
        float storedAngle = track.getSlopeAngle();

        // Level 1 — fixed ramp angle
        if (storedAngle > 0) {
            return (float) Math.toRadians(storedAngle);
        }

        // Level 3 — 3-segment track: slope → loop → flat exit
        if (storedAngle < 0) {
            double px = cart.getState().position.x * L3_PPM;
            if (px <= L3_SLOPE_LEN) {
                return (float) Math.atan2(
                        L3_SLOPE_END_Y - L3_SLOPE_START_Y,
                        L3_SLOPE_END_X - L3_SLOPE_START_X); // fixed slope angle
            }
            px -= L3_SLOPE_LEN;
            if (px <= L3_LOOP_CIRC) {
                double angle = (Math.PI / 2) - (px / L3_LOOP_RADIUS);
                return (float)(angle - Math.PI / 2); // tangent = radial - 90°
            }
            return 0f; // flat exit
        }

        // Level 2 — parabola: walk arc to find current x, then compute tangent
        double trackXStart  = 60;
        double trackXEnd    = 790;
        double trackMidX    = (trackXStart + trackXEnd) / 2.0;
        double trackBottomY = 340;
        double trackLeftY   = 90;
        double A = (trackLeftY - trackBottomY)
                / ((trackMidX - trackXStart) * (trackMidX - trackXStart)); // parabola coefficient
        double pixelsPerMeter = 2.5;

        double pixels = cart.getState().position.x * pixelsPerMeter;
        double x = trackXStart;
        double accumulated = 0;
        double step = 0.5; // 0.5px steps for accuracy
        while (x < trackXEnd) {
            double y1     = trackBottomY + A * (x - trackMidX) * (x - trackMidX);
            double y2     = trackBottomY + A * (x + step - trackMidX) * (x + step - trackMidX);
            double segLen = Math.sqrt(step * step + (y2 - y1) * (y2 - y1)); // Pythagoras
            if (accumulated + segLen >= pixels) break;
            accumulated += segLen;
            x += step;
        }
        double dy_dx = 2.0 * A * (x - trackMidX); // derivative of parabola at x
        return (float) Math.atan2(dy_dx, 1.0);
    }

    // advances position and velocity by one time step dt (seconds)
    public void update(float dt) {
        Cart.cartState state = cart.getState();
        float mass    = (float) cart.getMass();
        float angle   = getCurrentAngleRad();
        float mu      = track.getFrictionCoeff();

        float gravityAlong = (float)(mass * G * Math.sin(angle)); // component along track
        float normalForce  = (float)(mass * G * Math.abs(Math.cos(angle))); // perpendicular to track

        // friction opposes direction of motion
        float vel = state.velocity.x;
        float friction;
        if      (vel >  0.02f) friction = -mu * normalForce; // moving forward
        else if (vel < -0.02f) friction =  mu * normalForce; // moving backward
        else                   friction = 0;

        float netForce     = gravityAlong + friction;
        float acceleration = netForce / mass; // F = ma → a = F/m

        state.velocity.x     = state.velocity.x + acceleration * dt;
        state.position.x     = state.position.x + state.velocity.x * dt;
        state.acceleration.x = acceleration;

        if (state.position.x < 0) { // clamp at start — can't go behind origin
            state.position.x = 0;
            state.velocity.x = 0;
        }

        // stop when nearly stationary on flat ground
        if (Math.abs(state.velocity.x) < 0.005f && Math.abs(angle) < 0.05f) {
            state.velocity.x     = 0;
            state.acceleration.x = 0;
        }
    }
}