package com.arman.efficientqhalgoforch.common;


import android.graphics.Color;

public class Point2D extends android.graphics.Point {

    public static final int VISITED = Color.BLUE;
    public static final int UNVISITED = (Color.RED);

    private volatile int color = UNVISITED;

    public volatile String debugText = "";

    public Point2D(int a, int i) {
        super(a, i);
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
