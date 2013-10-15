package uk.co.digitalbrainswitch.dbsblobodiary.location;

/**
 * Created by mingkichong on 15/10/2013.
 */
public class TimeLocation {

    private long timeInMillisecond = -1;
    private double latitude = -1;
    private double longitude = -1;

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
}
