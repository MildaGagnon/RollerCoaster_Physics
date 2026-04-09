package com.example.rollercoaster_physics;

import java.util.ArrayList;

public class MotionData {

    // each list stores one value per frame
    private ArrayList<Double> times         = new ArrayList<Double>();
    private ArrayList<Double> positions     = new ArrayList<Double>();
    private ArrayList<Double> velocities    = new ArrayList<Double>();
    private ArrayList<Double> accelerations = new ArrayList<Double>();
    private ArrayList<Double> kineticEnergies   = new ArrayList<Double>();
    private ArrayList<Double> potentialEnergies = new ArrayList<Double>();

    private static final int MAX_POINTS = 400;

    // called every frame to add a new data point
    public void addPoint(double t, double pos, double vel, double acc,
                         double ke, double pe) {
        times.add(t);
        positions.add(pos);
        velocities.add(vel);
        accelerations.add(acc);
        kineticEnergies.add(ke);
        potentialEnergies.add(pe);

        // remove oldest point if we have too many
        if (times.size() > MAX_POINTS) {
            times.remove(0);
            positions.remove(0);
            velocities.remove(0);
            accelerations.remove(0);
            kineticEnergies.remove(0);
            potentialEnergies.remove(0);
        }
    }

    // clears all data (called on reset)
    public void clear() {
        times.clear();
        positions.clear();
        velocities.clear();
        accelerations.clear();
        kineticEnergies.clear();
        potentialEnergies.clear();
    }

    // getters so the chart classes can read the data
    public ArrayList<Double> getTimes()         { return times; }
    public ArrayList<Double> getPositions()     { return positions; }
    public ArrayList<Double> getVelocities()    { return velocities; }
    public ArrayList<Double> getAccelerations() { return accelerations; }
    public ArrayList<Double> getKineticEnergies()   { return kineticEnergies; }
    public ArrayList<Double> getPotentialEnergies() { return potentialEnergies; }

    public int size() { return times.size(); }
}
