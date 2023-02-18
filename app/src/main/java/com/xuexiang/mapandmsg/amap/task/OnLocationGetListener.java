/**  
 * Project Name:Android_Car_Example  
 * File Name:OnLocationGetListener.java  
 * Package Name:com.amap.api.car.example  
 * Date:2015年4月2日下午6:17:17  
 *  
*/  
  
package com.xuexiang.mapandmsg.amap.task;


import com.xuexiang.mapandmsg.amap.entity.PositionEntity;

/**
 * ClassName:OnLocationGetListener <br/>  
 * Function: 逆地理编码或者定位完成后回调接口<br/>
 */
public interface OnLocationGetListener {

	public PositionEntity onLocationGet(PositionEntity entity);

	public void onRegecodeGet(PositionEntity entity);
}
  
