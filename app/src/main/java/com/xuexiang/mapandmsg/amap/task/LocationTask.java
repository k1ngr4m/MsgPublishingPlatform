/**  
 * Project Name:Android_Car_Example  
 * File Name:LocationTask.java  
 * Package Name:com.amap.api.car.example  
 * Date:2015年4月3日上午9:27:45  
 *  
 */

package com.xuexiang.mapandmsg.amap.task;

import android.content.Context;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.xuexiang.mapandmsg.amap.entity.PositionEntity;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * ClassName:LocationTask <br/>
 * Function: 简单封装了定位请求，可以进行单次定位和多次定位，注意的是在app结束或定位结束时注意销毁定位 <br/>
 */
public class LocationTask implements AMapLocationListener,
		OnLocationGetListener {

	private AMapLocationClient mLocationClient;

	private static LocationTask mLocationTask;

	private Context mContext;

	private OnLocationGetListener mOnLocationGetlisGetListener;

	private RegeocodeTask mRegecodeTask;

	private LocationTask(Context context) throws Exception {
		mLocationClient = new AMapLocationClient(context);
		mLocationClient.setLocationListener(this);
		mRegecodeTask = new RegeocodeTask(context);
		mRegecodeTask.setOnLocationGetListener(this);
		mContext = context;
	}

	public void setOnLocationGetListener(
			OnLocationGetListener onGetLocationListener) {
		mOnLocationGetlisGetListener = onGetLocationListener;
	}

	public static LocationTask getInstance(Context context) throws Exception {
		if (mLocationTask == null) {
			mLocationTask = new LocationTask(context);
		}
		return mLocationTask;
	}

	/**  
	 * 开启单次定位
	 */
	public void startSingleLocate() {
		AMapLocationClientOption option=new AMapLocationClientOption();
		option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
		option.setInterval(2000);
		option.setOnceLocation(true);
		mLocationClient.setLocationOption(option);
		mLocationClient.startLocation();

	}

	/**  
	 * 开启多次定位
	 */
	public void startLocate() {

		AMapLocationClientOption option=new AMapLocationClientOption();
		option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
		option.setOnceLocation(false);
		option.setInterval(8*1000);
		mLocationClient.setLocationOption(option);
		mLocationClient.startLocation();

	}

	/**  
	 * 结束定位，可以跟多次定位配合使用
	 */
	public void stopLocate() {
		mLocationClient.stopLocation();

	}

	/**  
	 * 销毁定位资源
	 */
	public void onDestroy() {
		mLocationClient.stopLocation();
		mLocationClient.onDestroy();
		mLocationTask = null;
	}



//	@Override
//	public void onLocationChanged(AMapLocation amapLocation) {
//		if (amapLocation != null && amapLocation.getErrorCode() == 0) {
//			PositionEntity entity = new PositionEntity();
//			entity.latitue = amapLocation.getLatitude();
//			entity.longitude = amapLocation.getLongitude();
//
//			if (!TextUtils.isEmpty(amapLocation.getAddress())) {
//				entity.address = amapLocation.getAddress();
//				Log.e("Debug:","onLocationChanged:当前地点：" + entity.address);
//			}else {
//				Log.e("Debug:","onLocationChanged:address is null");
//			}
//			mOnLocationGetlisGetListener.onLocationGet(entity);
//
//		}
//	}

	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
		if (amapLocation != null) {
			if (amapLocation.getErrorCode() == 0) {
				PositionEntity entity = new PositionEntity();
				//定位成功回调信息，设置相关消息
				amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
				entity.latitue = amapLocation.getLatitude();//获取纬度
				entity.longitude = amapLocation.getLongitude();//获取经度
				amapLocation.getAccuracy();//获取精度信息
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date date = new Date(amapLocation.getTime());
				df.format(date);//定位时间
				System.out.println(amapLocation.getAccuracy());
				mOnLocationGetlisGetListener.onLocationGet(entity);
			} else {
				//显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
				Log.e("AmapError","location Error, ErrCode:"
						+ amapLocation.getErrorCode() + ", errInfo:"
						+ amapLocation.getErrorInfo());
			}
		}
	}
	@Override
	public PositionEntity onLocationGet(PositionEntity entity) {

		// TODO Auto-generated method stub


		return entity;
	}

	@Override
	public void onRegecodeGet(PositionEntity entity) {

		// TODO Auto-generated method stub

	}

}
