package com.example.rollercoaster_physics;

public class Level3 {

    private static final float SLOPE_ANGLE = 40;
    private static final float FRICTION    = 0.35f;

    public static void setup(Track track) {
        track.setSlopeAngle(SLOPE_ANGLE);
        track.setFrictionCoeff(FRICTION);
    }

    public static String getName() {
        return "Bumps and Gaps";
    }

    public static String getDescription() {
        return "Complex geometry\nMomentum + Energy";
    }
}
