package com.example.rollercoaster_physics;

// holds track properties used by PhysicsEngine each frame
public class Track {

    private float frictionCoeff; // μ — set by SimPanel material buttons
    private float slopeAngle;    // degrees: >0 = Level1, 0 = Level2, -1 = Level3

    // full constructor — used when both values are known upfront
    public Track(float frictionCoeff, float slopeAngle) {
        this.frictionCoeff = frictionCoeff;
        this.slopeAngle    = slopeAngle;
    }

    // default constructor — slopeAngle overridden by Level.setup()
    public Track(String material) {
        this.frictionCoeff = 0.10f; // smooth default
        this.slopeAngle    = 0f;    // overridden by Level1/2/3.setup()
    }

    public float getFrictionCoeff() { return frictionCoeff; }
    public float getSlopeAngle()    { return slopeAngle; }

    public void setFrictionCoeff(float frictionCoeff) { this.frictionCoeff = frictionCoeff; }
    public void setSlopeAngle(float slopeAngle)       { this.slopeAngle    = slopeAngle; }
}
