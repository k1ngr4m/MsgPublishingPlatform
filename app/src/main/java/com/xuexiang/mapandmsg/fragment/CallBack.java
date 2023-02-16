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

package com.xuexiang.mapandmsg.fragment;

import com.amap.api.location.AMapLocation;
import com.amap.api.services.core.PoiItem;

/**
 * @ClassName: CallBack
 * @Description: 回调
 * @Author
 * @Date 2021/5/17
 * @Version 1.0
 */
public class CallBack {
    private static CallBack mCallBack;
    public CallBack() {
    }

    public static CallBack getInstance() {
        if (mCallBack == null) {
            mCallBack = new CallBack();
        }
        return mCallBack;
    }

    public interface OnClickPoiItemListener{
        /**
         *从search返回POI到map
         * @param poiItem
         */
        void onGetClickPoiItem(PoiItem poiItem);
    }
    private OnClickPoiItemListener onClickPoiItemListener;
    public void setOnClickPoiItemListener(OnClickPoiItemListener onClickPoiItemListener) {
        this.onClickPoiItemListener = onClickPoiItemListener;
    }
    public void sendPoiItem(PoiItem poiItem){
        onClickPoiItemListener.onGetClickPoiItem(poiItem);
    }


    public interface OnBackUpDataUserNameListener{
        /**
         * 从modify返回account更新用户名
         */
        void onBackUpDataUserName();
    }
    private OnBackUpDataUserNameListener onBackUpDataUserNameListener;
    public void setOnBackUpDataUserNameListener(OnBackUpDataUserNameListener onBackUpDataUserNameListener) {
        this.onBackUpDataUserNameListener = onBackUpDataUserNameListener;
    }
    public void backToAccount(){
        onBackUpDataUserNameListener.onBackUpDataUserName();
    }


    public  interface OnRefreshListener{
        /**
         * 刷新消息列表
         */
        void onRefresh();
    }
    private OnRefreshListener onRefreshListener;
    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
    }
    public void callRefresh(){onRefreshListener.onRefresh();}

    public interface OnRefreshHeadListener{
        /**
         * 告诉main页面更新头像
         */
        void onRefreshHead();

        void onLocationChanged(AMapLocation aMapLocation);
    }
    private OnRefreshHeadListener onRefreshHeadListener;
    public void setOnRefreshHeadListener(OnRefreshHeadListener onRefreshHeadListener) {
        this.onRefreshHeadListener = onRefreshHeadListener;
    }
    public void callMainRefreshHead(){
        onRefreshHeadListener.onRefreshHead();
    }

    public interface OnBackShowToMapListener{
        /**
         * 从show页面跳转到map页面
         */
        void onBackShowToMap();
    }
    public void setOnBackShowToMapListener(OnBackShowToMapListener onBackShowToMapListener) {
        this.onBackShowToMapListener = onBackShowToMapListener;
    }
    private OnBackShowToMapListener onBackShowToMapListener;
    public void callBackShowToMap(){
        onBackShowToMapListener.onBackShowToMap();
    }

    /**
     * 销毁定位资源
     */
    public void onDestroy() {
       mCallBack = null;
    }

}
