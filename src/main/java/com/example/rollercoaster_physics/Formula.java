package com.example.rollercoaster_physics;

public class Formula {

    private String name;
    private String equation;

    // constructor
    public Formula(String name, String equation) {
        this.name     = name;
        this.equation = equation;
    }

    // getters
    public String getName()     { return name; }
    public String getEquation() { return equation; }

    // returns a nicely formatted single line like "F = m x a"
    public String getDisplay() {
        return name + " = " + equation;
    }

    // these are all the formulas used in the simulator
    // RollerCoasterUI calls this to get the list to show in the panel
    public static Formula[] getAllFormulas() {
        Formula[] list = new Formula[7];
        list[0] = new Formula("F",  "m x a");
        list[1] = new Formula("KE", "(1/2) m v²");
        list[2] = new Formula("PE", "m x g x h");
        list[3] = new Formula("Ff", "μ x N");
        list[4] = new Formula("N",  "m x g x cos θ");
        list[5] = new Formula("a",  "g x sin θ  −  μ g x cos θ");
        list[6] = new Formula("Ki + Ui", "Kf + Uf");
        return list;
    }
}
