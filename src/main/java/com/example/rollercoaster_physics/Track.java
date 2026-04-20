package com.example.rollercoaster_physics;

public class Track {

    // attributes
    private float  frictionCoeff;
    private float  slopeAngle;

    // full constructor
    public Track(float frictionCoeff, float slopeAngle) {
        this.frictionCoeff = frictionCoeff;
        this.slopeAngle    = slopeAngle;
    }

    // constructor used by RollerCoasterUI — now sets safe defaults
    public Track(String material) {
        this.frictionCoeff = 0.10f;  // smooth by default
        this.slopeAngle    = 0f;     // Level1.setup() will override this
    }

    // getters
    public float  getFrictionCoeff() { return frictionCoeff; }
    public float  getSlopeAngle()    { return slopeAngle; }

    // setters
    public void setFrictionCoeff(float frictionCoeff) { this.frictionCoeff = frictionCoeff; }
    public void setSlopeAngle(float slopeAngle)       { this.slopeAngle    = slopeAngle; }

}
