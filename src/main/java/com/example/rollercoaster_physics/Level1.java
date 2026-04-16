package com.example.rollercoaster_physics;

public class Level1 {


    private static final float SLOPE_ANGLE = 25;     // degrees
    private static final float FRICTION    = 0.10f;  // smooth track

    // applies the Level 1 settings to the given track
    public static void setup(Track track) {
        track.setSlopeAngle(SLOPE_ANGLE);
        track.setFrictionCoeff(FRICTION);
    }

    public static String getName() {
        return "Basic Slope";
    }

    public static String getDescription() {
        return "Simple incline\nGravity + Friction";
    }
}
