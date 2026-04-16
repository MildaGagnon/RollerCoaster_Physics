package com.example.rollercoaster_physics;

public class Level2 {
    public static void setup(Track track) {
        track.setSlopeAngle(0f);
        track.setFrictionCoeff(0.10f);
    }

    public static String getName()        { return "Parabola"; }
    public static String getDescription() { return "Curved track\nEnergy + Velocity"; }
}
