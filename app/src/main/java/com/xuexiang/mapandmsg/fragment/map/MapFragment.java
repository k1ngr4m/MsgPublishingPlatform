/*
 * Copyright (C) 2021 xuexiangjys(xuexiangjys@163.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.xuexiang.mapandmsg.fragment.map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.xuexiang.mapandmsg.R;
import com.xuexiang.mapandmsg.activity.SearchViewActivity;
import com.xuexiang.mapandmsg.amap.MyNowPosition;
import com.xuexiang.mapandmsg.amap.entity.PositionEntity;
import com.xuexiang.mapandmsg.amap.overlay.WalkRouteOverlay;
import com.xuexiang.mapandmsg.amap.task.LocationTask;
import com.xuexiang.mapandmsg.amap.task.OnLocationGetListener;
import com.xuexiang.mapandmsg.amap.task.RegeocodeTask;
import com.xuexiang.mapandmsg.amap.task.WalkRouteTask;
import com.xuexiang.mapandmsg.amap.util.AMapUtil;
import com.xuexiang.mapandmsg.core.BaseFragment;
import com.xuexiang.mapandmsg.fragment.CallBack;
import com.xuexiang.mapandmsg.fragment.Thread.ThreadPool;
import com.xuexiang.mapandmsg.fragment.newInfo.NewInfoReleaseFragment;
import com.xuexiang.mapandmsg.fragment.newInfo.ShowNewInfoFragment;
import com.xuexiang.mapandmsg.fragment.newInfo.task.NewInfoTask;
import com.xuexiang.mapandmsg.leancloud.Push;
import com.xuexiang.mapandmsg.utils.XToastUtils;
import com.xuexiang.mapandmsg.widget.EditSpinnerDialog;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xpage.enums.CoreAnim;
import com.xuexiang.xui.utils.ResUtils;
import com.xuexiang.xui.widget.actionbar.TitleBar;
import com.xuexiang.xui.widget.dialog.DialogLoader;
import com.xuexiang.xui.widget.textview.supertextview.SuperButton;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import butterknife.OnClick;
import cn.leancloud.AVObject;
import cn.leancloud.AVQuery;
import cn.leancloud.types.AVGeoPoint;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * @author jap191
 * @since 2021-5-13 23:10:09
 */
@Page(anim = CoreAnim.none)
public class MapFragment extends BaseFragment implements OnLocationGetListener,
        View.OnClickListener, AMap.OnMapLoadedListener, AMap.OnCameraChangeListener,
        NewInfoTask.OnDataListener, WalkRouteTask.OnWalkRouteListener,
        CallBack.OnClickPoiItemListener, AMap.InfoWindowAdapter {

    private static final String INIT_DISTANCE_LEVEL = "初始";
    private static final int UPDATE_MARKERS_FIRST = 10001;
    ImageButton imageButtonMode;
    ImageButton imageButtonLocate;
    ImageButton imageButton_release;
    ImageButton imageButton_add;
    ImageButton imageButton_subtract;
    ImageButton imageButton_distance;
    SuperButton superButton_now;
    FloatingActionButton floatingActionButtonSearch;
    private String data = "初始";
    private MapView mapView;
    private AMap mAMap;
    private LocationTask mLocationTask;
    private RegeocodeTask mRegeocodeTask;
    /**
     * 路线规划
     */
    private WalkRouteOverlay mWalkRouteOverlay;
    private WalkRouteTask mWalkRouteTask;

    private final ArrayList<Marker> markerArrayList = new ArrayList<>();
    /**
     * 定位时记录下position
     */
    private PositionEntity mNowPosition;
    /**
     * 逆地理编码时记录屏幕中心点
     */
    private PositionEntity mCenterPosition;
    /**
     * 发布消息时记录发布点
     */
    private PositionEntity mNewsPosition;
    private String clickMarkerId;
    private Circle circle;
    com.github.clans.fab.FloatingActionMenu mFloatingActionMenu;
    com.github.clans.fab.FloatingActionButton floatingActionButtonNormal;
    com.github.clans.fab.FloatingActionButton floatingActionButtonSatellite;
    com.github.clans.fab.FloatingActionButton floatingActionButtonNight;
    com.github.clans.fab.FloatingActionButton floatingActionButtonNavigation;
    private final ExecutorService pool = ThreadPool.getThreadPool();
    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == UPDATE_MARKERS_FIRST) {
                initMarkers(-1);

            }
        }
    };

    /**
     * @return 返回为 null意为不需要导航栏
     */
    @Override
    protected TitleBar initTitle() {
        return null;
    }

    /**
     * 布局的资源id
     *
     * @return int
     */
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_map;
    }

    /**
     * 初始化控件
     */
    @Override
    protected void initViews() {
    }

    /**
     * 初始化地图控件
     */
    private void initMapView() throws Exception {
        AMapLocationClient.updatePrivacyAgree(getContext(),true);
        AMapLocationClient.updatePrivacyShow(getContext(),true,true);
        if (mAMap == null) {
            mAMap = mapView.getMap();
        }
        //加载地图时移动到指定经纬度：杭州
        moveSpecifyLatLng(new LatLng(30.232982, 120.046609));
        //初始化地图控件
        initMapWidget();
        //初始化逆地理编码任务
        mRegeocodeTask = new RegeocodeTask(getContext());
        //初始化定位任务
        mLocationTask = LocationTask.getInstance(getContext());
        //初始化路径规划任务
        mWalkRouteTask = WalkRouteTask.getInstance(getContext());

        //定位一次，利用回调记录当前位置
        mLocationTask.startSingleLocate();
    }

    /**
     * 初始化地图监听器
     */
    private void initMapListener() {
        //地图加载监听
        mAMap.setOnMapLoadedListener(this);
        //地图位移监听
        mAMap.setOnCameraChangeListener(this);
        //定位回调监听
        mLocationTask.setOnLocationGetListener(this);
        //逆地理编码监听
        mRegeocodeTask.setOnLocationGetListener(this);
        //路径规划监听
        mWalkRouteTask.addRouteCalculateListener(this);
        //Maker监听器
        initMakerListener();
        //自定义InfoWindow样式
        mAMap.setInfoWindowAdapter(this);
    }

    /**
     * Maker有关的监听事件
     */
    private void initMakerListener() {
        // 定义 Marker 点击事件监听
        // marker 对象被点击时回调的接口
// 返回 true 则表示接口已响应事件，否则返回false
        AMap.OnMarkerClickListener markerClickListener = marker -> {
            clickMarkerId = marker.getId();
            marker.showInfoWindow();
            LatLonPoint start = new LatLonPoint(mNowPosition.latitue, mNowPosition.longitude);
            LatLonPoint end = new LatLonPoint(marker.getPosition().latitude,
                    marker.getPosition().longitude);
            mWalkRouteTask.search(start, end);

            return true;
        };
        // 绑定 Marker 被点击事件
        mAMap.setOnMarkerClickListener(markerClickListener);

        //定义信息窗点击监听
        AMap.OnInfoWindowClickListener listener = arg0 -> openNewPage(ShowNewInfoFragment.class, ShowNewInfoFragment.KEY_MAP_DATA_TO_SHOW,
                arg0.getObject());
        //绑定信息窗点击事件
        mAMap.setOnInfoWindowClickListener(listener);

    }


    /**
     * 初始化地图自带控件
     */
    private void initMapWidget() {
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        myLocationStyle.interval(2000);
        //连续定位、蓝点不会移动到地图中心点，定位点依照设备方向旋转，并且蓝点会跟随设备移动。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
        //设置是否显示定位小蓝点，用于满足只想使用定位，不想使用定位小蓝点的场景，设置false以后图面上不再有定位蓝点的概念，但是会持续回调位置信息。
        myLocationStyle.showMyLocation(true);
        //设置定位蓝点的Style
        mAMap.setMyLocationStyle(myLocationStyle);
        // 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        mAMap.setMyLocationEnabled(true);

        //实例化UiSettings类对象
        UiSettings mUiSettings = mAMap.getUiSettings();
        //指南针
        mUiSettings.setCompassEnabled(true);
        //定位按钮
        mUiSettings.setMyLocationButtonEnabled(false);
        // 可触发定位并显示当前位置
        mAMap.setMyLocationEnabled(true);
        //比例尺
        mUiSettings.setScaleControlsEnabled(true);
        //缩放按钮
        mUiSettings.setZoomControlsEnabled(false);
    }


    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = View.inflate(getActivity(), getLayoutId(), null);
        mapView = view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);

        //安卓控件
        initMyView(view);
        //地图控件
        try {
            initMapView();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //地图监听器
        initMapListener();
        NewInfoTask mNewInfoTask = NewInfoTask.getInstance();
        mNewInfoTask.setOnDataListener(this);
        CallBack.getInstance().setOnClickPoiItemListener(this);
        return view;
    }



    /**
     * Android控件初始化
     */
    private void initMyView(View view) {
        imageButtonMode = view.findViewById(R.id.map_mode_ib);
        imageButtonMode.setOnClickListener(this);
        imageButtonLocate = view.findViewById(R.id.map_locatenow_ib);
        imageButtonLocate.setOnClickListener(this);
        imageButton_release = view.findViewById(R.id.map_release_ib);
        imageButton_release.setOnClickListener(this);
        imageButton_add = view.findViewById(R.id.map_add_ib);
        imageButton_add.setOnClickListener(this);
        imageButton_subtract = view.findViewById(R.id.map_subtract_ib);
        imageButton_subtract.setOnClickListener(this);
        imageButton_distance = view.findViewById(R.id.map_distance_ib);
        imageButton_distance.setOnClickListener(this);
        superButton_now = view.findViewById(R.id.map_nowlocation_sb);
        superButton_now.setOnClickListener(this);
        floatingActionButtonSearch = view.findViewById(R.id.fab);
        floatingActionButtonSearch.setOnClickListener(this);
        mFloatingActionMenu = view.findViewById(R.id.fab_menu);
        floatingActionButtonNormal = view.findViewById(R.id.fab_normal);
        floatingActionButtonNormal.setOnClickListener(this);
        floatingActionButtonNight = view.findViewById(R.id.fab_night);
        floatingActionButtonNight.setOnClickListener(this);
        floatingActionButtonNavigation = view.findViewById(R.id.fab_navigation);
        floatingActionButtonNavigation.setOnClickListener(this);
        floatingActionButtonSatellite = view.findViewById(R.id.fab_satellite);
        floatingActionButtonSatellite.setOnClickListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mapView.onDestroy();
        mLocationTask.onDestroy();
        NewInfoTask.getInstance().onDestroy();
        try {
            WalkRouteTask.getInstance(getContext()).onDestroy();
        } catch (AMapException e) {
            e.printStackTrace();
        }
        CallBack.getInstance().onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mapView.onSaveInstanceState(outState);
    }

    /**
     * onclick
     */
    @SuppressLint("NonConstantResourceId")
    @OnClick({R.id.fab_navigation, R.id.fab_night, R.id.fab_normal, R.id.fab_satellite})
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.map_mode_ib:
                if (mFloatingActionMenu.getVisibility() == View.INVISIBLE) {
                    mFloatingActionMenu.setVisibility(View.VISIBLE);
                } else {
                    mFloatingActionMenu.setVisibility(View.INVISIBLE);
                }
                break;
            case R.id.fab_normal:
                // 设置标准地图模式，aMap是地图控制器对象。
                mAMap.setMapType(AMap.MAP_TYPE_NORMAL);
                break;
            case R.id.fab_navigation:
                // 设置导航地图模式，aMap是地图控制器对象。
                mAMap.setMapType(AMap.MAP_TYPE_BUS);
                break;
            case R.id.fab_night:
                // 设置夜间地图模式，aMap是地图控制器对象。
                mAMap.setMapType(AMap.MAP_TYPE_NIGHT);
                break;
            case R.id.fab_satellite:
                // 设置卫星地图模式，aMap是地图控制器对象。
                mAMap.setMapType(AMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.map_locatenow_ib:
                if (mLocationTask != null) {
                    mLocationTask.startSingleLocate();
                    imageButtonLocate.setImageResource(R.drawable.ic_baseline_my_location_24_blue);
                    if (mWalkRouteOverlay != null) {
                        //关闭路径
                        mWalkRouteOverlay.removeFromMap();
                    }
                    //关闭InfoWindow
                    closeAllMarkerInfoWindow();
                }
                break;
            case R.id.map_release_ib:
                //记录发布点
                mNewsPosition = mCenterPosition;
                showDialog();
                break;
            case R.id.map_nowlocation_sb:
                Push.pushAll("发布消息");

                break;
            case R.id.fab:
                Intent intent = new Intent(getContext(), SearchViewActivity.class);
                intent.putExtra(SearchViewActivity.KEY_POI_LATITUDE, mNowPosition.latitue);
                intent.putExtra(SearchViewActivity.KEY_POI_LONGITUDE, mNowPosition.longitude);
                startActivity(intent);
                break;
            case R.id.map_add_ib:
                addMap();
                break;
            case R.id.map_subtract_ib:
                subtractMap();
                break;
            case R.id.map_distance_ib:
                showEditSpinnerDialog(getContext(), "按距离筛选消息", data, ResUtils.getStringArray(R.array.sort_mode_distance), value -> {
                    data = value;
                    markerArrayList.clear();
                    mAMap.clear();
                    if (INIT_DISTANCE_LEVEL.equals(data)) {
                        initMarkers(-1);
                    } else {
                        initMarkers(Float.parseFloat(data));
                    }
                });
                break;
            default:
                break;
        }
        mFloatingActionMenu.toggle(false);
    }


    /**
     * 移动到指定经纬度和缩放级别
     */
    private void moveSpecifyLatLng(LatLng latLng) {
        //参数依次是：视角调整区域的中心点坐标、希望调整到的缩放级别、俯仰角0°~45°（垂直与地图时为0）、偏航角 0~360° (正北方为0)
        CameraPosition cameraPosition = new CameraPosition(latLng, (float) 18, 0, 0);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        mAMap.moveCamera(cameraUpdate);
    }

    /**
     * 发布任务时的确认框
     */
    private void showDialog() {
        String address = mCenterPosition.getAddress();
        String address_latitude_longitude = address
                + "|" + mNewsPosition.latitue
                + "|" + mNewsPosition.longitude;
        String str = "是否在当前位置：<" + address + ">发布消息？";
        DialogLoader.getInstance().showConfirmDialog(
                getContext(),
                getString(R.string.lab_tip_title),
                str,
                getString(R.string.lab_yes),
                (dialog, which) -> {
                    XToastUtils.toast("请填写消息！");
                    dialog.dismiss();
                    //携带当前地址数据打卡消息发布页面
                    openNewPage(NewInfoReleaseFragment.class,
                            NewInfoReleaseFragment.KEY_ADDRESS_LATITUDE_LONGITUDE,
                            address_latitude_longitude);
                },
                getString(R.string.lab_no),
                (dialog, which) -> {
                    XToastUtils.toast("已拒绝发布！");
                    dialog.dismiss();
                }
        );
    }


    /**
     * 定位回调
     *
     * @return
     */
    @Override
    public PositionEntity onLocationGet(PositionEntity entity) {
        if (entity != null) {
            //用全局变量记录定位的地点
            MyNowPosition.myNowPositionEntity = entity;
            mNowPosition = entity;
            LatLng latLng = new LatLng(entity.latitue, entity.longitude);
            float zoo = mAMap.getCameraPosition().zoom;
            moveCenter(latLng, zoo);
        } else {
            XToastUtils.error("定位失败！");
        }
        return entity;
    }

    private void moveCenter(LatLng latLng, float zoo) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                latLng, zoo);
        mAMap.animateCamera(cameraUpdate);
    }

    private void subtractMap() {
        CameraUpdate cameraUpdate = CameraUpdateFactory.zoomTo(
                mAMap.getCameraPosition().zoom - 1
        );
        mAMap.animateCamera(cameraUpdate);
    }

    private void addMap() {
        CameraUpdate cameraUpdate = CameraUpdateFactory.zoomTo(
                mAMap.getCameraPosition().zoom + 1
        );
        mAMap.animateCamera(cameraUpdate);
    }

    /**
     * 逆地理编码回调
     */
    @Override
    public void onRegecodeGet(PositionEntity entity) {
        if (entity != null) {
            //用全局变量记录地图中点的地点
            mCenterPosition = entity;
            if (entity.getAddress() != null) {
                superButton_now.setText(entity.getAddress());
            } else {
                XToastUtils.error("onRegecodeGet_address is null");
            }
        } else {
            XToastUtils.error("逆地理编码失败！");
        }
    }

    private void initMarkers(float distance) {
        if(MyNowPosition.myNowPositionEntity != null){
            LatLng latLng = new LatLng(MyNowPosition.myNowPositionEntity.latitue, MyNowPosition.myNowPositionEntity.longitude);
            drawCircle(latLng, distance);
        }
        AVQuery<AVObject> avQuery = new AVQuery<>("NewsInfo");
        avQuery.findInBackground().subscribe(new Observer<List<AVObject>>() {
            @Override
            public void onSubscribe(@NotNull Disposable d) {

            }

            @Override
            public void onNext(@NotNull List<AVObject> avObjects) {
                for (AVObject avObject : avObjects) {
                    filterByDistance(avObject, distance);
                }
                drawCenterMarker();
            }

            @Override
            public void onError(@NotNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    private float getDistance(AVGeoPoint avGeoPoint) {
        LatLng latLng1 = new LatLng(avGeoPoint.getLatitude(), avGeoPoint.getLongitude());
        LatLng latLng2 = new LatLng(MyNowPosition.myNowPositionEntity.latitue, MyNowPosition.myNowPositionEntity.longitude);
        return AMapUtils.calculateLineDistance(latLng1, latLng2);
    }

    private void filterByDistance(AVObject avObject, float distance) {
        String location = "location";
        AVGeoPoint avGeoPoint = (AVGeoPoint) avObject.get(location);
        if (distance == -1) {
            drawMarker(avObject);
        } else {
            if (getDistance(avGeoPoint) < distance) {
                drawMarker(avObject);
            }
        }
    }

    /**
     * 地图加载监听
     */
    @Override
    public void onMapLoaded() {
        pool.execute(() -> {
            Message message = new Message();
            message.what = UPDATE_MARKERS_FIRST;
            handler.sendMessage(message);
        });
        drawCenterMarker();


//        moveSpecifyLatLng(new LatLng(mNowPosition.getLatitue(),mNowPosition.getLongitude()));
    }

    private void drawCenterMarker() {
        MarkerOptions markerOptions = new MarkerOptions();
        //设置marker平贴地图效果
        markerOptions.setFlat(false);
        markerOptions.position(new LatLng(0, 0));
        markerOptions
                .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                        .decodeResource(getResources(),
                                R.drawable.ic_baseline_location_on_24)));
        Marker mPositionMark = mAMap.addMarker(markerOptions);
        mPositionMark.setClickable(false);
        mPositionMark.setPositionByPixels(mapView.getWidth() / 2,
                mapView.getHeight() / 2);
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
    }

    /**
     * 地图移动监听回调
     */
    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        LatLng latLng = cameraPosition.target;
        if (latLng != null) {
            mRegeocodeTask.search(latLng.latitude, latLng.longitude);
            if (!judgeIsChangeLocation(latLng.latitude, latLng.longitude, mNowPosition.latitue,
                    mNowPosition.longitude)) {
                imageButtonLocate.setImageResource(R.drawable.ic_baseline_my_location_24_blue);
            } else {
                imageButtonLocate.setImageResource(R.drawable.ic_baseline_my_location_24_gray);
            }
        } else {
            Log.e("Debug:", "cameraPosition.target is null");
        }

    }

    /**
     * 判断屏幕中心点是否偏移了定位所在点
     * @param latitude 纬度
     * @param longitude 经度
     * @param latitude1 纬度1
     * @param longitude1 经度1
     * @return boolean
     */
    private boolean judgeIsChangeLocation(double latitude, double longitude, double latitude1,
                                          double longitude1) {
        double shift = 0.00001;
        double latShift = Math.abs(latitude - latitude1);
        double lngShift = Math.abs(longitude - longitude1);
        return !(latShift < shift) || !(lngShift < shift);
    }

    /**
     * 在地图上画出marker并且把avObject的id记录在marker的object里面
     *
     * @param avObject avObject数据操作类
     */
    private void drawMarker(AVObject avObject) {
        AVGeoPoint avGeoPoint = (AVGeoPoint) avObject.get("location");
        //点标记初始化
        LatLng latLng = new LatLng(avGeoPoint.getLatitude(), avGeoPoint.getLongitude());
        MarkerOptions mMarkerOptions = new MarkerOptions();
        mMarkerOptions.position(latLng);
        mMarkerOptions.draggable(false);
        mMarkerOptions
                .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                        .decodeResource(getResources(),
                                R.drawable.messa2)));
        Marker mMarker = mAMap.addMarker(mMarkerOptions);
        //将NewsInfo的Id记录在对应的marker里
        mMarker.setObject(avObject.getObjectId());
        String title = (String) avObject.get("title");
        mMarker.setTitle(title);
        mMarker.showInfoWindow();
        //Marker存入list
        markerArrayList.add(mMarker);
    }

    private void drawCircle(LatLng latLng, float distance) {
        if (circle != null) {
            circle.remove();
        }
        if (distance > 0) {
            circle = mAMap.addCircle(new CircleOptions().
                    center(latLng).
                    radius(distance).
                    fillColor(Color.argb(30, 0, 0, 255)).
                    strokeColor(Color.argb(30, 0, 0, 255)).
                    strokeWidth(15));
        }
    }

    @Override
    public void onDataGet(String objectId) {
        AVQuery<AVObject> avQuery = new AVQuery<>("NewsInfo");
        avQuery.getInBackground(objectId).subscribe(new Observer<AVObject>() {
            @Override
            public void onSubscribe(@NotNull Disposable d) {

            }

            @Override
            public void onNext(@NotNull AVObject avObject) {
                drawMarker(avObject);
            }

            @Override
            public void onError(@NotNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    @Override
    public void onDeleteNewInfo(String newsInfoId) {
        //从list和map上删除marker
        removeMarkerByIdFromList(newsInfoId);
        //删除路线图层
        if (mWalkRouteOverlay != null) {
            mWalkRouteOverlay.removeFromMap();
        }
    }

    /**
     * 从list和地图上删除marker
     *
     * @param newSInfoId 消息类
     * @return void
     */
    private void removeMarkerByIdFromList(String newSInfoId) {
        for (Marker marker : markerArrayList) {
            String object = (String) marker.getObject();
            if (object.equals(newSInfoId)) {
                marker.remove();
                markerArrayList.remove(marker);
            }
        }
    }

    /**
     * 从list根据Id得到Marker
     *
     * @param markerId 标记点id
     * @return Marker
     */
    private Marker getMarkerByIdFormList(String markerId) {
        for (Marker marker : markerArrayList) {
            if (marker.getId().equals(markerId)) {
                return marker;
            }
        }
        return null;
    }


    @SuppressLint("DefaultLocale")
    @Override
    public void onWalkRouteCalculate(float distance, int duration, WalkRouteResult walkRouteResult) {
        if (!TextUtils.isEmpty(clickMarkerId)) {
            Marker marker = getMarkerByIdFormList(clickMarkerId);
            String dur = AMapUtil.getFriendlyTime(duration);
            String dis = String.format("%.1f米", distance);
            if (marker != null) {
                marker.setSnippet(dur + "|" + dis);
            }

        }

        WalkPath walkPath = walkRouteResult.getPaths().get(0);
        WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(getContext(), mAMap, walkPath,
                walkRouteResult.getStartPos(), walkRouteResult.getTargetPos());
        if (mWalkRouteOverlay != null) {
            //去掉DriveLineOverlay上的线段和标记
            mWalkRouteOverlay.removeFromMap();
        }
        //添加路线添加到地图上显示
        walkRouteOverlay.addToMap();
        //移动镜头到当前的视角
        walkRouteOverlay.zoomToSpan();
        //记录
        mWalkRouteOverlay = walkRouteOverlay;


    }

    /**
     * 关闭所有marker的infoWindow
     */
    private void closeAllMarkerInfoWindow() {
        for (Marker marker : markerArrayList) {
            marker.hideInfoWindow();
        }
    }

    @Override
    public void onGetClickPoiItem(PoiItem poiItem) {
        if (mWalkRouteOverlay != null) {
            //关闭路径
            mWalkRouteOverlay.removeFromMap();
        }
        //关闭InfoWindow
        closeAllMarkerInfoWindow();
        //移动到search点
        LatLonPoint latLonPoint = poiItem.getLatLonPoint();
        LatLng latLng = new LatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude());
        float zoo = mAMap.getCameraPosition().zoom + 1;
        moveCenter(latLng, zoo);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        @SuppressLint("InflateParams") View infoWindow = getLayoutInflater().inflate(
                R.layout.custom_info_window, null);

        render(marker, infoWindow);
        return infoWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    /**
     * 自定义infowindow窗口
     */
    public void render(Marker marker, View view) {

        String title = marker.getTitle();
        TextView titleUi = view.findViewById(R.id.info_window_title);
        titleUi.setText(title);

        String snippet = marker.getSnippet();
        String[] strings = snippet.split("\\|");
        //时间
        TextView snippetUi1 =  view.findViewById(R.id.info_window_snippet1);
        snippetUi1.setText(strings[0]);
        //路程
        TextView snippetUi2 =  view.findViewById(R.id.info_window_snippet2);
        snippetUi2.setText(strings[1]);

    }

    /**
     * 显示spinner编辑弹窗
     *
     * @param context 上下文
     * @param title 标题
     * @param defaultItems 默认值
     * @param listener 监听器
     */
    public static void showEditSpinnerDialog(Context context, String title, String data, String[] defaultItems, EditSpinnerDialog.OnEditListener listener) {
        EditSpinnerDialog.newBuilder(context)
                .setTitle(title).setText(data)
                .setDefaultItems(defaultItems)
                .setOnEditListener(listener)
                .show();
    }

    @Override
    public void onDestroyView() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroyView();
    }

}
