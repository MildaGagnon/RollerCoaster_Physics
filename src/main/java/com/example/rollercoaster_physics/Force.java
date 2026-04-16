package com.example.rollercoaster_physics;

public class Force {

    private double magnitude;   // how strong the force is, in Newtons
    private double directionDeg; // which way it points, in degrees

    // constructor
    public Force(double magnitude, double directionDeg) {
        this.magnitude    = magnitude;
        this.directionDeg = directionDeg;
    }

    // getters
    public double getMagnitude()    { return magnitude; }
    public double getDirectionDeg() { return directionDeg; }

    // setters
    public void setMagnitude(double magnitude)       { this.magnitude    = magnitude; }
    public void setDirectionDeg(double directionDeg) { this.directionDeg = directionDeg; }

    // converts this force into a Vector2D so PhysicsEngine can add forces together
    public Vector2D toVector() {
        double rad = Math.toRadians(directionDeg);
        float x = (float)(magnitude * Math.cos(rad));
        float y = (float)(magnitude * Math.sin(rad));
        return new Vector2D(x, y);
    }
}
