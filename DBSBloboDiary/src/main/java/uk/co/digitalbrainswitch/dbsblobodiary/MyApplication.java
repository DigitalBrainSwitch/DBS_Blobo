package uk.co.digitalbrainswitch.dbsblobodiary;

import android.app.Application;
import android.graphics.Typeface;

/**
 * Created by mingkichong on 10/10/2013.
 */
public class MyApplication extends Application{
    private static Typeface customTypeface = null;

    //Create a global singleton typeface font. This prevent creating multiple instances of the same font
    public Typeface getCustomTypeface(){
        if(customTypeface == null){
            customTypeface = Typeface.createFromAsset(getAssets(), "abel.ttf");
        }
        return customTypeface;
    }
}
