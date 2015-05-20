package com.arman.efficientqhalgoforch.common;

import android.util.Log;

import com.arman.efficientqhalgoforch.SuperAwesomeCardFragment;
import com.arman.efficientqhalgoforch.external.DoneListener;

import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class ConvexHull implements Runnable {

    private static final Console console = Console.getInstance(ConvexHull.class);
    protected final Point2DCloud pointCloud;
    protected final int pointCount;
    protected final int threads;
    protected final List<Point2D> points;
    protected boolean debug = true;
    protected boolean debugStepThrough = false;
    protected long debugFrameDelay = 1000;
    protected Lock debugStep;
    protected Condition debugStepCondition;
    private boolean active = false;
    private long startTime;
    DoneListener listener;

    public ConvexHull(Point2DCloud pointCloud, int threads,DoneListener listener) {
        this(pointCloud, threads, true,listener);
    }

    public ConvexHull(Point2DCloud pointCloud, int threads, boolean debug,DoneListener listener) {
        this(pointCloud, threads, debug, 1000,listener);
    }

    public ConvexHull(Point2DCloud pointCloud, int threads, final boolean debug, int animationDelay,DoneListener listener) {
        this.pointCloud = pointCloud;
        this.threads = threads;
        this.debug = debug;
        this.debugStepThrough = animationDelay == Integer.MAX_VALUE;
        this.debugFrameDelay = debugStepThrough ? 0 : animationDelay;
        this.points = this.pointCloud.getPoints();
        this.pointCount = points.size();
        this.debugStep = new ReentrantLock();
        this.debugStepCondition = debugStep.newCondition();
        this.listener = listener;


    }

    @Override
    public void run() {
            active = true;
            startTime = System.nanoTime();
            findHull();
            double duration = ((double) (System.nanoTime() - startTime)) / 1000000000.0;
            duration = Math.round(duration * 10000.0) / 10000.0;

            Log.d("Duration  ", String.valueOf(duration));
        if(SuperAwesomeCardFragment.algorithmIndex==2) {
           float dur = new Float(duration);
            pointCloud.toast("Completed QuickHull");
            listener.jobDone(2,dur);

        }
        else if(SuperAwesomeCardFragment.algorithmIndex==3) {
            float dur = new Float(duration);
            pointCloud.toast("Completed GrahamScan");
            listener.jobDone(3,dur);

        }



            active = false;
        }


    protected void delay() {
        if (debug) {

            try {
                Thread.sleep(debugFrameDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected void requestAnimationFrame() {
        if (debugStepThrough) {
            debugStep.lock();
            try {
                debugStepCondition.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected void releaseAnimationFrame() {
        if (debugStepThrough) {
            console.err("Released lock");
            debugStep.unlock();
        }
    }

    protected abstract void findHull();
}
