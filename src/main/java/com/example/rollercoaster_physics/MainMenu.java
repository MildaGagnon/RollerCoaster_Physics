package com.example.rollercoaster_physics;

// static data class — all strings shown on the welcome and level-select screens
public class MainMenu {

    // welcome screen title
    public static String getTitle() {
        return "ROLLER COASTER";
    }

    // welcome screen subtitle
    public static String getSubtitle() {
        return "Physics Simulator";
    }

    // version tag shown under subtitle
    public static String getEdition() {
        return "[ Educational Physics Engine v1.0 ]";
    }

    // authors line
    public static String getAuthors() {
        return "Milda Gagnon  —  Matias Leheudre  —  Isaiah Sassi";
    }

    // level names for the level-select cards — pulled from each Level class
    public static String[] getLevelNames() {
        String[] names = new String[3];
        names[0] = Level1.getName();
        names[1] = Level2.getName();
        names[2] = Level3.getName();
        return names;
    }

    // level descriptions for the level-select cards
    public static String[] getLevelDescriptions() {
        String[] descriptions = new String[3];
        descriptions[0] = Level1.getDescription();
        descriptions[1] = Level2.getDescription();
        descriptions[2] = Level3.getDescription();
        return descriptions;
    }
}
