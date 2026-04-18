package com.example.rollercoaster_physics;

public class Level3 {


    // slopeAngle = -1 signals Level 3 (loop track) to PhysicsEngine
    public static void setup(Track track) {
        track.setSlopeAngle(-1f);
        track.setFrictionCoeff(0.05f);
    }

    public static String getName()        { return "Loop"; }
    public static String getDescription() { return "Slope into full loop\nCentripetal force"; }
}
