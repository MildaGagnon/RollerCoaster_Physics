package com.example.rollercoaster_physics;

public class MainMenu {

    // the title shown at the top of the welcome screen
    public static String getTitle() {
        return "ROLLER COASTER";
    }

    // the subtitle shown below the title
    public static String getSubtitle() {
        return "Physics Simulator";
    }

    // the version tag shown under the subtitle
    public static String getEdition() {
        return "[ Educational Physics Engine v1.0 ]";
    }

    // the authors line
    public static String getAuthors() {
        return "Milda Gagnon  —  Matias Leheudre  —  Isaiah Sassi";
    }

    // the names of all three levels (used on the level-select screen)
    public static String[] getLevelNames() {
        String[] names = new String[3];
        names[0] = Level1.getName();
        names[1] = Level2.getName();
        names[2] = Level3.getName();
        return names;
    }

    // the descriptions of all three levels
    public static String[] getLevelDescriptions() {
        String[] descriptions = new String[3];
        descriptions[0] = Level1.getDescription();
        descriptions[1] = Level2.getDescription();
        descriptions[2] = Level3.getDescription();
        return descriptions;
    }
}
