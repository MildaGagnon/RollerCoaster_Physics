package com.example.rollercoaster_physics;

public class Vector2D {

    public float x;
    public float y;

    // constructor
    public Vector2D(float x, float y) {
        this.x = x;
        this.y = y;
    }

    // adds another vector to this one
    public void add(Vector2D other) {
        this.x = this.x + other.x;
        this.y = this.y + other.y;
    }

    // returns the length of this vector
    public float magnitude() {
        return (float) Math.sqrt(x * x + y * y);
    }
}
