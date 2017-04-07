package com.android.lbs;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.R;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SetLocationActivity extends AppCompatActivity {

    @BindView(R.id.bmap_view)
    MapView mBmapView;
    @BindView(R.id.tip)
    TextView mTip;
    @BindView(R.id.tv_location)
    TextView mTvLocation;
    @BindView(R.id.btn_ensure)
    Button mBtnEnsure;

    private BaiduMap baiduMap;
    private LocationClient mLocationClient;
    private MarkerOptions option;
    private Boolean isLoadCurrentLocation = false;
    private String address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_location);
        ButterKnife.bind(this);

        baiduMap = mBmapView.getMap();
        baiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(17));//地图缩放级别为17
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        initLocation();
        setListener();
    }

    private void setListener() {
        mBtnEnsure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(option != null) {
                    Intent intent = new Intent();
                    intent.putExtra("latitude", String.valueOf(option.getPosition().latitude));//纬度
                    intent.putExtra("longitude", String.valueOf(option.getPosition().longitude));//经度
                    intent.putExtra("address", address);//字符串形式的地址
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    Toast.makeText(SetLocationActivity.this, "设置地点失败，请检查手机网络或设置！", Toast.LENGTH_LONG).show();
                }
            }
        });
        baiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {

            @Override
            public boolean onMapPoiClick(MapPoi poi) {

                return false;
            }

            @Override
            public void onMapClick(LatLng point) {
                // Log.d("map click", point.longitude+"，"+point.latitude);
                setMapOverlay(point);
                getInfoFromLAL(point);
            }
        });

        //调用BaiduMap对象的setOnMarkerDragListener方法设置marker拖拽的监听
        baiduMap.setOnMarkerDragListener(new BaiduMap.OnMarkerDragListener() {
            public void onMarkerDrag(Marker marker) {
                //拖拽中

            }

            public void onMarkerDragEnd(Marker marker) {
                //拖拽结束
                Toast.makeText(SetLocationActivity.this, "拖拽结束", Toast.LENGTH_SHORT).show();
                getInfoFromLAL(marker.getPosition());

            }

            public void onMarkerDragStart(Marker marker) {
                //开始拖拽
                marker.getPosition();
                Toast.makeText(SetLocationActivity.this, "开始拖拽", Toast.LENGTH_SHORT).show();

            }
        });
    }


    // 初始化定位参数
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果
        option.setIgnoreKillProcess(false);//可选，默认false，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(option);
        mLocationClient.start();
        Toast.makeText(this, "正在定位...", Toast.LENGTH_SHORT).show();
    }


    // 定位监听
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation loc) {
            if (loc != null && (loc.getLocType() == 161 || loc.getLocType() == 66)) {
                //这里得到BDLocation就是定位出来的信息了
                LatLng point = new LatLng(loc.getLatitude(), loc.getLongitude());
                //  Toast.makeText(SetLocationActivity.this, "获得位置信息", Toast.LENGTH_LONG).show();
                //
                if (!isLoadCurrentLocation) {
                    isLoadCurrentLocation = true;
                    //setMapOverlay(point);
                    baiduMap.clear();
                    BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_openmap_mark);
                    option = new MarkerOptions()    //初始化标记覆盖物
                            .position(point)
                            .icon(bitmap)
                            .zIndex(9)  //设置marker所在层级
                            .draggable(true);  //设置手势拖拽
                    baiduMap.addOverlay(option);
                    baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(point));//设置当前位置在地图显示
                    getInfoFromLAL(point);
                    mLocationClient.stop();//停止监听
                }
                //  mLocationClient.stop();
            } else {
                Toast.makeText(SetLocationActivity.this, "定位失败，请检查手机网络或设置！", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
    }

    // 在地图上添加标注
    private void setMapOverlay(LatLng point) {
/*        latitude = point.latitude;
        longitude = point.longitude;*/

        baiduMap.clear();
        option.position(point);
        baiduMap.addOverlay(option);
/*        baiduMap.clear();
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_openmap_mark);
        OverlayOptions option = new MarkerOptions()
                .position(point)
                .icon(bitmap)
                .zIndex(9)  //设置marker所在层级
                .draggable(true);  //设置手势拖拽
        baiduMap.addOverlay(option);*/
    }

    // 根据经纬度查询位置
    private void getInfoFromLAL(final LatLng point) {
        GeoCoder gc = GeoCoder.newInstance();
        gc.reverseGeoCode(new ReverseGeoCodeOption().location(point));
        gc.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {

            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    Log.e("发起反地理编码请求", "未能找到结果");
                } else {
                    mTvLocation.setText(result.getAddress());
                    Toast.makeText(SetLocationActivity.this, "经度：" + point.longitudeE6 + "，纬度" + point.latitudeE6
                            + "\n" + result.getAddress(), Toast.LENGTH_LONG).show();
                    address = result.getAddress();
                }
            }

            @Override
            public void onGetGeoCodeResult(GeoCodeResult result) {

            }
        });
    }
}
