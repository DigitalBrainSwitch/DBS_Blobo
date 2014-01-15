package uk.co.digitalbrainswitch.dbsblobodiary.util;

import android.util.Log;

import java.util.Vector;

/**
 * Created by mingkichong on 07/11/2013.
 */
public class SimpleMovingAveragesSmoothing {

    private final static int DEFAULT_WINDOWS_SIZE = 1; //default only one element in the data window

    private int _windowSize = 1;
    private Vector<Float> recentData;

    public SimpleMovingAveragesSmoothing(){
        this(DEFAULT_WINDOWS_SIZE);
    }

    public SimpleMovingAveragesSmoothing(int wSize){
        recentData = new Vector<Float>();
        this.setWindowSize(wSize);
    }

    public float addMostRecentValue(float value){
        if(recentData.size() < _windowSize){
            recentData.add(value);
        }else{
            recentData.removeElementAt(0);
            recentData.add(value);
        }
        return getCurrentAverage();
    }

    public float getCurrentAverage(){
        float average = 0;
        for(float value : recentData){
            average += value;
        }
        return average / recentData.size();
    }

    public void setWindowSize(int newWindowSize){
        this._windowSize = newWindowSize;
        //resize the vector to match the new window size
        while(recentData.size() > _windowSize){
            recentData.removeElementAt(0);
        }
        Log.e("SimpleMovingAveragesSmoothing", "Vector size: " + recentData.size() + " Window size: " + _windowSize);
    }

    public int getWindowSize(){
        return _windowSize;
    }

    public void resetRecentData(){
        recentData.clear();
    }
}
