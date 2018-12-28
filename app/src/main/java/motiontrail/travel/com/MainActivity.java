package motiontrail.travel.com;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {

    private ListView list_item;
    private TextView gps_number;
    private TextView mTv = null;
    private String provider = LocationManager.GPS_PROVIDER;
    private LocationManager mLocationManager;
    private LocationAdapter mAdapter = null;

    private List<String> mLocation = new ArrayList<>();
    private int mPermissionsCount = 0;
    private List<String> mLocationItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        list_item = findViewById(R.id.list_item);
        gps_number = findViewById(R.id.gps_number);
        mTv = findViewById(R.id.location_tv);
        mAdapter = new LocationAdapter(this, mLocation);
        list_item.setAdapter(mAdapter);

        requestPermissions();
    }

    private void initDate() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // 获取所有可用的位置提供器
        List<String> providerList = mLocationManager.getProviders(true);
        if (providerList.contains(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
        } else if (providerList.contains(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
        } else if (providerList.contains(LocationManager.PASSIVE_PROVIDER)) {
            provider = LocationManager.PASSIVE_PROVIDER;
        } else {
            // 当没有可用的位置提供器时，弹出Toast提示用户
            Toast.makeText(this, "No location provider to use", Toast.LENGTH_SHORT).show();
            return;
        }
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        Toast.makeText(this, "正在用 " + provider + " 接收位置信息", Toast.LENGTH_SHORT).show();
        Location location = mLocationManager.getLastKnownLocation(mLocationManager.getBestProvider(createFineCriteria(), true));
        if (location != null) {
            showLocation(location);
        }

        mLocationManager.addGpsStatusListener(mGpsListener);
        mLocationManager.requestLocationUpdates(provider, 1000, 1, mLocationListener);
    }

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.i("zhaoyy1111", "===onLocationChanged=== > " + location.toString());
            showLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.i("zhaoyy1111", "===onStatusChanged=== > " + provider + ", status: " + status + ", " + extras);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.i("zhaoyy1111", "===onProviderEnabled=== > " + provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.i("zhaoyy1111", "===onProviderDisabled=== > " + provider);
        }
    };

    private GpsStatus.Listener mGpsListener = new GpsStatus.Listener() {
        @Override
        public void onGpsStatusChanged(int event) {
            switch (event) {
                //卫星状态改变
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    //获取当前状态
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    GpsStatus gpsStatus = mLocationManager.getGpsStatus(null);
                    //获取卫星颗数的默认最大值
                    int maxSatellites = gpsStatus.getMaxSatellites();
                    //获取所有的卫星
                    Iterator<GpsSatellite> iters = gpsStatus.getSatellites().iterator();
                    //卫星颗数统计
                    int count = 0;
                    StringBuilder sb = new StringBuilder();
                    while (iters.hasNext() && count <= maxSatellites) {
                        count++;
                        GpsSatellite s = iters.next();
                        //卫星的信噪比
                        float snr = s.getSnr();
                        sb.append("第").append(count).append("颗").append("：").append(snr).append("\n");
                    }
                    Log.i("zhaoyy1111", "卫星信息：" + sb.toString());
                    gps_number.setText(count + "");
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 检测权限
     */
    private void requestPermissions() {
        RxPermissions rxPermission = new RxPermissions(this);
        rxPermission.requestEach(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.INTERNET)
                .subscribe(new Consumer<Permission>() {
                    @Override
                    public void accept(Permission permission) {
                        if (!permission.granted) {
                            if (permission.name.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                Toast.makeText(MainActivity.this, "请打开读写权限", Toast.LENGTH_SHORT).show();
                            } else if (permission.name.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                                Toast.makeText(MainActivity.this, "请打开位置权限", Toast.LENGTH_SHORT).show();
                            } else if (permission.name.equals(Manifest.permission.INTERNET)) {
                                Toast.makeText(MainActivity.this, "请打开网络权限", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            mPermissionsCount++;
                        }
                        if (mPermissionsCount == 3) {
                            initDate();
                        }
                    }
                });
    }

    private void showLocation(Location location) {
        String currentPosition = "纬度：" + location.getLatitude() + "，经度：" + location.getLongitude()
                + ", 精度：" + location.getAccuracy() + ", 海拔：" + location.getAltitude()
                + ", 方向：" + location.getBearing();
        LocationItem item = new LocationItem();
        item.setLatitude(location.getLatitude() + "");
        item.setLongitude(location.getLongitude() + "");
        item.setAccuracy(location.getAccuracy() + "");
        item.setAltitude(location.getAltitude() + "");
        item.setDate(TimeUtils.timeStamp2Data(System.currentTimeMillis()));
        LogManager.newInstance().printAndSaveLog(item.toString() + ",");

        Toast.makeText(MainActivity.this, currentPosition, Toast.LENGTH_SHORT).show();
        mAdapter.add(currentPosition);
        mTv.setText(currentPosition);
        getAddress(location.getLatitude(), location.getLongitude());
    }

    private Criteria createFineCriteria() {
        Criteria c = new Criteria();
        c.setAccuracy(Criteria.ACCURACY_FINE);//高精度
        c.setAltitudeRequired(true);//包含高度信息
        c.setBearingRequired(true);//包含方位信息
        c.setSpeedRequired(true);//包含速度信息
        c.setCostAllowed(true);//允许付费
        c.setPowerRequirement(Criteria.POWER_HIGH);//高耗电
        return c;
    }

    private class LocationAdapter extends BaseAdapter {

        private List<String> mStrings = new ArrayList<>();
        private Context mContext = null;

        public LocationAdapter(Context context, List<String> list) {
            mContext = context;
            mStrings = list;
        }

        @Override
        public int getCount() {
            return mStrings.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(MainActivity.this, R.layout.view_location_list, null);
            }
            TextView tv = convertView.findViewById(R.id.location_item);
            tv.setText(mStrings.get(position));

            return convertView;
        }

        public void add(String s) {
            mStrings.add(s);
            notifyDataSetChanged();
        }
    }

    /**
     * 顯示地址信息
     * @param latitude
     * @param longitude
     */
    private void getAddress(double latitude, double longitude) {
        Geocoder gc = new Geocoder(MainActivity.this, Locale.getDefault());
        try {
            List<Address> locationList = gc.getFromLocation(latitude, longitude, 1);
            if (locationList != null) {
                Address address = locationList.get(0);
                String countryName = address.getCountryName();
                //国家
                String countryCode = address.getCountryCode();
                String adminArea = address.getAdminArea();
                //省
                String locality = address.getLocality();
                //市
                String subAdminArea = address.getSubAdminArea();
                //区
                String featureName = address.getFeatureName();
                //街道
                for (int i = 0; address.getAddressLine(i) != null; i++) {
                    String addressLine = address.getAddressLine(i);
                    //街道名称:广东省深圳市罗湖区蔡屋围一街深圳瑞吉酒店
                    Log.i("zhaoyy1111", "addressLine: " + addressLine);
                }
                String currentPosition = "latitude is " + latitude//22.545975
                        + "\n" + "longitude is " + longitude//114.101232
                        + "\n" + "countryName is " + countryName//null
                        + "\n" + "countryCode is " + countryCode//CN
                        + "\n" + "adminArea is " + adminArea//广东省
                        + "\n" + "locality is " + locality//深圳市
                        + "\n" + "subAdminArea is " + subAdminArea//null
                        + "\n" + "featureName is " + featureName + " 附近";
                Toast.makeText(MainActivity.this, currentPosition, Toast.LENGTH_SHORT).show();
                Log.i("zhaoyy1111", "currentPosition: " + currentPosition);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
