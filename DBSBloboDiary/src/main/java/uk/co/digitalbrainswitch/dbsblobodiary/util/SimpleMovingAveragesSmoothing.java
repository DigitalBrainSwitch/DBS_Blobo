package uk.co.digitalbrainswitch.dbsblobodiary.util;

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
        this.setWindowSize(wSize);
        recentData = new Vector<Float>();
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
    }

    public void resetRecentData(){
        recentData.clear();
    }
}
