package com.example.rollercoaster_physics;

public class Track {

    // attributes
    private float  frictionCoeff;
    private float  slopeAngle;
    private float  loopRadius;
    private String material;

    // full constructor
    public Track(float frictionCoeff, float slopeAngle, float loopRadius) {
        this.frictionCoeff = frictionCoeff;
        this.slopeAngle    = slopeAngle;
        this.loopRadius    = loopRadius;
    }

    // constructor used by RollerCoasterUI — now sets safe defaults
    public Track(String material) {
        this.material      = material;
        this.frictionCoeff = 0.10f;  // smooth by default
        this.slopeAngle    = 0f;     // Level1.setup() will override this
        this.loopRadius    = 0f;
    }

    // getters
    public float  getFrictionCoeff() { return frictionCoeff; }
    public float  getSlopeAngle()    { return slopeAngle; }
    public float  getLoopRadius()    { return loopRadius; }
    public String getMaterial()      { return material; }

    // setters
    public void setFrictionCoeff(float frictionCoeff) { this.frictionCoeff = frictionCoeff; }
    public void setSlopeAngle(float slopeAngle)       { this.slopeAngle    = slopeAngle; }
    public void setLoopRadius(float loopRadius)       { this.loopRadius    = loopRadius; }
    public void setMaterial(String material)          { this.material      = material; }

    // physics helpers used by LevelTest and PhysicsEngine
    public float getSlope()  { return (float) Math.sin(Math.toRadians(slopeAngle)); }
    public float getNormal() { return (float) Math.cos(Math.toRadians(slopeAngle)); }
}