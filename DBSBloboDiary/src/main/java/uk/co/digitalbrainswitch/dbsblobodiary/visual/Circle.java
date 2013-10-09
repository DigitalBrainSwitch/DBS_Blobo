package uk.co.digitalbrainswitch.dbsblobodiary.visual;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import uk.co.digitalbrainswitch.dbsblobodiary.R;

/**
 * Created by mingkichong on 09/10/2013.
 */
public class Circle extends SurfaceView implements SurfaceHolder.Callback {

    private Paint paint;
    private Paint[] paintCircle;
    private Paint[] paintRect;
    private CircleThread thread;
    private int xPosition, yPosition, radius = 0;

    public Circle(Context context) {
        super(context);
        getHolder().addCallback(this);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(getResources().getColor(R.color.gray));
    }

    @Override
    protected void onDraw(Canvas canvas) {}

    public void colorize(Canvas canvas){

//        if (MainActivity.pressure == 0) paint.setColor(getResources().getColor(R.color.gray));
//        else if (MainActivity.pressure > 10000 && MainActivity.pressure <= 16600) paint.setColor(getResources().getColor(R.color.green_1));
//        else if(MainActivity.pressure > 16600 && MainActivity.pressure <= 16700) paint.setColor(getResources().getColor(R.color.green_2));
//        else if(MainActivity.pressure > 16700 && MainActivity.pressure <= 16800) paint.setColor(getResources().getColor(R.color.green_3));
//        else if(MainActivity.pressure > 16800 && MainActivity.pressure <= 16900) paint.setColor(getResources().getColor(R.color.green_4));
//        else if(MainActivity.pressure > 16900 && MainActivity.pressure <= 17000) paint.setColor(getResources().getColor(R.color.green_5));
//        else if(MainActivity.pressure > 17000 && MainActivity.pressure <= 17100) paint.setColor(getResources().getColor(R.color.green_6));
//        else if(MainActivity.pressure > 17100 && MainActivity.pressure <= 17200) paint.setColor(getResources().getColor(R.color.green_7));
//        else if(MainActivity.pressure > 17200 && MainActivity.pressure <= 17300) paint.setColor(getResources().getColor(R.color.green_8));
//        else if(MainActivity.pressure > 17300 && MainActivity.pressure <= 17400) paint.setColor(getResources().getColor(R.color.green_9));
//        else if(MainActivity.pressure > 17400 && MainActivity.pressure <= 17500) paint.setColor(getResources().getColor(R.color.green_10));
//        else if(MainActivity.pressure > 17500 && MainActivity.pressure <= 17600) paint.setColor(getResources().getColor(R.color.yellow_1));
//        else if(MainActivity.pressure > 17600 && MainActivity.pressure <= 17700) paint.setColor(getResources().getColor(R.color.yellow_2));
//        else if(MainActivity.pressure > 17700 && MainActivity.pressure <= 17800) paint.setColor(getResources().getColor(R.color.yellow_3));
//        else if(MainActivity.pressure > 17800 && MainActivity.pressure <= 17900) paint.setColor(getResources().getColor(R.color.yellow_4));
//        else if(MainActivity.pressure > 17900 && MainActivity.pressure <= 18000) paint.setColor(getResources().getColor(R.color.yellow_5));
//        else if(MainActivity.pressure > 18000 && MainActivity.pressure <= 18100) paint.setColor(getResources().getColor(R.color.yellow_6));
//        else if(MainActivity.pressure > 18100 && MainActivity.pressure <= 18200) paint.setColor(getResources().getColor(R.color.yellow_7));
//        else if(MainActivity.pressure > 18200 && MainActivity.pressure <= 18300) paint.setColor(getResources().getColor(R.color.yellow_8));
//        else if(MainActivity.pressure > 18300 && MainActivity.pressure <= 18400) paint.setColor(getResources().getColor(R.color.yellow_9));
//        else if(MainActivity.pressure > 18400 && MainActivity.pressure <= 18500) paint.setColor(getResources().getColor(R.color.yellow_10));
//        else if(MainActivity.pressure > 18500 && MainActivity.pressure <= 18700) paint.setColor(getResources().getColor(R.color.red_1));
//        else if(MainActivity.pressure > 18700 && MainActivity.pressure <= 19000) paint.setColor(getResources().getColor(R.color.red_2));
//        else if(MainActivity.pressure > 19000 && MainActivity.pressure <= 19300) paint.setColor(getResources().getColor(R.color.red_3));
//        else if(MainActivity.pressure > 19300 && MainActivity.pressure <= 19700) paint.setColor(getResources().getColor(R.color.red_4));
//        else if(MainActivity.pressure > 19700) paint.setColor(getResources().getColor(R.color.red_5));

//        switch (MainActivity.hardSqueezeCounter){
//            case 0:
//                for(int i = 0; i < paintCircle.length; i++){
//                    paintCircle[i].setColor(getResources().getColor(R.color.gray));
//                }
//                break;
//
//            case 1:
//                paintCircle[0].setColor(getResources().getColor(R.color.green_1));
//                break;
//
//            case 2:
//                paintCircle[1].setColor(getResources().getColor(R.color.green_6));
//                break;
//
//            case 3:
//                paintCircle[2].setColor(getResources().getColor(R.color.yellow_1));
//                break;
//
//            case 4:
//                paintCircle[3].setColor(getResources().getColor(R.color.yellow_7));
//                break;
//        }

//        switch (MainActivity.longSqueezeCounter){
//            case 0:
//                for(int i = 0; i < paintRect.length; i++){
//                    paintRect[i].setColor(getResources().getColor(R.color.gray));
//                }
//                break;
//            case 1:
//                paintRect[0].setColor(getResources().getColor(R.color.green_1));
//                break;
//            case 2:
//                paintRect[1].setColor(getResources().getColor(R.color.green_4));
//                break;
//            case 3:
//                paintRect[2].setColor(getResources().getColor(R.color.green_7));
//                break;
//            case 4:
//                paintRect[3].setColor(getResources().getColor(R.color.green_10));
//                break;
//            case 5:
//                paintRect[4].setColor(getResources().getColor(R.color.yellow_3));
//                break;
//            case 6:
//                paintRect[5].setColor(getResources().getColor(R.color.yellow_7));
//                break;
//            case 7:
//                paintRect[6].setColor(getResources().getColor(R.color.yellow_10));
//                break;
//        }

        //the big one
        canvas.drawCircle(xPosition, yPosition, radius, paint);
    }

    private final int POS_OFFSET = 80;
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        radius = this.getWidth()/2 - POS_OFFSET;
        xPosition = this.getWidth() - radius - POS_OFFSET;
        yPosition = (this.getHeight() - radius)/2 + POS_OFFSET;

        thread = new CircleThread(getHolder(), this);
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        thread.setRunning(false);

        while (retry){
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
