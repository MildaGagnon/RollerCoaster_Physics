module com.example.rollercoaster_physics {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.desktop;


    opens com.example.rollercoaster_physics to javafx.fxml;
    exports com.example.rollercoaster_physics;

}