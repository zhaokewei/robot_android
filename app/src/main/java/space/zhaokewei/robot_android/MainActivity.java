package space.zhaokewei.robot_android;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private AMapLocationClient mLocationClient = null;
    private AMapLocationClientOption mLocationOption = null;

    private TextView mTextView;
    private EditText mEditText;

    private double last_longtitude = 0;
    private double last_latitude = 0;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView)findViewById(R.id.textView);
        mEditText = (EditText)findViewById(R.id.editText);
        initAmap();
    }

    @Override
    protected void onDestroy(){
        super .onDestroy();
        if(mLocationClient != null){
            mLocationClient.onDestroy();
            mLocationClient = null;
            mLocationOption = null;
        }
    }

    private void initAmap(){
        mLocationClient = new AMapLocationClient(getApplicationContext());
        mLocationClient.setLocationListener(mAMapLocationListener);

        mLocationOption = new AMapLocationClientOption();
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置定位模式为AMapLocationMode.Battery_Saving，低功耗模式。
        //mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
        //设置定位模式为AMapLocationMode.Device_Sensors，仅设备模式。
        //mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Device_Sensors);

        //本demo采用定时间隔5秒获取方式，所以以下两项单次获取方式相关设置需设为false
        //获取一次定位结果：
        //该方法默认为false。
        mLocationOption.setOnceLocation(false);
        //获取最近3s内精度最高的一次定位结果：
        //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
        mLocationOption.setOnceLocationLatest(false);

        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否允许模拟位置,默认为true，允许模拟位置
        mLocationOption.setMockEnable(true);
        //单位是毫秒，默认30000毫秒，建议超时时间不要低于8000毫秒。
        mLocationOption.setHttpTimeOut(30000);
        mLocationOption.setInterval(5000);//可选，设置定位间隔。默认为2秒
        //关闭缓存机制
        mLocationOption.setLocationCacheEnable(false);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
    }

    private String post_data(final JSONObject data){
        final String path = mEditText.getText().toString();
        android.util.Log.v("req_url", path);
        new Thread(){
            public void run(){
                try{
                    URL url = new URL(path);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(5000);
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type","application/json");
                    String data_str = String.valueOf(data);
                    connection.setRequestProperty("Content-Length", data_str.length()+"");
                    OutputStream os = connection.getOutputStream();
                    os.write(data_str.getBytes());
                    android.util.Log.v("resp", connection.getResponseMessage());
                    os.close();
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

        return null;
    }

    AMapLocationListener mAMapLocationListener = new AMapLocationListener(){
        @Override
        public void onLocationChanged(AMapLocation location) {
            if (location != null) {
                StringBuffer sb = new StringBuffer();
                JSONObject data_post = new JSONObject();
                if (location.getErrorCode() == 0) {
                    //可在其中解析amapLocation获取相应内容。
                    //解析定位结果
                    //Utils.getLocationStr(amapLocation);  //Utils为demo中封装的一个信息解释类

                    sb.append("定位成功" + "\n");
                    sb.append("定位类型: " + location.getLocationType() + "\n");
                    sb.append("经    度    : " + location.getLongitude() + "\n");
                    sb.append("纬    度    : " + location.getLatitude() + "\n");
                    sb.append("精    度    : " + location.getAccuracy() + "米" + "\n");
                    sb.append("提供者    : " + location.getProvider() + "\n");

                    sb.append("海    拔    : " + location.getAltitude() + "米" + "\n");
                    sb.append("速    度    : " + location.getSpeed() + "米/秒" + "\n");
                    sb.append("角    度    : " + location.getBearing() + "\n");
                    if (location.getProvider().equalsIgnoreCase(
                            android.location.LocationManager.GPS_PROVIDER)) {
                        // 以下信息只有提供者是GPS时才会有
                        // 获取当前提供定位服务的卫星个数
                        sb.append("星    数    : "
                                + location.getSatellites() + "\n");
                    }

                    //逆地理信息
                    sb.append("国    家    : " + location.getCountry() + "\n");
                    sb.append("省            : " + location.getProvince() + "\n");
                    sb.append("市            : " + location.getCity() + "\n");
                    sb.append("城市编码 : " + location.getCityCode() + "\n");
                    sb.append("区            : " + location.getDistrict() + "\n");
                    sb.append("区域 码   : " + location.getAdCode() + "\n");
                    sb.append("地    址    : " + location.getAddress() + "\n");
                    sb.append("兴趣点    : " + location.getPoiName() + "\n");
                    //定位完成的时间
                    SimpleDateFormat datefmter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                    sb.append("定位时间: " + datefmter.format(location.getTime()) + "\n");
                    mTextView.setText(sb.toString());

                    if(location.getLongitude() != last_longtitude || location.getLatitude() != last_latitude) {
                        last_latitude = location.getLatitude();
                        last_longtitude = location.getLongitude();
                        try {
                            data_post.put("longtitude", location.getLongitude());
                            data_post.put("latitude", location.getLatitude());
                            data_post.put("type", location.getLocationType());
                            data_post.put("provider", location.getProvider());
                            data_post.put("speed", location.getSpeed());
                            data_post.put("accuracy", location.getAccuracy());
                            data_post.put("bearing", location.getBearing());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        post_data(data_post);
                    }
                    else{
                        android.util.Log.v("status", "in old pos");
                    }


                }else {
                    //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                    Log.e("AmapError","location Error, ErrCode:"
                            + location.getErrorCode() + ", errInfo:"
                            + location.getErrorInfo());
                }
            }
        }
    };
}
