package com.example.rollercoaster_physics;

public class PhysicsEngine {

    private static final float G = 9.81f;

    // Level 3 loop constants — must match Level3Test.java EXACTLY
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
    private static final double L3_PPM           = 35;

    private static final double L3_SLOPE_LEN;
    private static final double L3_LOOP_CIRC;

    static {
        L3_SLOPE_LEN = Math.sqrt(
                (L3_SLOPE_END_X - L3_SLOPE_START_X) * (L3_SLOPE_END_X - L3_SLOPE_START_X) +
                        (L3_SLOPE_END_Y - L3_SLOPE_START_Y) * (L3_SLOPE_END_Y - L3_SLOPE_START_Y));
        L3_LOOP_CIRC = 2 * Math.PI * L3_LOOP_RADIUS;
    }

    private Cart  cart;
    private Track track;

    public PhysicsEngine(Cart cart, Track track) {
        this.cart  = cart;
        this.track = track;
    }

    // returns the slope angle at the ball's current position
    private float getCurrentAngleRad() {
        float storedAngle = track.getSlopeAngle();

        // Level 1 — straight ramp
        if (storedAngle > 0) {
            return (float) Math.toRadians(storedAngle);
        }

        // Level 3 — loop track (slopeAngle = -1)
        if (storedAngle < 0) {
            double px = cart.getState().position.x * L3_PPM;
            if (px <= L3_SLOPE_LEN) {
                return (float) Math.atan2(
                        L3_SLOPE_END_Y - L3_SLOPE_START_Y,
                        L3_SLOPE_END_X - L3_SLOPE_START_X);
            }
            px -= L3_SLOPE_LEN;
            if (px <= L3_LOOP_CIRC) {
                double angle = (Math.PI / 2) - (px / L3_LOOP_RADIUS);
                return (float)(angle - Math.PI / 2);
            }
            return 0f;
        }

        // Level 2 — parabola (slopeAngle = 0)
        double trackXStart  = 60;
        double trackXEnd    = 790;
        double trackMidX    = (trackXStart + trackXEnd) / 2.0;
        double trackBottomY = 340;
        double trackLeftY   = 90;
        double A = (trackLeftY - trackBottomY)
                / ((trackMidX - trackXStart) * (trackMidX - trackXStart));
        double pixelsPerMeter = 2.5;

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
        double dy_dx = 2.0 * A * (x - trackMidX);
        return (float) Math.atan2(dy_dx, 1.0);
    }


    public void update(float dt) {
        Cart.cartState state = cart.getState();
        float mass    = (float) cart.getMass();
        float angle   = getCurrentAngleRad();
        float mu      = track.getFrictionCoeff();

        // gravity component along the track
        // positive angle = uphill to the right → gravity opposes forward motion
        // negative angle = downhill to the right → gravity aids forward motion
        float gravityAlong = (float)(mass * G * Math.sin(angle));

        // normal force
        float normalForce = (float)(mass * G * Math.abs(Math.cos(angle)));

        // friction always opposes direction of motion
        float vel = state.velocity.x;
        float friction;
        if      (vel >  0.02f) friction = -mu * normalForce;
        else if (vel < -0.02f) friction =  mu * normalForce;
        else                   friction = 0;

        float netForce    = gravityAlong + friction;
        float acceleration = netForce / mass;

        state.velocity.x     = state.velocity.x + acceleration * dt;
        state.position.x     = state.position.x + state.velocity.x * dt;
        state.acceleration.x = acceleration;

        // clamp at start
        if (state.position.x < 0) {
            state.position.x = 0;
            state.velocity.x = 0;
        }

        // stop if nearly stationary at flat ground (angle ≈ 0)
        if (Math.abs(state.velocity.x) < 0.005f && Math.abs(angle) < 0.05f) {
            state.velocity.x     = 0;
            state.acceleration.x = 0;
        }
    }
}