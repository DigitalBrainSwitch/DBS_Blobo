package uk.co.digitalbrainswitch.dbsblobodiary.location;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mingkichong on 15/10/2013.
 */
public class TimeLocation implements Parcelable{

    private long timeInMillisecond = -1;
    private double latitude = -1;
    private double longitude = -1;

    public TimeLocation(Parcel in){
        readFromParcel(in);
    }

    public TimeLocation(long time, double latitude, double longitude) {
        this.timeInMillisecond = time;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public long getTimeInMillisecond() {
        return timeInMillisecond;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(timeInMillisecond);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }

    private void readFromParcel(Parcel in) {
        timeInMillisecond = in.readLong();
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    public static final Parcelable.Creator<TimeLocation> CREATOR =
            new Parcelable.Creator<TimeLocation>(){

                @Override
                public TimeLocation createFromParcel(Parcel source) {
                    return new TimeLocation(source);
                }

                @Override
                public TimeLocation[] newArray(int size) {
                    return new TimeLocation[size];
                }
            };
}
