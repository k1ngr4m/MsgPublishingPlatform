/**  
 * Project Name:Android_Car_Example  
 * File Name:PoiSearchTask.java  
 * Package Name:com.amap.api.car.example  
 * Date:2015年4月7日上午11:25:07  
 *  
 */

package com.xuexiang.mapandmsg.amap.task;

import android.content.Context;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener;
import com.amap.api.services.poisearch.PoiSearch.Query;
import com.xuexiang.mapandmsg.adapter.base.delegate.SimpleDelegateAdapter;

import java.util.ArrayList;


/**
 * ClassName:PoiSearchTask <br/>
 * Function: 简单封装了poi搜索的功能，搜索结果配合RecommendAdapter进行使用显示 <br/>
 * @author 夕子
 */
public class PoiSearchTask implements OnPoiSearchListener {

	private Context mContext;
	private SimpleDelegateAdapter<PoiItem> mPoiListAdapter;

	public PoiSearchTask(Context applicationContext, SimpleDelegateAdapter<PoiItem> poiListAdapter) {
		mContext = applicationContext;
		mPoiListAdapter = poiListAdapter;
	}

	public void search(String keyWord,String city) throws AMapException {
		Query query = new Query(keyWord, "", city);
		query.setPageSize(10);
		query.setPageNum(0);

		PoiSearch poiSearch = new PoiSearch(mContext, query);
		poiSearch.setOnPoiSearchListener(this);
		poiSearch.searchPOIAsyn();
	}

	/**
	 * 检索中心点附近的地址
	 *
	 * @param keyWord 关键词
	 * @param lat
	 * @param lng
	 */
	public void query(String keyWord, double lat, double lng) throws AMapException {
		//keyWord表示搜索字符串，
		//第二个参数表示POI搜索类型，二者选填其一，选用POI搜索类型时建议填写类型代码，码表可以参考下方（而非文字）
		//cityCode表示POI搜索区域，可以是城市编码也可以是城市名称，也可以传空字符串，空字符串代表全国在全国范围内进行搜索
		Query query = new Query(keyWord, "", "");
		// 设置每页最多返回多少条poiitem
		query.setPageSize(50);
		//设置查询页码
		query.setPageNum(0);
		PoiSearch poiSearch = new PoiSearch(mContext, query);
		//设置周边搜索的中心点以及半径
		poiSearch.setBound(new PoiSearch.SearchBound(new LatLonPoint(lat, lng), 1000));
		poiSearch.setOnPoiSearchListener(this);
		poiSearch.searchPOIAsyn();
	}



	@Override
	public void onPoiSearched(PoiResult poiResult, int resultCode) {
		if (resultCode == AMapException.CODE_AMAP_SUCCESS && poiResult != null) {
			ArrayList<PoiItem> poiItemArrayList=poiResult.getPois();
			if(poiItemArrayList==null){
				return;
			}
			//监听回调：废弃
			onPoiListGetListener.onPoiListGet(poiItemArrayList);
			mPoiListAdapter.refresh(poiItemArrayList);
		}
		//TODO 可以根据app自身需求对查询错误情况进行相应的提示或者逻辑处理
	}

	@Override
	public void onPoiItemSearched(PoiItem poiItem, int i) {

	}

	public interface OnPoiListGetListener {
		/**
		 * 得到PoiList时候回调
		 * @param poiItemArrayList
		 */
		void onPoiListGet(ArrayList<PoiItem> poiItemArrayList);
	}

	private OnPoiListGetListener onPoiListGetListener;

	public void setOnPoiListGetListener(OnPoiListGetListener onPoiListGetListener) {
		this.onPoiListGetListener = onPoiListGetListener;
	}
}
