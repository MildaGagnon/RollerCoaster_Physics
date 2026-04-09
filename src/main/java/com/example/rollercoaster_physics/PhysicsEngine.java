package com.example.rollercoaster_physics;
import com.example.rollercoaster_physics.Cart;
import com.example.rollercoaster_physics.Track;
public class PhysicsEngine {


    private static final float G = 9.81f;  // gravity in m/s²

    private Cart  cart;
    private Track track;

    // ── constructor ─────────────────────────────────────────────
    public PhysicsEngine(Cart cart, Track track) {
        this.cart  = cart;
        this.track = track;
    }

    // ── gravity pulling the ball down the slope ─────────────────
    // formula: Fg_slope = m * g * sin(angle)
    public float getGravityForce() {
        float angleRad = (float) Math.toRadians(track.getSlopeAngle());
        return (float) cart.getMass() * G * (float) Math.sin(angleRad);
    }

    // ── force pushing the ball away from the ramp surface ───────
    // formula: N = m * g * cos(angle)
    public float getNormalForce() {
        float angleRad = (float) Math.toRadians(track.getSlopeAngle());
        return (float) cart.getMass() * G * (float) Math.cos(angleRad);
    }

    // ── friction slowing the ball down ──────────────────────────
    // friction depends on the TRACK material (μ), not the ball
    // formula: Ff = μ * N  (opposes motion, so negative)
    public float getFrictionForce() {
        float mu = track.getFrictionCoeff();   // comes from the track
        float N  = getNormalForce();
        return -mu * N;                        // negative = opposing downhill motion
    }

    // ── one step of the simulation ──────────────────────────────
    // called every frame with a small time step dt (in seconds)
    public void update(float dt) {

        Cart.cartState state = cart.getState();
        float mass = (float) cart.getMass();

        // net force = gravity down slope + friction up slope
        float netForce = getGravityForce() + getFrictionForce();

        // Newton's second law: a = F / m
        float acceleration = netForce / mass;

        // update velocity: v = v + a * dt
        state.velocity.x = state.velocity.x + acceleration * dt;

        // ball cannot roll backwards on level 1
        if (state.velocity.x < 0) {
            state.velocity.x = 0;
        }

        // update position: x = x + v * dt
        state.position.x = state.position.x + state.velocity.x * dt;

        // store acceleration so it can be displayed
        state.acceleration.x = acceleration;
    }
}
