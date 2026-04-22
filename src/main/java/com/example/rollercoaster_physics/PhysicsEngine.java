package com.example.rollercoaster_physics;

// PhysicsEngine calculates how the cart moves each frame.
// It uses only three basic physics formulas:
//
//   1. Acceleration on a slope:  a = g*sin(angle) - mu*g*cos(angle)
//   2. Velocity update:          v = v + a * dt
//   3. Position update:          x = x + v * dt
//
// The only thing that changes between levels is how we find the slope angle.
// Once we have the angle, the same three formulas apply to all levels.

public class PhysicsEngine {

    private static final float G = 9.81f; // gravity in m/s²

    // Level 2 parabola constants — must match Level2Test exactly
    private static final double L2_TRACK_X_START  = 60;
    private static final double L2_TRACK_X_END    = 790;
    private static final double L2_TRACK_MID_X    = (L2_TRACK_X_START + L2_TRACK_X_END) / 2.0;
    private static final double L2_TRACK_BOTTOM_Y = 340;
    private static final double L2_TRACK_LEFT_Y   = 90;
    // A is the parabola coefficient: how curved the track is
    private static final double L2_A = (L2_TRACK_LEFT_Y - L2_TRACK_BOTTOM_Y)
            / ((L2_TRACK_MID_X - L2_TRACK_X_START) * (L2_TRACK_MID_X - L2_TRACK_X_START));
    private static final double L2_PPM = 2.5; // pixels per metre for Level 2

    // Level 3 constants — must match Level3Test exactly
    private static final double L3_SLOPE_LEN;   // slope length in pixels (computed once below)
    private static final double L3_LOOP_RADIUS  = 90;
    private static final double L3_LOOP_CIRC    = 2 * Math.PI * L3_LOOP_RADIUS; // = 2πr
    private static final double L3_SLOPE_START_X = 60,  L3_SLOPE_START_Y = 140;
    private static final double L3_SLOPE_END_X   = 310, L3_SLOPE_END_Y   = 350;
    private static final double L3_PPM           = 35;  // pixels per metre for Level 3

    static {
        // Pythagoras to get the straight-line slope length once at startup
        L3_SLOPE_LEN = Math.sqrt(
                (L3_SLOPE_END_X - L3_SLOPE_START_X) * (L3_SLOPE_END_X - L3_SLOPE_START_X) +
                        (L3_SLOPE_END_Y - L3_SLOPE_START_Y) * (L3_SLOPE_END_Y - L3_SLOPE_START_Y));
    }

    private Cart  cart;
    private Track track;

    public PhysicsEngine(Cart cart, Track track) {
        this.cart  = cart;
        this.track = track;
    }

    // -------------------------------------------------------------------------
    // getCurrentAngleRad()
    //
    // Returns the slope angle (in radians) at the cart's current position.
    // We use track.getSlopeAngle() as a level flag:
    //   slopeAngle > 0  →  Level 1 (straight ramp, fixed angle)
    //   slopeAngle = 0  →  Level 2 (parabola, exact angle from derivative)
    //   slopeAngle < 0  →  Level 3 (slope into loop, three zones)
    // -------------------------------------------------------------------------
    private float getCurrentAngleRad() {

        float slopeAngle = track.getSlopeAngle();

        // ── LEVEL 1: straight ramp ────────────────────────────────────────────
        // Fixed angle the whole way down — just convert degrees to radians.
        if (slopeAngle > 0) {
            return (float) Math.toRadians(slopeAngle);
        }

        // ── LEVEL 3: slope → loop → flat exit ────────────────────────────────
        // Track is split into three zones; each zone has its own angle.
        if (slopeAngle < 0) {
            // cart position is stored in metres; convert to pixels to find the zone
            double px = cart.getState().position.x * L3_PPM;

            // Zone 1 — downhill slope: fixed angle from start point to loop entrance
            if (px <= L3_SLOPE_LEN) {
                return (float) Math.atan2(
                        L3_SLOPE_END_Y - L3_SLOPE_START_Y,
                        L3_SLOPE_END_X - L3_SLOPE_START_X); // fixed slope angle
            }

            // Zone 2 — circular loop
            // Distance into the loop tells us the tangent angle at that point.
            // Tangent angle = angle around the circle minus 90° (tangent is perpendicular to radius).
            double distIntoLoop = px - L3_SLOPE_LEN;
            if (distIntoLoop <= L3_LOOP_CIRC) {
                double theta = (Math.PI / 2) - (distIntoLoop / L3_LOOP_RADIUS);
                return (float)(theta - Math.PI / 2); // tangent = radial angle - 90°
            }

            // Zone 3 — flat exit, no gravity component along the track
            return 0f;
        }

        // ── LEVEL 2: parabolic track ──────────────────────────────────────────
        // position.x for Level 2 is stored as screen X (pixels) not arc metres.
        // This means when the ball rolls backward, screenX decreases correctly
        // and we always get the right angle — the energy bug is fixed here.
        //
        // The parabola equation is y = L2_A * (x - midX)^2 + BOTTOM_Y
        // Its derivative dy/dx = 2 * L2_A * (x - midX) gives the exact slope.
        // angle = arctan(dy/dx) — accurate at every point, in both directions.
        double screenX = cart.getState().position.x; // stored as screen X for Level 2
        screenX = Math.max(L2_TRACK_X_START, Math.min(screenX, L2_TRACK_X_END)); // clamp to track

        double dy_dx = 2.0 * L2_A * (screenX - L2_TRACK_MID_X);
        return (float) Math.atan2(dy_dx, 1.0);
    }

    // -------------------------------------------------------------------------
    // update(dt)
    //
    // Moves the cart forward by one time step dt (seconds, usually ~0.016s).
    //
    //   Step 1 — gravity along slope:  Fg = m * g * sin(angle)
    //   Step 2 — normal force:          N = m * g * cos(angle)
    //   Step 3 — friction force:       Ff = mu * N  (always opposes motion)
    //   Step 4 — acceleration:          a = (Fg + Ff) / m       →  from F = ma
    //   Step 5 — update velocity:       v = v + a * dt
    //   Step 6 — update position:       x = x + v * dt
    // -------------------------------------------------------------------------
    public void update(float dt) {
        Cart.cartState state = cart.getState();
        float mass  = (float) cart.getMass();
        float angle = getCurrentAngleRad();
        float mu    = track.getFrictionCoeff();

        // Step 1 — gravity pulls the cart along the slope: Fg = m*g*sin(angle)
        float gravityAlong = (float)(mass * G * Math.sin(angle));

        // Steps 2 & 3 — normal force and friction: N = m*g*cos(angle), Ff = mu*N
        // Friction always opposes motion so its sign flips with velocity direction
        float normalForce = (float)(mass * G * Math.abs(Math.cos(angle)));
        float friction;
        if      (state.velocity.x >  0.02f) friction = -mu * normalForce; // moving forward
        else if (state.velocity.x < -0.02f) friction =  mu * normalForce; // moving backward
        else                                friction = 0;                  // stopped

        // Step 4 — net force and acceleration: F = ma → a = F/m
        float netForce     = gravityAlong + friction;
        float acceleration = netForce / mass;

        // Step 5 — v = v + a * dt
        state.velocity.x = state.velocity.x + acceleration * dt;

        // Step 6 — x = x + v * dt
        // For Level 2, velocity is in pixels/s so position stays in screen pixels.
        // For Levels 1 and 3, velocity is in m/s so position stays in metres.
        state.position.x = state.position.x + state.velocity.x * dt;

        // save acceleration so the UI can display it
        state.acceleration.x = acceleration;

        // cart cannot go behind the starting point
        if (state.position.x < 0) {
            state.position.x = 0;
            state.velocity.x = 0;
        }

        // stop the cart if it is barely moving on flat ground
        // prevents tiny rounding errors from jiggling the cart forever
        if (Math.abs(state.velocity.x) < 0.005f && Math.abs(angle) < 0.05f) {
            state.velocity.x     = 0;
            state.acceleration.x = 0;
        }
    }
}