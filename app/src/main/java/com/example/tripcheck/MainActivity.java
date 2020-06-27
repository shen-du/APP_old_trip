package com.example.tripcheck;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.animation.Animation;
import com.amap.api.maps.model.animation.ScaleAnimation;
import com.amap.api.maps.model.animation.TranslateAnimation;


import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    double latitude=39.705201;
    double longitude=113.710772;

    public AMap aMap;
    public MapView mMapView;
    private UiSettings mUiSettings;
    private CheckBox mStyleCheckbox;
    private SharedPreferences sp;//编辑器

    public Marker PeiQiMarker;//佩奇标签
    public LatLng latLng;//地理位置
    public MarkerOptions markerOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMapView = findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);//创建地图

        MapInit();

        mStyleCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                //isChecked = mStyleCheckbox.isChecked();
                if(isChecked) {
                    sp = getSharedPreferences("isChecked", 0);
                    // 使用编辑器来进行操作
                    SharedPreferences.Editor edit = sp.edit();
                    // 将勾选的状态保存起来
                    edit.putBoolean("choose", true); // 这里的choose就是一个key 通过这个key我们就可以得到对应的值
                    // 最好我们别忘记提交一下
                    edit.commit();
                    aMap.setMapType(AMap.MAP_TYPE_NIGHT);//夜景地图模式
                }else{
                    sp = getSharedPreferences("isChecked", 0);
                    // 使用编辑器来进行操作
                    SharedPreferences.Editor edit = sp.edit();
                    // 将勾选的状态保存起来
                    edit.putBoolean("choose", false); // 这里的choose就是一个key 通过这个key我们就可以得到对应的值
                    // 最好我们别忘记提交一下
                    edit.commit();
                    aMap.setMapType(AMap.MAP_TYPE_NORMAL);// 矢量地图模式
                }
            }
        });
        System.out.println("onCreate");
    }



    /**
     * 初始化AMap对象
     */
    private void MapInit() {
        if (aMap == null) {
            aMap = mMapView.getMap();
            modeEcho();
            mUiSettings =  aMap.getUiSettings();
            mUiSettings.setCompassEnabled(true);//显示指南针
            mUiSettings.setScaleControlsEnabled(true);//显示比例尺
            mUiSettings.setLogoBottomMargin(-69);//隐藏logo

            MyLocationStyle myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类
            myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。

            aMap.moveCamera(CameraUpdateFactory.zoomTo(aMap.getCameraPosition().zoom));
            aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
            aMap.getUiSettings().setMyLocationButtonEnabled(true);//设置默认定位按钮是否显示，非必需设置。
            aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        }
    }

    /**
     * 软件设置回显
     */
    private void modeEcho() {
        mStyleCheckbox = findViewById(R.id.nightmap);
        // 要想回显CheckBox的状态 我们需要取得数据
        // [1] 还需要获得SharedPreferences
        sp = getSharedPreferences("isChecked", 0);
        boolean result = sp.getBoolean("choose", false); // 这里就是开始取值了 false代表的就是如果没有得到对应数据我们默认显示为false
        if(result){
            aMap.setMapType(AMap.MAP_TYPE_NIGHT);//夜景地图模式
        }else{
            aMap.setMapType(AMap.MAP_TYPE_NORMAL);// 矢量地图模式
        }
        // 把得到的状态设置给CheckBox组件
        mStyleCheckbox.setChecked(result);
    }

    /**点击事件
     * 佩奇位置和信息更新
     * @param view
     */
    public void PeiQiLocatedUpdate(View view) {
        if(PeiQiMarker!=null)
            PeiQiMarker.remove();
        //startJumpAnimation(PeiQiMarker,latitude++,longitude++);

        new Thread(){
            @Override
            public void run() {
                try {
                    String urlPath = getString(R.string.map_login_url);
                    URL url = new URL(urlPath);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setConnectTimeout(5000);
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");// 设置发送的数据为表单类型，会被添加到http body当中
                    String data = "post=" +URLEncoder.encode( "getDate", "utf-8")+"&username="+URLEncoder.encode("xiaoge", "utf-8");
                    conn.setRequestProperty("Content-Length", String.valueOf(data.length()));
                    // post的请求是把数据以流的方式写了服务器
                    // 指定请求输出模式
                    conn.setDoOutput(true);
                    conn.getOutputStream().write(data.getBytes());
                    int code = conn.getResponseCode();
                    if (code == 200) {
                        InputStream is = conn.getInputStream();
                        String str = StreamUtils.readStream(is);
                        System.out.println(str);
                        longitude=Double.valueOf(str.substring(str.indexOf("longitude=")+"longitude=".length(),str.indexOf("&latitude")));
                        latitude=Double.valueOf(str.substring(str.indexOf("latitude=")+"latitude=".length(),str.length()));
                        showToastInAnyThread("获取数据成功");
                    } else if(code == 500){
                        showToastInAnyThread("服务器内部错误");
                    }else{
                        showToastInAnyThread("服务器未响应");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showToastInAnyThread("POST请求失败");
                }
            }
        }.start();
        latLng = new LatLng(latitude,longitude);
        markerOption = new MarkerOptions()
                    .position(latLng)
                    .title("用户：佩奇")
                    .snippet("状态：上线\n血氧：")
                    .visible(true)
                    .icon(BitmapDescriptorFactory
                    .fromBitmap(BitmapFactory
                    .decodeResource(getResources(),R.drawable.ic_peiqi_color)))
                    .setFlat(true);//设置marker平贴地图效果

        Animation animation = new ScaleAnimation(0,1,0,1);
        animation.setInterpolator(new LinearInterpolator());
        animation.setDuration(1000);//整个移动所需要的时间

        CameraPosition cameraPosition = new CameraPosition(latLng, aMap.getCameraPosition().zoom, 0, 30);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        aMap.moveCamera(cameraUpdate);

        PeiQiMarker=aMap.addMarker(markerOption);
        PeiQiMarker.setAnimation(animation); /*设置动画*/
        PeiQiMarker.startAnimation();//*开始动画*/
        PeiQiMarker.showInfoWindow();//展示消息
    }

    /**
     * 屏幕中心marker 跳动
     */
    public void startJumpAnimation(Marker marker ,double v, double v1) {

        if (marker != null ) {

            //根据屏幕距离计算需要移动的目标点
            final LatLng latLng = marker.getPosition();
            Point point =  aMap.getProjection().toScreenLocation(latLng);
            point.y -= dip2px(this,125);
            LatLng target = aMap.getProjection()
                    .fromScreenLocation(point);
            //使用TranslateAnimation,填写一个需要移动的目标点
            Animation animation = new TranslateAnimation(target);
            animation.setInterpolator(new Interpolator() {
                @Override
                public float getInterpolation(float input) {
                    // 模拟重加速度的interpolator
                    if(input <= 0.5) {
                        return (float) (0.5f - 2 * (0.5 - input) * (0.5 - input));
                    } else {
                        return (float) (0.5f - Math.sqrt((input - 0.5f)*(1.5f - input)));
                    }
                }
            });
            //整个移动所需要的时间
            animation.setDuration(600);
            //设置动画
            marker.setAnimation(animation);
            //开始动画
            marker.startAnimation();

        } else {
            Log.e("amap","screenMarker is null");
        }
    }
    //dip和px转换
    private static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
    @Override
    protected void onStart() {// 应用程序界面用户可见时调用
        super.onStart();
        System.out.println("onStart");
    }
    @Override
    protected void onResume() {// 应用程序界面获得焦点时间时调用
        super.onResume();
        mMapView.onResume();
        System.out.println("onResume");
    }
    @Override
    protected void onRestart() {// 当界面再次可见时被调用
        super.onRestart();
        System.out.println("onRestart");
    }
    @Override
    protected void onPause() {    // 界面失去焦点时调用
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
        System.out.println("onPause");
    }
    @Override
    protected void onStop() { // 当界面不可见时调用
        super.onStop();
        System.out.println("onStop");
    }
    @Override
    protected void onDestroy() { // 当界面被销毁时调用
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        new Thread(){
            @Override
            public void run() {
                try {
                    String urlPath = getString(R.string.map_login_url);
                    URL url = new URL(urlPath);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setConnectTimeout(5000);
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");// 设置发送的数据为表单类型，会被添加到http body当中
                    String data =  "post=" + URLEncoder.encode( "logout", "utf-8");
                    conn.setRequestProperty("Content-Length", String.valueOf(data.length()));
                    // post的请求是把数据以流的方式写了服务器
                    // 指定请求输出模式
                    conn.setDoOutput(true);
                    conn.getOutputStream().write(data.getBytes());
                    int code = conn.getResponseCode();
                    if (code == 200) {
                        InputStream is = conn.getInputStream();
                        String str = StreamUtils.readStream(is);
                        showToastInAnyThread(str);
                    } else if(code == 500){
                        showToastInAnyThread("服务器内部错误");
                    }else{
                        showToastInAnyThread("服务器未响应");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showToastInAnyThread("POST请求失败");
                }
            }
        }.start();
        System.out.println("onDestroy");
        mMapView.onDestroy();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }
    /**
     * 在任意现成当中都可以调用弹出吐司的方法
     * @param result
     */
    private void showToastInAnyThread(final String result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
