package motiontrail.travel.com;

/**
 * Created by Aaron on 2018/12/28.
 */
public class LocationItem {

    private String mLatitude = null;  //緯度
    private String mLongitude = null;  //經度
    private String mAltitude = null;  //海拔
    private String mAccuracy = null;  //精度
    private String mDate = null;

    public String getLatitude() {
        return mLatitude;
    }

    public void setLatitude(String latitude) {
        mLatitude = latitude;
    }

    public String getLongitude() {
        return mLongitude;
    }

    public void setLongitude(String longitude) {
        mLongitude = longitude;
    }

    public String getAltitude() {
        return mAltitude;
    }

    public void setAltitude(String altitude) {
        mAltitude = altitude;
    }

    public String getAccuracy() {
        return mAccuracy;
    }

    public void setAccuracy(String accuracy) {
        mAccuracy = accuracy;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    @Override
    public String toString() {
        return "{mLatitude:" + mLatitude +
                ",mLongitude:" + mLongitude +
                ",mAltitude:" + mAltitude +
                ",mAccuracy:" + mAccuracy +
                ",mDate:" + mDate +"}";
    }
}
