package com.example.rollercoaster_physics;

public class Level2 {

    private static final float SLOPE_ANGLE = 35;
    private static final float FRICTION    = 0.20f;

    public static void setup(Track track) {
        track.setSlopeAngle(SLOPE_ANGLE);
        track.setFrictionCoeff(FRICTION);
    }

    public static String getName() {
        return "Loop";
    }

    public static String getDescription() {
        return "Centripetal force\nEnergy conservation";
    }
}
