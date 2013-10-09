package uk.co.digitalbrainswitch.dbsblobodiary.visual;

import android.graphics.Canvas;
import android.graphics.Color;
import android.view.SurfaceHolder;

/**
 * Created by mingkichong on 09/10/2013.
 */
public class CircleThread extends Thread {

    private long time = 0;
    private final int fps = 20;
    private boolean toRun = false;
    private SurfaceHolder surfaceHolder;
    private Circle circle;

    public CircleThread(SurfaceHolder surfaceHolder, Circle circle) {
        this.surfaceHolder = surfaceHolder;
        this.circle = circle;
    }

    public void setRunning(boolean run){
        toRun = run;
    }

    public void run() {

        Canvas canvas = null;

        while (toRun) {
            synchronized (circle){
                long cTime = System.currentTimeMillis();
                if((cTime - time) <= (1000/fps)){
                    try {
                        canvas = surfaceHolder.lockCanvas(null);
                        synchronized (surfaceHolder) {
                            if(canvas != null){
                                canvas.drawColor(Color.WHITE);
                                circle.colorize(canvas);
                            }
                        }
                    } finally {
                        if (canvas != null) {
                            surfaceHolder.unlockCanvasAndPost(canvas);
                        }
                    }
                }
                time = cTime;
            }
        }
    }
}
