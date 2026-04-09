package com.example.rollercoaster_physics;

public class SimulationEngine {

    private Cart cart;
    private Track track;

    private float position = 0;
    private float time = 0;

    private boolean running = false;
    private static final float g = 9.81f;

    public SimulationEngine(Cart cart, Track track) {
        this.cart = cart;
        this.track = track;
    }

    public void update(float dt) {
        if (!running) return;

        // simple slope acceleration
        float angle = (float) Math.toRadians(track.getSlopeAngle());
        float acceleration = g * (float) Math.sin(angle);

        float newVelocity = (float) (cart.getVelocity() + acceleration * dt);
        cart.setVelocity(newVelocity);

        time += dt;
    }

    public void start() {
        running = true;
    }

    public void stop() {
        running = false;
    }

    public float getTime() {
        return time;
    }
}
