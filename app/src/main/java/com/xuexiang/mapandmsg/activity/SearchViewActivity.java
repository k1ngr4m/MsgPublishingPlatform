/*
 * Copyright (C) 2019 xuexiangjys(xuexiangjys@163.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xuexiang.mapandmsg.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.VirtualLayoutManager;
import com.alibaba.android.vlayout.layout.LinearLayoutHelper;
import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.PoiItem;
import com.xuexiang.mapandmsg.R;
import com.xuexiang.mapandmsg.adapter.base.delegate.SimpleDelegateAdapter;
import com.xuexiang.mapandmsg.amap.task.PoiSearchTask;
import com.xuexiang.mapandmsg.amap.util.utils;
import com.xuexiang.mapandmsg.fragment.CallBack;
import com.xuexiang.mapandmsg.utils.XToastUtils;
import com.xuexiang.xui.adapter.recyclerview.RecyclerViewHolder;
import com.xuexiang.xui.widget.searchview.MaterialSearchView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * 搜索页面
 */
public class SearchViewActivity extends AppCompatActivity implements PoiSearchTask.OnPoiListGetListener {
    public static final String KEY_POI_LATITUDE = "poi_latitude";
    public static final String KEY_POI_LONGITUDE = "poi_longitude";
    private static final String LATLNG_HANGZHOU = "杭州";

    @BindView(R.id.search_view)
    MaterialSearchView mSearchView;
    @BindView(R.id.search_poi_recycler_view)
    RecyclerView mRecyclerView;

    private Unbinder mUnbinder;
    private PoiSearchTask poiSearchTask;
    private LatLng latLng;
    private SimpleDelegateAdapter<PoiItem> mPoiListAdapter;

    private String searchText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchview_toolbar);
        mUnbinder = ButterKnife.bind(this);
        initViews();
        initData();
        initRecyclerList();
        initTask();
    }

    private SpannableString changeTextColor(String str, String str1) {
        SpannableString spannableString = new SpannableString(str);
        if (!TextUtils.isEmpty(str1)) {
            for (int i = 0; i < str.length(); i++) {
                for (int j = 0; j < str1.length(); j++) {
                    if (str.charAt(i) == str1.charAt(j)) {
                        spannableString.setSpan(new ForegroundColorSpan(
                                        Color.parseColor("#299EE3")),
                                str.indexOf(searchText),
                                str.indexOf(searchText) + 1,
                                0);
                        i = j;
                        continue;
                    }
                }
            }


        }
        return spannableString;
    }

    private void initRecyclerList() {
        VirtualLayoutManager virtualLayoutManager = new VirtualLayoutManager(this);
        mRecyclerView.setLayoutManager(virtualLayoutManager);
        RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
        mRecyclerView.setRecycledViewPool(viewPool);
        viewPool.setMaxRecycledViews(0, 10);

        mPoiListAdapter = new SimpleDelegateAdapter<PoiItem>(R.layout.poi_list_item, new LinearLayoutHelper()) {
            @Override
            protected void bindData(@NonNull @NotNull RecyclerViewHolder holder, int position, PoiItem item) {
                if (holder != null) {
                    String title = item.getTitle();
                    SpannableString spannableString = null;
                    int start = title.indexOf(searchText.charAt(0));
                    int end = start + searchText.length();
                    if (!TextUtils.isEmpty(searchText)) {
                        spannableString = new SpannableString(title);
                        if (spannableString != null) {
                            if (start >= 0 && start <= spannableString.length()) {
                                if (end >= start && end <= spannableString.length()) {
                                    spannableString.setSpan(
                                            new ForegroundColorSpan(Color.parseColor("#299EE3")),
                                            start,
                                            end,
                                            0);
                                }
                            }
                        }
                    }
                    holder.text(R.id.poi_item_text, spannableString);
                    holder.text(R.id.poi_item_detail, item.getSnippet());
                    holder.click(R.id.poi_item_RL, view -> {
                        CallBack.getInstance().sendPoiItem(item);
                        //收回输入法
                        utils.hideSoftKeyboard(SearchViewActivity.this);
                        finish();
                    });
                }
            }
        };

        DelegateAdapter delegateAdapter = new DelegateAdapter(virtualLayoutManager);
        delegateAdapter.addAdapter(mPoiListAdapter);
        mRecyclerView.setAdapter(delegateAdapter);

    }

    private void initTask() {
        //生成poiSearchTask对象
        poiSearchTask = new PoiSearchTask(getApplicationContext(), mPoiListAdapter);
        poiSearchTask.setOnPoiListGetListener(this);
    }

    private void initData() {
        Intent intent = getIntent();
        double latitude = intent.getDoubleExtra(KEY_POI_LATITUDE, 0.0);
        double longitude = intent.getDoubleExtra(KEY_POI_LONGITUDE, 0.0);
        latLng = new LatLng(latitude, longitude);
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSearchView.setVoiceSearch(false);
        mSearchView.setEllipsize(true);
        mSearchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String nowText) {
                if (!TextUtils.isEmpty(nowText)) {
                    searchText = nowText;
                    Log.e("tag", searchText);
                    if (latLng.latitude != 0 && latLng.longitude != 0) {
                        try {
                            poiSearchTask.query(nowText, latLng.latitude, latLng.longitude);
                        } catch (AMapException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            poiSearchTask.search(nowText, LATLNG_HANGZHOU);
                        } catch (AMapException e) {
                            e.printStackTrace();
                        }
                        XToastUtils.error("经纬度数据错误，系统设定在杭州搜索！");
                    }
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText)) {
                    searchText = newText;
                    Log.e("tag", searchText);
                    if (latLng.latitude != 0 && latLng.longitude != 0) {
                        try {
                            poiSearchTask.query(newText, latLng.latitude, latLng.longitude);
                        } catch (AMapException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            poiSearchTask.search(newText, LATLNG_HANGZHOU);
                        } catch (AMapException e) {
                            e.printStackTrace();
                        }
                        XToastUtils.error("经纬度数据错误，系统设定在杭州搜索！");
                    }
                }
                return false;
            }
        });
        mSearchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

                //Do some magic
            }

            @Override
            public void onSearchViewClosed() {

                //Do some magic
            }
        });
        mSearchView.setSubmitOnClick(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        mSearchView.setMenuItem(item);

        return true;
    }

    @Override
    public void onBackPressed() {
        if (mSearchView.isSearchOpen()) {
            mSearchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && matches.size() > 0) {
                String searchWrd = matches.get(0);
                if (!TextUtils.isEmpty(searchWrd)) {
                    XToastUtils.toast("mSearchView.setQuery(searchWrd, false);");
                    mSearchView.setQuery(searchWrd, false);
                }
            }

            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        mUnbinder.unbind();
        super.onDestroy();
    }

    @Override
    public void onPoiListGet(ArrayList<PoiItem> poiItemArrayList) {
        //poiListAdapter.addAll(poiItemArrayList);

    }
}
