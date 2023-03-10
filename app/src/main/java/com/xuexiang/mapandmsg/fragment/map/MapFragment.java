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

    private static final String INIT_DISTANCE_LEVEL = "??????";
    private static final int UPDATE_MARKERS_FIRST = 10001;
    ImageButton imageButtonMode;
    ImageButton imageButtonLocate;
    ImageButton imageButton_release;
    ImageButton imageButton_add;
    ImageButton imageButton_subtract;
    ImageButton imageButton_distance;
    SuperButton superButton_now;
    FloatingActionButton floatingActionButtonSearch;
    private String data = "??????";
    private MapView mapView;
    private AMap mAMap;
    private LocationTask mLocationTask;
    private RegeocodeTask mRegeocodeTask;
    /**
     * ????????????
     */
    private WalkRouteOverlay mWalkRouteOverlay;
    private WalkRouteTask mWalkRouteTask;

    private final ArrayList<Marker> markerArrayList = new ArrayList<>();
    /**
     * ??????????????????position
     */
    private PositionEntity mNowPosition;
    /**
     * ???????????????????????????????????????
     */
    private PositionEntity mCenterPosition;
    /**
     * ??????????????????????????????
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
     * @return ????????? null????????????????????????
     */
    @Override
    protected TitleBar initTitle() {
        return null;
    }

    /**
     * ???????????????id
     *
     * @return int
     */
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_map;
    }

    /**
     * ???????????????
     */
    @Override
    protected void initViews() {
    }

    /**
     * ?????????????????????
     */
    private void initMapView() throws Exception {
        AMapLocationClient.updatePrivacyAgree(getContext(),true);
        AMapLocationClient.updatePrivacyShow(getContext(),true,true);
        if (mAMap == null) {
            mAMap = mapView.getMap();
        }
        //????????????????????????????????????????????????
        moveSpecifyLatLng(new LatLng(30.232982, 120.046609));
        //?????????????????????
        initMapWidget();
        //??????????????????????????????
        mRegeocodeTask = new RegeocodeTask(getContext());
        //?????????????????????
        mLocationTask = LocationTask.getInstance(getContext());
        //???????????????????????????
        mWalkRouteTask = WalkRouteTask.getInstance(getContext());

        //?????????????????????????????????????????????
        mLocationTask.startSingleLocate();
    }

    /**
     * ????????????????????????
     */
    private void initMapListener() {
        //??????????????????
        mAMap.setOnMapLoadedListener(this);
        //??????????????????
        mAMap.setOnCameraChangeListener(this);
        //??????????????????
        mLocationTask.setOnLocationGetListener(this);
        //?????????????????????
        mRegeocodeTask.setOnLocationGetListener(this);
        //??????????????????
        mWalkRouteTask.addRouteCalculateListener(this);
        //Maker?????????
        initMakerListener();
        //?????????InfoWindow??????
        mAMap.setInfoWindowAdapter(this);
    }

    /**
     * Maker?????????????????????
     */
    private void initMakerListener() {
        // ?????? Marker ??????????????????
        // marker ?????????????????????????????????
// ?????? true ?????????????????????????????????????????????false
        AMap.OnMarkerClickListener markerClickListener = marker -> {
            clickMarkerId = marker.getId();
            marker.showInfoWindow();
            LatLonPoint start = new LatLonPoint(mNowPosition.latitue, mNowPosition.longitude);
            LatLonPoint end = new LatLonPoint(marker.getPosition().latitude,
                    marker.getPosition().longitude);
            mWalkRouteTask.search(start, end);

            return true;
        };
        // ?????? Marker ???????????????
        mAMap.setOnMarkerClickListener(markerClickListener);

        //???????????????????????????
        AMap.OnInfoWindowClickListener listener = arg0 -> openNewPage(ShowNewInfoFragment.class, ShowNewInfoFragment.KEY_MAP_DATA_TO_SHOW,
                arg0.getObject());
        //???????????????????????????
        mAMap.setOnInfoWindowClickListener(listener);

    }


    /**
     * ???????????????????????????
     */
    private void initMapWidget() {
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        //???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        myLocationStyle.interval(2000);
        //??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
        //??????????????????????????????????????????????????????????????????????????????????????????????????????????????????false????????????????????????????????????????????????????????????????????????????????????
        myLocationStyle.showMyLocation(true);
        //?????????????????????Style
        mAMap.setMyLocationStyle(myLocationStyle);
        // ?????????true?????????????????????????????????false??????????????????????????????????????????????????????false???
        mAMap.setMyLocationEnabled(true);

        //?????????UiSettings?????????
        UiSettings mUiSettings = mAMap.getUiSettings();
        //?????????
        mUiSettings.setCompassEnabled(true);
        //????????????
        mUiSettings.setMyLocationButtonEnabled(false);
        // ????????????????????????????????????
        mAMap.setMyLocationEnabled(true);
        //?????????
        mUiSettings.setScaleControlsEnabled(true);
        //????????????
        mUiSettings.setZoomControlsEnabled(false);
    }


    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = View.inflate(getActivity(), getLayoutId(), null);
        mapView = view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);

        //????????????
        initMyView(view);
        //????????????
        try {
            initMapView();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //???????????????
        initMapListener();
        NewInfoTask mNewInfoTask = NewInfoTask.getInstance();
        mNewInfoTask.setOnDataListener(this);
        CallBack.getInstance().setOnClickPoiItemListener(this);
        return view;
    }



    /**
     * Android???????????????
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
        //???activity??????onDestroy?????????mMapView.onDestroy()???????????????
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
        //???activity??????onResume?????????mMapView.onResume ()???????????????????????????
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        //???activity??????onPause?????????mMapView.onPause ()????????????????????????
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //???activity??????onSaveInstanceState?????????mMapView.onSaveInstanceState (outState)??????????????????????????????
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
                // ???????????????????????????aMap???????????????????????????
                mAMap.setMapType(AMap.MAP_TYPE_NORMAL);
                break;
            case R.id.fab_navigation:
                // ???????????????????????????aMap???????????????????????????
                mAMap.setMapType(AMap.MAP_TYPE_BUS);
                break;
            case R.id.fab_night:
                // ???????????????????????????aMap???????????????????????????
                mAMap.setMapType(AMap.MAP_TYPE_NIGHT);
                break;
            case R.id.fab_satellite:
                // ???????????????????????????aMap???????????????????????????
                mAMap.setMapType(AMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.map_locatenow_ib:
                if (mLocationTask != null) {
                    mLocationTask.startSingleLocate();
                    imageButtonLocate.setImageResource(R.drawable.ic_baseline_my_location_24_blue);
                    if (mWalkRouteOverlay != null) {
                        //????????????
                        mWalkRouteOverlay.removeFromMap();
                    }
                    //??????InfoWindow
                    closeAllMarkerInfoWindow();
                }
                break;
            case R.id.map_release_ib:
                //???????????????
                mNewsPosition = mCenterPosition;
                showDialog();
                break;
            case R.id.map_nowlocation_sb:
                Push.pushAll("????????????");

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
                showEditSpinnerDialog(getContext(), "?????????????????????", data, ResUtils.getStringArray(R.array.sort_mode_distance), value -> {
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
     * ???????????????????????????????????????
     */
    private void moveSpecifyLatLng(LatLng latLng) {
        //???????????????????????????????????????????????????????????????????????????????????????????????????0??~45??????????????????????????0??????????????? 0~360?? (????????????0)
        CameraPosition cameraPosition = new CameraPosition(latLng, (float) 18, 0, 0);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        mAMap.moveCamera(cameraUpdate);
    }

    /**
     * ???????????????????????????
     */
    private void showDialog() {
        String address = mCenterPosition.getAddress();
        String address_latitude_longitude = address
                + "|" + mNewsPosition.latitue
                + "|" + mNewsPosition.longitude;
        String str = "????????????????????????<" + address + ">???????????????";
        DialogLoader.getInstance().showConfirmDialog(
                getContext(),
                getString(R.string.lab_tip_title),
                str,
                getString(R.string.lab_yes),
                (dialog, which) -> {
                    XToastUtils.toast("??????????????????");
                    dialog.dismiss();
                    //????????????????????????????????????????????????
                    openNewPage(NewInfoReleaseFragment.class,
                            NewInfoReleaseFragment.KEY_ADDRESS_LATITUDE_LONGITUDE,
                            address_latitude_longitude);
                },
                getString(R.string.lab_no),
                (dialog, which) -> {
                    XToastUtils.toast("??????????????????");
                    dialog.dismiss();
                }
        );
    }


    /**
     * ????????????
     *
     * @return
     */
    @Override
    public PositionEntity onLocationGet(PositionEntity entity) {
        if (entity != null) {
            //????????????????????????????????????
            MyNowPosition.myNowPositionEntity = entity;
            mNowPosition = entity;
            LatLng latLng = new LatLng(entity.latitue, entity.longitude);
            float zoo = mAMap.getCameraPosition().zoom;
            moveCenter(latLng, zoo);
        } else {
            XToastUtils.error("???????????????");
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
     * ?????????????????????
     */
    @Override
    public void onRegecodeGet(PositionEntity entity) {
        if (entity != null) {
            //??????????????????????????????????????????
            mCenterPosition = entity;
            if (entity.getAddress() != null) {
                superButton_now.setText(entity.getAddress());
            } else {
                XToastUtils.error("onRegecodeGet_address is null");
            }
        } else {
            XToastUtils.error("????????????????????????");
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
     * ??????????????????
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
        //??????marker??????????????????
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
     * ????????????????????????
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
     * ???????????????????????????????????????????????????
     * @param latitude ??????
     * @param longitude ??????
     * @param latitude1 ??????1
     * @param longitude1 ??????1
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
     * ??????????????????marker?????????avObject???id?????????marker???object??????
     *
     * @param avObject avObject???????????????
     */
    private void drawMarker(AVObject avObject) {
        AVGeoPoint avGeoPoint = (AVGeoPoint) avObject.get("location");
        //??????????????????
        LatLng latLng = new LatLng(avGeoPoint.getLatitude(), avGeoPoint.getLongitude());
        MarkerOptions mMarkerOptions = new MarkerOptions();
        mMarkerOptions.position(latLng);
        mMarkerOptions.draggable(false);
        mMarkerOptions
                .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                        .decodeResource(getResources(),
                                R.drawable.messa2)));
        Marker mMarker = mAMap.addMarker(mMarkerOptions);
        //???NewsInfo???Id??????????????????marker???
        mMarker.setObject(avObject.getObjectId());
        String title = (String) avObject.get("title");
        mMarker.setTitle(title);
        mMarker.showInfoWindow();
        //Marker??????list
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
        //???list???map?????????marker
        removeMarkerByIdFromList(newsInfoId);
        //??????????????????
        if (mWalkRouteOverlay != null) {
            mWalkRouteOverlay.removeFromMap();
        }
    }

    /**
     * ???list??????????????????marker
     *
     * @param newSInfoId ?????????
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
     * ???list??????Id??????Marker
     *
     * @param markerId ?????????id
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
            String dis = String.format("%.1f???", distance);
            if (marker != null) {
                marker.setSnippet(dur + "|" + dis);
            }

        }

        WalkPath walkPath = walkRouteResult.getPaths().get(0);
        WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(getContext(), mAMap, walkPath,
                walkRouteResult.getStartPos(), walkRouteResult.getTargetPos());
        if (mWalkRouteOverlay != null) {
            //??????DriveLineOverlay?????????????????????
            mWalkRouteOverlay.removeFromMap();
        }
        //????????????????????????????????????
        walkRouteOverlay.addToMap();
        //??????????????????????????????
        walkRouteOverlay.zoomToSpan();
        //??????
        mWalkRouteOverlay = walkRouteOverlay;


    }

    /**
     * ????????????marker???infoWindow
     */
    private void closeAllMarkerInfoWindow() {
        for (Marker marker : markerArrayList) {
            marker.hideInfoWindow();
        }
    }

    @Override
    public void onGetClickPoiItem(PoiItem poiItem) {
        if (mWalkRouteOverlay != null) {
            //????????????
            mWalkRouteOverlay.removeFromMap();
        }
        //??????InfoWindow
        closeAllMarkerInfoWindow();
        //?????????search???
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
     * ?????????infowindow??????
     */
    public void render(Marker marker, View view) {

        String title = marker.getTitle();
        TextView titleUi = view.findViewById(R.id.info_window_title);
        titleUi.setText(title);

        String snippet = marker.getSnippet();
        String[] strings = snippet.split("\\|");
        //??????
        TextView snippetUi1 =  view.findViewById(R.id.info_window_snippet1);
        snippetUi1.setText(strings[0]);
        //??????
        TextView snippetUi2 =  view.findViewById(R.id.info_window_snippet2);
        snippetUi2.setText(strings[1]);

    }

    /**
     * ??????spinner????????????
     *
     * @param context ?????????
     * @param title ??????
     * @param defaultItems ?????????
     * @param listener ?????????
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
