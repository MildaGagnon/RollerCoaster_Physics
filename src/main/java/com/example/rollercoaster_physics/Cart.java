package com.example.rollercoaster_physics;

public class Cart {

    // attributes
    private double    mass;
    private double    velocity;
    private String    shape;
    private cartState state;
    private Material  material;

    // constructor
    public Cart(double mass, double velocity, String shape,
                Material material, cartState initialState) {
        this.mass     = mass;
        this.velocity = velocity;
        this.shape    = shape;
        this.material = material;
        this.state    = initialState;
    }

    // getters
    public double    getMass()     { return mass; }
    public double    getVelocity() { return velocity; }
    public cartState getState()    { return state; }

    // setters
    public void setMass(double mass)         { this.mass     = mass; }
    public void setVelocity(double velocity) { this.velocity = velocity; }

    // ── inner class: stores position, velocity, acceleration ──────
    public static class cartState {

        public Vector2D position;
        public Vector2D velocity;
        public Vector2D acceleration;

        public cartState(Vector2D position, Vector2D velocity,
                         Vector2D acceleration) {
            this.position     = position;
            this.velocity     = velocity;
            this.acceleration = acceleration;
        }
    }

    // ── inner class: material properties ──────────────────────────
    public static class Material {

        String name;
        float  frictionCoeff;

        public Material(String name, float frictionCoeff) {
            this.name         = name;
            this.frictionCoeff = frictionCoeff;
        }
    }

}
