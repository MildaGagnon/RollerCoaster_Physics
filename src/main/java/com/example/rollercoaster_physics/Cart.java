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
    public String    getShape()    { return shape; }
    public cartState getState()    { return state; }
    public Material  getMaterial() { return material; }

    // setters
    public void setMass(double mass)         { this.mass     = mass; }
    public void setVelocity(double velocity) { this.velocity = velocity; }
    public void setShape(String shape)       { this.shape    = shape; }
    public void setMaterial(Material mat)    { this.material = mat; }

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

    // ── inner class: shapes ────────────────────────────────────────
    public static class Shape {
        String name;
        float  momentOfInertia;

        public Shape(String name, float momentOfInertia) {
            this.name            = name;
            this.momentOfInertia = momentOfInertia;
        }
    }

    public static class Sphere extends Shape {
        float radius;
        public Sphere(float radius) {
            super("Sphere", (2f / 5f) * radius * radius);
            this.radius = radius;
        }
    }

    public static class Cylinder extends Shape {
        float radius;
        public Cylinder(float radius) {
            super("Cylinder", (1f / 2f) * radius * radius);
            this.radius = radius;
        }
    }

    public static class RectanglePrism extends Shape {
        float width;
        float height;
        public RectanglePrism(float width, float height) {
            super("RectanglePrism",
                    (1f / 12f) * (width * width + height * height));
            this.width  = width;
            this.height = height;
        }
    }
}
