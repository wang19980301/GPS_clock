package com.example.lbs1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import java.util.ArrayList;
import java.util.List;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {
    public LocationClient locationClient;
    private EditText editText;
    private MapView mapView;
    private LatLng myLatLng;
    private BaiduMap baiduMap;
    private  String reserveDistance;
    private double distance;
    private Button button1;
    private ImageButton imageButton;
    private boolean isFirstLocate=true;
    private BDLocation mLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationClient=new LocationClient(getApplicationContext());
        locationClient.registerLocationListener(new MyLocationListener());
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        mapView=(MapView)findViewById(R.id.bmapView);
        button1=(Button)findViewById(R.id.button1);
        editText=(EditText)findViewById(R.id.editText);
        imageButton=(ImageButton)findViewById(R.id.imageButton) ;

        baiduMap=mapView.getMap();
        baiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        baiduMap.setTrafficEnabled(true);
        baiduMap.setMyLocationEnabled(true);

        baiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                baiduMap.clear();//用于点一下清除上一个marker
                BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.maker);
                //构建MarkerOption，用于在地图上添加Marker
                OverlayOptions option = new MarkerOptions().position(latLng).icon(bitmap);
                //在地图上添加Marker，并显示
                baiduMap.addOverlay(option);
                distance= DistanceUtil. getDistance(latLng,myLatLng );
                DecimalFormat fmt=new DecimalFormat("0.##");
                Toast.makeText(getApplicationContext(),"设置目的地成功！您距离此处"+fmt.format(distance)+"米！",
                        Toast.LENGTH_SHORT).show();
            }
            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });

        List<String> permissionList=new ArrayList<>();
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.
                ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.
                READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.
                WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!permissionList.isEmpty()){
            String[] permissions=permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }else {
            requestLocation();
        }
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng latLng=new LatLng(mLocation.getLatitude(),mLocation.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(latLng).zoom(16f);
                baiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
        });
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reserveDistance=editText.getText().toString();
                if(distance<Integer.parseInt(reserveDistance)){
                    Intent intent=new Intent(MainActivity.this,SecondActivity.class);
                    startActivity(intent);

                }
            }
        });
    }
    private void navigateTo(BDLocation location){
        if(isFirstLocate){
            isFirstLocate=false;
            Toast.makeText(this,"正在移动到："+location.getAddrStr(),Toast.LENGTH_SHORT).show();
            LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());
            MapStatus.Builder builder = new MapStatus.Builder();
            builder.target(latLng).zoom(16f);
            baiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        }
        MyLocationData.Builder builder=new MyLocationData.Builder();
        builder.latitude(location.getLatitude());
        builder.longitude(location.getLongitude());
        MyLocationData locationData=builder.build();
        baiduMap.setMyLocationData(locationData);
    }
    private void requestLocation(){
        initLocation();
        locationClient.start();
    }
    private void initLocation(){
        LocationClientOption option=new LocationClientOption();
        option.setScanSpan(3000);
        option.setIsNeedAddress(true);
        locationClient.setLocOption(option);
    }
    @Override
    protected void onResume(){
        super.onResume();
        mapView.onResume();
    }
    @Override
    protected void onPause(){
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        locationClient.stop();
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        switch (requestCode){
            case 1:
                if(grantResults.length>0){
                    for(int result:grantResults){
                        if(result!=PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this,"必须同意全部权限才能使用本程序！",Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                }else {
                    Toast.makeText(this,"发生未知错误！",Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            default:
        }
    }

    public class MyLocationListener implements BDLocationListener{
        @Override
        public void onReceiveLocation(BDLocation location) {
            mLocation=location;
//            String currentPosition;
//            currentPosition="";
//            currentPosition+="纬度："+location.getLatitude()+"\n";
//            currentPosition+="经线："+location.getLongitude()+"\n";
//            currentPosition+="国家："+location.getCountry()+"\n";
//            currentPosition+="省："+location.getProvince()+"\n";
//            currentPosition+="市："+location.getCity()+"\n";
//            currentPosition+="区："+location.getDistrict()+"\n";
//            currentPosition+="街道："+location.getStreet()+"\n";
//            currentPosition+="定位方式：";
//            if(location.getLocType()==BDLocation.TypeGpsLocation)
//                currentPosition+="GPS";
//            if(location.getLocType()==BDLocation.TypeNetWorkLocation)
//                currentPosition+="网络";
//            textView.setText(currentPosition);
            if(location.getLocType()==BDLocation.TypeGpsLocation||
                    location.getLocType()==BDLocation.TypeNetWorkLocation){
                navigateTo(location);
            }
            myLatLng=new LatLng(location.getLatitude(),location.getLongitude());
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
    }
}

