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

package com.xuexiang.mapandmsg.amap.task;

import android.content.Context;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: WalkRouteTaxk
 * @Description: 封装步行路径规划
 * @Author
 * @Date 2021/5/16
 * @Version 1.0
 */
public class WalkRouteTask implements RouteSearch.OnRouteSearchListener {
    private static WalkRouteTask mWalkRouteTask;
    private RouteSearch mRouteSearch;
    private LatLonPoint fromPoint;
    private LatLonPoint toPoint;
    private List<OnWalkRouteListener> mListeners = new ArrayList<OnWalkRouteListener>();
    public WalkRouteTask(Context context) throws AMapException {
        mRouteSearch = new RouteSearch(context);
        mRouteSearch.setRouteSearchListener(this);
    }

    public static WalkRouteTask getInstance(Context context) throws AMapException {
        if(mWalkRouteTask == null){
            mWalkRouteTask = new WalkRouteTask(context);
        }
        return mWalkRouteTask;
    }

    public void search(LatLonPoint fromPoint,LatLonPoint toPoint){
        this.fromPoint = fromPoint;
        this.toPoint = toPoint;
        search();
    }

    private void search() {
        if(fromPoint == null || toPoint == null){
            return;
        }
        RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(fromPoint,toPoint);
        RouteSearch.WalkRouteQuery walkRouteQuery = new RouteSearch.WalkRouteQuery(fromAndTo, RouteSearch.WalkDefault);
        mRouteSearch.calculateWalkRouteAsyn(walkRouteQuery);
    }

    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {

    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {

    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {
        if (i == AMapException.CODE_AMAP_SUCCESS && walkRouteResult != null) {
            synchronized (this) {
                for (OnWalkRouteListener listener : mListeners) {
                    List<WalkPath> walkPaths = walkRouteResult.getPaths();
                    float distance = 0;
                    int duration = 0;
                    if (walkPaths.size() > 0){
                        WalkPath walkPath = walkPaths.get(0);
                        distance = walkPath.getDistance();
                        duration = (int)(walkPath.getDuration());
                    }
                    listener.onWalkRouteCalculate(distance, duration,walkRouteResult);
                }

            }
        }
    }

    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

    }

    public interface OnWalkRouteListener{
        /**
         * 步行路径规划回调结果
         * @param distance-距离
         * @param duration-用时
         * @param walkRouteResult-步行路线规划结果集
         */
        void onWalkRouteCalculate(float distance,int duration,WalkRouteResult walkRouteResult);
    }

    public void addRouteCalculateListener(OnWalkRouteListener listener) {
        synchronized (this) {
            if (mListeners.contains(listener)){
                return;
            }
            mListeners.add(listener);
        }
    }

    public void removeRouteCalculateListener(OnWalkRouteListener listener) {
        synchronized (this) {
            mListeners.remove(listener);
        }
    }

    /**
     * 销毁定位资源
     */
    public void onDestroy() {
        mWalkRouteTask = null;
    }
}
