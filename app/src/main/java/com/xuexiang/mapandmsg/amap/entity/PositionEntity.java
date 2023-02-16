/**  
 * Project Name:Android_Car_Example  
 * File Name:PositionEntity.java  
 * Package Name:com.amap.api.car.example  
 * Date:2015年4月3日上午9:50:28  
 *  
 */

package com.xuexiang.mapandmsg.amap.entity;

/**
 * ClassName:PositionEntity <br/>
 * Function: 封装的关于位置的实体 <br/>
 * @see
 */
public class PositionEntity {

	public double latitue;

	public double longitude;

	public String address;
	
	public String city;

	public PositionEntity() {
	}

	public PositionEntity(double latitude, double longtitude, String address,String city) {
		this.latitue = latitude;
		this.longitude = longtitude;
		this.address = address;
	}

	public PositionEntity(double latitude, double longtitude) {
		this.latitue = latitude;
		this.longitude = longtitude;
	}
	public double getLatitue() {
		return latitue;
	}

	public double getLongitude() {
		return longitude;
	}

	public String getAddress() {
		return address;
	}

	public String getCity() {
		return city;
	}

	public void setLatitue(double latitue) {
		this.latitue = latitue;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setCity(String city) {
		this.city = city;
	}
}
