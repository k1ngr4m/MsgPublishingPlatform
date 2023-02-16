/**  
 * Project Name:Android_Car_Example  
 * File Name:RegeocodeTask.java  
 * Package Name:com.amap.api.car.example  
 * Date:2015年4月2日下午6:24:53  
 *  
 */

package com.xuexiang.mapandmsg.amap.task;

import android.content.Context;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.xuexiang.mapandmsg.amap.entity.PositionEntity;
import com.xuexiang.mapandmsg.utils.XToastUtils;


/**
 * ClassName:RegeocodeTask <br/>
 * Function: 简单的封装的逆地理编码功能 <br/>
 */
public class RegeocodeTask implements OnGeocodeSearchListener {
	private static final float SEARCH_RADIUS = 50;
	private OnLocationGetListener mOnLocationGetListener;

	private GeocodeSearch mGeocodeSearch;
	private LatLonPoint latLonPoint ;

	public RegeocodeTask(Context context) throws AMapException {
		mGeocodeSearch = new GeocodeSearch(context);
		mGeocodeSearch.setOnGeocodeSearchListener(this);
	}

	public void search(double latitude, double longitude) {
		latLonPoint = new LatLonPoint(latitude,longitude);
		RegeocodeQuery regecodeQuery = new RegeocodeQuery(new LatLonPoint(
				latitude, longitude), SEARCH_RADIUS, GeocodeSearch.AMAP);
		mGeocodeSearch.getFromLocationAsyn(regecodeQuery);
	}

	public void setOnLocationGetListener(
			OnLocationGetListener onLocationGetListener) {
		mOnLocationGetListener = onLocationGetListener;
	}

	@Override
	public void onGeocodeSearched(GeocodeResult arg0, int arg1) {

	}

	@Override
	public void onRegeocodeSearched(RegeocodeResult regeocodeReult,
			int resultCode) {
		if (resultCode == AMapException.CODE_AMAP_SUCCESS) {
			if (regeocodeReult != null
					&& regeocodeReult.getRegeocodeAddress() != null
					&& mOnLocationGetListener != null) {
				String address = regeocodeReult.getRegeocodeAddress()
						.getFormatAddress();
				String city = regeocodeReult.getRegeocodeAddress().getCityCode();
		 
				PositionEntity entity = new PositionEntity();
				entity.address = address;
				entity.city = city;
				entity.latitue = latLonPoint.getLatitude();
				entity.longitude = latLonPoint.getLongitude();
				mOnLocationGetListener.onRegecodeGet(entity);
			}
		}else {
			XToastUtils.error("逆地理回调失败！code：" + resultCode);
		}
		//TODO 可以根据app自身需求对查询错误情况进行相应的提示或者逻辑处理
	}

}
