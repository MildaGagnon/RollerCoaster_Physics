package com.example.rollercoaster_physics;
import com.example.rollercoaster_physics.Cart;
import com.example.rollercoaster_physics.Track;
public class PhysicsEngine {


    private static final float G = 9.81f;

    private Cart  cart;
    private Track track;

    public PhysicsEngine(Cart cart, Track track) {
        this.cart  = cart;
        this.track = track;
    }

    // ── detects which level we are in based on track slope angle ─
    // Level 1 uses slopeAngle = 25  (straight ramp, fixed angle)
    // Level 2 uses slopeAngle = 0   (parabola, dynamic angle)
    private float getCurrentAngleRad() {

        float storedAngle = track.getSlopeAngle();

        // Level 1 — straight ramp, angle is fixed
        if (storedAngle != 0) {
            return (float) Math.toRadians(storedAngle);
        }

        // Level 2 — parabola, angle is dynamic based on position
        // parabola constants matching Level2Test.java
        double trackXStart  = 60;
        double trackXEnd    = 890;
        double trackMidX    = (trackXStart + trackXEnd) / 2.0;
        double trackBottomY = 340;
        double trackLeftY   = 90;
        double A = (trackLeftY - trackBottomY)
                / ((trackMidX - trackXStart) * (trackMidX - trackXStart));
        double pixelsPerMeter = 2.5;

        // walk along the arc to find current screen x
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

        // dy/dx at this x gives the slope angle
        double dy_dx = 2.0 * A * (x - trackMidX);
        return (float) Math.atan2(dy_dx, 1.0);
    }

    public float getGravityForce() {
        float angleRad = getCurrentAngleRad();
        return (float) cart.getMass() * G * (float) Math.sin(angleRad);
    }

    public float getNormalForce() {
        float angleRad = getCurrentAngleRad();
        return (float) cart.getMass() * G * (float) Math.abs(Math.cos(angleRad));
    }

    public float getFrictionForce() {
        float mu  = track.getFrictionCoeff();
        float N   = getNormalForce();
        float vel = cart.getState().velocity.x;
        // friction always opposes motion direction
        // if ball moves forward (positive) friction is negative
        // if ball moves backward (negative) friction is positive
        if (vel > 0.01f)  return -mu * N;
        if (vel < -0.01f) return  mu * N;
        return 0;
    }

    public void update(float dt) {
        Cart.cartState state = cart.getState();
        float mass = (float) cart.getMass();

        float gravityAlong = getGravityForce();
        float friction     = getFrictionForce();
        float netForce     = gravityAlong + friction;
        float acceleration = netForce / mass;

        state.velocity.x     = state.velocity.x + acceleration * dt;
        state.position.x     = state.position.x + state.velocity.x * dt;
        state.acceleration.x = acceleration;

        // stop the ball if it rolls back past the start
        if (state.position.x < 0) {
            state.position.x = 0;
            state.velocity.x = 0;
        }

        // stop the ball if it slows to nearly zero
        // (friction has killed all energy — ball rests at bottom)
        if (Math.abs(state.velocity.x) < 0.01f && Math.abs(gravityAlong) < 0.05f) {
            state.velocity.x     = 0;
            state.acceleration.x = 0;
        }
    }
}
