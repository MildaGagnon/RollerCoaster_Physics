package com.example.rollercoaster_physics;

import javafx.scene.layout.Pane;

public class LevelTestAdapter3 extends LevelTest{

    private Level3Test level3;

    public LevelTestAdapter3(Level3Test level3) {
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
        this.level3 = level3;
    }


    @Override public void start()               { level3.start();                }
    @Override public void stop()                { level3.stop();                 }
    @Override public void reset()               { level3.reset();                }
    @Override public Pane getView()             { return level3.getView();       }
    @Override public MotionData getMotionData() { return level3.getMotionData(); }
}
