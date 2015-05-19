package com.arman.efficientqhalgoforch.common;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;


import java.util.*;
import java.util.List;

public class Point2DCloud {

    private List<Point2D> point2Ds;
    private Set<Edge> polygon;
    private HashMap<String, Integer> fieldsMap;
    private HashMap<Integer,String> model;
    private boolean drawEnabled;
    Activity ctx;

    public Point2DCloud(Activity ctx, final int count, final int width, final int height, boolean drawEnabled) {
         this.ctx = ctx;
        this.drawEnabled = drawEnabled;
        try {
                    fieldsMap = new HashMap<String, Integer>();
                    polygon = new HashSet<Edge>();
                    point2Ds = Utils.generateRandomPoints(count, width, height, 8);

        } catch (Exception e) {
        e.printStackTrace();
        }
    }





    public <T> void setField(final String key, final T value) {
        new Runnable() {
            @Override
            public void run() {
                Log.d("setField", key+"  "+ value + " aa");
            }

    };
    }

    public void toast(String msg) {
        Toast.makeText(ctx,msg,Toast.LENGTH_SHORT).show();
    }

    public List<Point2D> getPoints() {
        return new ArrayList<Point2D>(point2Ds);
    }

    public void removeAllEdges(){
        new Runnable() {
            @Override
            public void run() {
                polygon.removeAll(polygon);
            }
        };
    }



    public void addEdge(Edge edge) {
        final Edge edgeCpy = new Edge(edge);
                Point2DCloud.this.polygon.add(edgeCpy);

    }

    public void removeEdge(Edge edge) {
        if (!drawEnabled) {
            return;
        }
        // Thread-safe
        final Edge edgeCpy = new Edge(edge);
        final Edge edgeFlip = new Edge(edge.p2, edge.p1);

        new Runnable() {
            @Override
            public void run() {
                Point2DCloud.this.polygon.remove(edgeCpy);
                Point2DCloud.this.polygon.remove(edgeFlip);

            }
        };
    }


}
