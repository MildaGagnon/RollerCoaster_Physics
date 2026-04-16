package com.example.rollercoaster_physics;

import javafx.scene.layout.Pane;

public class LevelTestAdapter extends LevelTest {

    private Level2Test level2;

    public LevelTestAdapter(Level2Test level2) {
        super(
                new Cart(5, 8, "Sphere",
                        new Cart.Material("Metal", 0.2f),
                        new Cart.cartState(
                                new Vector2D(0, 0),
                                new Vector2D(8, 0),
                                new Vector2D(0, 0))),
                new Track("Metal"),
                new ChartPosition(),
                new ChartVelocity(),
                new ChartAcceleration(),
                new ChartEnergy()
        );
        this.level2 = level2;
    }

    @Override public void start()               { level2.start();                }
    @Override public void stop()                { level2.stop();                 }
    @Override public void reset()               { level2.reset();                }
    @Override public Pane getView()             { return level2.getView();       }
    @Override public MotionData getMotionData() { return level2.getMotionData(); }
}