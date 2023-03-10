/*
 * Copyright (C) 2019 xuexiangjys(xuexiangjys@163.com)
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

package com.xuexiang.mapandmsg.fragment.news;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.VirtualLayoutManager;
import com.alibaba.android.vlayout.layout.LinearLayoutHelper;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.codingending.library.FairySearchView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.xuexiang.mapandmsg.R;
import com.xuexiang.mapandmsg.adapter.base.broccoli.BroccoliSimpleDelegateAdapter;
import com.xuexiang.mapandmsg.adapter.base.delegate.SimpleDelegateAdapter;
import com.xuexiang.mapandmsg.adapter.base.delegate.SingleDelegateAdapter;
import com.xuexiang.mapandmsg.amap.MyNowPosition;
import com.xuexiang.mapandmsg.core.BaseFragment;
import com.xuexiang.mapandmsg.fragment.CallBack;
import com.xuexiang.mapandmsg.fragment.Thread.ThreadPool;
import com.xuexiang.mapandmsg.fragment.newInfo.ShowNewInfoFragment;
import com.xuexiang.mapandmsg.leancloud.PraiseList;
import com.xuexiang.mapandmsg.utils.XToastUtils;
import com.xuexiang.mapandmsg.widget.EditSpinnerDialog;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xpage.enums.CoreAnim;
import com.xuexiang.xui.adapter.recyclerview.RecyclerViewHolder;
import com.xuexiang.xui.utils.ResUtils;
import com.xuexiang.xui.widget.actionbar.TitleBar;
import com.xuexiang.xui.widget.popupwindow.good.GoodView;
import com.xuexiang.xui.widget.popupwindow.good.IGoodView;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import butterknife.BindView;
import cn.leancloud.AVFile;
import cn.leancloud.AVObject;
import cn.leancloud.AVQuery;
import cn.leancloud.AVUser;
import cn.leancloud.types.AVGeoPoint;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import me.samlss.broccoli.Broccoli;

/**
 * ????????????
 *
 * @author ??????
 */
@Page(anim = CoreAnim.none)
public class NewsFragment extends BaseFragment implements CallBack.OnRefreshListener {
    final int UPDATE_UI = 10001;
    final int UPDATE_ASCEND_DATE = 10002;
    final int UPDATE_DESCEND_DATE = 10003;
    final int UPDATE_ASCEND_DISTANCE = 10004;
    final int UPDATE_DESCEND_DISTANCE = 10005;
    final int UPDATE_DESCEND_PRAISE = 10006;
    final int UPDATE_DESCEND_READ = 10007;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.refreshLayout)
    /**
     * ??????????????????
     */
            SmartRefreshLayout refreshLayout;
    @BindView(R.id.search_view)
    FairySearchView fairySearchView;
    /**
     * ?????????list
     */
    List<AVObject> mAvObjectList;
    /**
     * ?????????
     */
    private SimpleDelegateAdapter<AVObject> mNewsAdapter;
    private String data = "????????????";
    AVFile file1;
    AVFile file2;
    /**
     * handler??????ui
     */
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            AVQuery<AVObject> query = new AVQuery<>("NewsInfo");
            query.include("owner");
            switch (msg.what) {
                case UPDATE_UI:
                    break;
                case UPDATE_ASCEND_DATE:
                    // ??? createdAt ????????????
                    query.orderByAscending("createdAt");
                    break;
                case UPDATE_DESCEND_DATE:
                    // ??? createdAt ????????????
                    query.orderByDescending("createdAt");
                    break;
                case UPDATE_ASCEND_DISTANCE:
                    AVGeoPoint point = new AVGeoPoint(MyNowPosition.myNowPositionEntity.latitue,
                            MyNowPosition.myNowPositionEntity.longitude);
                    query.whereNear("location", point);
                    break;
                case UPDATE_DESCEND_DISTANCE:
                    break;
                case UPDATE_DESCEND_PRAISE:
                    //??????????????????
                    query.orderByDescending("praise");
                    break;
                case UPDATE_DESCEND_READ:
                    //??????????????????
                    query.orderByDescending("read");
                    break;
                default:
                    break;
            }
            query.findInBackground().subscribe(new Observer<List<AVObject>>() {
                @Override
                public void onSubscribe(@NotNull Disposable d) {

                }

                @Override
                public void onNext(@NotNull List<AVObject> avObjects) {
                    mAvObjectList = avObjects;
                    refreshLayout.autoRefresh();
                }

                @Override
                public void onError(@NotNull Throwable e) {

                }

                @Override
                public void onComplete() {

                }
            });
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
     * @return
     */
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_news;
    }

    /**
     * ???????????????
     */
    @Override
    protected void initViews() {
        getData();
        //VirtualLayout???????????????RecyclerView???LayoutManager??????, ????????????????????????????????????????????????????????????????????????
        VirtualLayoutManager virtualLayoutManager = new VirtualLayoutManager(Objects.requireNonNull(getContext()));
        recyclerView.setLayoutManager(virtualLayoutManager);
        //??????????????????????????????RecyclerView????????????ViewHolder??????????????????????????????RecycledViewPool???????????????????????????????????????ViewHolder????????????????????????GC????????????????????????????????????????????????????????????
        RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
        recyclerView.setRecycledViewPool(viewPool);
        viewPool.setMaxRecycledViews(0, 10);

        //???????????????
        SingleDelegateAdapter titleAdapter = new SingleDelegateAdapter(R.layout.adapter_title_item) {
            @Override
            public void onBindViewHolder(@NonNull @NotNull RecyclerViewHolder holder, int position) {
                holder.text(R.id.tv_title, "??????");
                holder.text(R.id.tv_action, "??????");
                holder.click(R.id.tv_action, view -> {
                    showEditSpinnerDialog(getContext(), "????????????", data, ResUtils.getStringArray(R.array.sort_mode_entry), value -> {
                        data = value;
                        ExecutorService pool = ThreadPool.getThreadPool();
                        Message message = new Message();
                        switch (value) {
                            case "????????????":
                                pool.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        message.what = UPDATE_UI;
                                        handler.sendMessage(message);
                                    }
                                });
                                break;
                            case "????????????????????????":
                                pool.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        message.what = UPDATE_ASCEND_DATE;
                                        handler.sendMessage(message);
                                    }
                                });
                                break;
                            case "????????????????????????":
                                pool.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        message.what = UPDATE_DESCEND_DATE;
                                        handler.sendMessage(message);
                                    }
                                });
                                break;
                            case "????????????????????????":
                                pool.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        message.what = UPDATE_ASCEND_DISTANCE;
                                        handler.sendMessage(message);
                                    }
                                });
                                break;
                            case "????????????????????????":
                                pool.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        message.what = UPDATE_DESCEND_DISTANCE;
                                        handler.sendMessage(message);
                                    }
                                });
                                break;
                            case "????????????":
                                pool.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        message.what = UPDATE_DESCEND_PRAISE;
                                        handler.sendMessage(message);
                                    }
                                });
                                break;
                            case "???????????????":
                                pool.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        message.what = UPDATE_DESCEND_READ;
                                        handler.sendMessage(message);
                                    }
                                });
                                break;
                            default:
                                break;
                        }
                        XToastUtils.toast(data);
                    });
                });
            }
        };

        //??????
        mNewsAdapter = new BroccoliSimpleDelegateAdapter<AVObject>(R.layout.adapter_news_card_view_list_item,
                new LinearLayoutHelper(), mAvObjectList) {
            @SuppressLint("DefaultLocale")
            @Override
            protected void onBindData(RecyclerViewHolder holder, AVObject model, int position) {
                String effective_date = model.get("effective_date").toString();
                System.out.println(model.get("title").toString());
                boolean isEffective = true;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy???MM???dd??? HH:mm:ss", Locale.CHINA);
                    LocalDateTime deadLine = LocalDateTime.parse(effective_date, formatter);
                    isEffective = deadLine.isAfter(LocalDateTime.now());
                }

                if (model != null && isEffective) {
                    file1 = file2 = null;
                    //Profile
                    AVObject owner = model.getAVObject("owner");

                    holder.text(R.id.tv_user_name, owner == null ? "??????"
                            : owner.getString("username"));
                    holder.text(R.id.tv_tag, model.get("date") == null ? ""
                            : model.get("date").toString());
                    holder.text(R.id.tv_title, model.get("title") == null ? ""
                            : model.get("title").toString());
                    holder.text(R.id.tv_summary, model.get("summery") == null ? ""
                            : model.get("summery").toString());
                    holder.text(R.id.tv_praise, model.get("praise") == null ? "?????? 0"
                            : "?????? " + String.valueOf(model.get("praise")));
                    holder.text(R.id.tv_comment, model.get("comment") == null ? "?????? 0"
                            : "?????? " + String.valueOf(model.get("comment")));
                    float distance = getDistance(model.getAVGeoPoint("location"));
                    holder.text(R.id.tv_read, String.format("%.1f???", distance));
                    file1 = model.getAVFile("image" + 0);
                    holder.image(R.id.iv_image, file1 == null ? R.drawable.xui_ic_default_img
                            : file1.getUrl());
                    //??????
                    String url = "http://lc-mbiWXKga.cn-n1.lcfile.com/q5b2OAvMXYIN9ozkDzWqsbpbVbwUAkzO/head.png";
                    holder.image(R.id.iv_avatar, owner == null ? R.drawable.head
                            : owner.getAVFile("head") == null ? R.drawable.head
                            : url);
                    holder.text(R.id.tv_comment, "????????? " + (model.get("read")
                            == null ? "0" : model.get("read").toString()));
                    holder.click(R.id.card_view, v -> openNewPage(ShowNewInfoFragment.class,
                            ShowNewInfoFragment.KEY_MAP_DATA_TO_SHOW, model.getObjectId()));
                    //??????
                    holder.click(R.id.iv_praise, v -> {
                        holder.image(R.id.iv_praise, R.drawable.ic_good_checked);
                        IGoodView mGoodView = new GoodView(getContext());
                        mGoodView.setText("+1")
                                .setTextColor(Color.parseColor("#f66467"))
                                .setTextSize(12)
                                .show(v);
                        praiseInBackground(model.getObjectId());
                    });
                    holder.click(R.id.tv_praise, v -> {
                        praiseInBackground(model.getObjectId());
                    });
                }
                else {
//                    holder.visible(R.id.card_view_linear, View.GONE);
                    holder.itemView.setVisibility(View.GONE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0,0));
                }
            }

            @Override
            protected void onBindBroccoli(RecyclerViewHolder holder, Broccoli broccoli) {
                broccoli.addPlaceholders(
                        holder.findView(R.id.tv_user_name),
                        holder.findView(R.id.tv_tag),
                        holder.findView(R.id.tv_title),
                        holder.findView(R.id.tv_summary),
                        holder.findView(R.id.tv_praise),
                        holder.findView(R.id.tv_comment),
                        holder.findView(R.id.tv_read),
                        holder.findView(R.id.iv_image)
                );
            }
        };

        DelegateAdapter delegateAdapter = new DelegateAdapter(virtualLayoutManager);
        delegateAdapter.addAdapter(titleAdapter);
        delegateAdapter.addAdapter(mNewsAdapter);
        recyclerView.setAdapter(delegateAdapter);
    }

    private void praiseInBackground(String objectId) {
        AVObject oneNewInfo = AVObject.createWithoutData("NewsInfo",objectId);
        AVQuery<PraiseList> query = new AVQuery<>("PraiseList");
        //AVQuery<PraiseList> query = AVQuery.getQuery(PraiseList.class);
        query.whereEqualTo(PraiseList.USERNAME,AVUser.getCurrentUser().getUsername());
        query.whereEqualTo(PraiseList.NEWSINFO,oneNewInfo);
        query.countInBackground().subscribe(new Observer<Integer>() {
            @Override
            public void onSubscribe(@NotNull Disposable d) {

            }

            @Override
            public void onNext(@NotNull Integer integer) {
                if(integer == 0){
                    oneNewInfo.increment("praise",1);
                    oneNewInfo.saveInBackground().subscribe();
                    AVObject praiseList = new AVObject("PraiseList");
                    praiseList.put(PraiseList.USERNAME, AVUser.getCurrentUser().getUsername());
                    praiseList.put(PraiseList.NEWSINFO,oneNewInfo);
                    praiseList.saveInBackground().subscribe(new Observer<AVObject>() {
                        @Override
                        public void onSubscribe(@NotNull Disposable d) {

                        }

                        @Override
                        public void onNext(@NotNull AVObject avObject) {
                            log("???????????????");
                        }

                        @Override
                        public void onError(@NotNull Throwable e) {
                            log("???????????????"+e.getMessage());
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
                    CallBack.getInstance().callRefresh();
                }else {
                    XToastUtils.error("???????????????");
                }
            }

            @Override
            public void onError(@NotNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });

    }

    private void log(String s) {
        Log.e("debug(news)",s);
    }

    private float getDistance(AVGeoPoint avGeoPoint) {
        LatLng latLng1 = new LatLng(avGeoPoint.getLatitude(), avGeoPoint.getLongitude());
        if (MyNowPosition.myNowPositionEntity == null) {
            return -1;
        }
        LatLng latLng2 = new LatLng(MyNowPosition.myNowPositionEntity.latitue, MyNowPosition.myNowPositionEntity.longitude);
        return AMapUtils.calculateLineDistance(latLng1, latLng2);
    }

    private void getData() {
        AVQuery<AVObject> query = new AVQuery<>("NewsInfo");
        query.include("owner");
        query.findInBackground().subscribe(new Observer<List<AVObject>>() {
            @Override
            public void onSubscribe(@NotNull Disposable d) {

            }

            @Override
            public void onNext(@NotNull List<AVObject> avObjects) {
                mAvObjectList = avObjects;
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
    protected void initListeners() {
        CallBack.getInstance().setOnRefreshListener(this);
        //????????????
        refreshLayout.setOnRefreshListener(refreshLayout -> {
            // TODO: 2020-02-25 ?????????????????????????????????
            refreshLayout.getLayout().postDelayed(() -> {
                mNewsAdapter.refresh(mAvObjectList);
                onRefresh();
                refreshLayout.finishRefresh();
            }, 0);

        });
        //????????????
        refreshLayout.setOnLoadMoreListener(refreshLayout -> {
            // TODO: 2020-02-25 ?????????????????????????????????
            refreshLayout.getLayout().postDelayed(() -> {
                //getData();
                List<AVObject> newList = null;
                mNewsAdapter.loadMore(newList);
                refreshLayout.finishLoadMore();
            }, 1000);
        });
        refreshLayout.autoRefresh();//????????????????????????????????????????????????

        fairySearchView.setOnEditChangeListener(new FairySearchView.OnEditChangeListener() {
            @Override
            public void onEditChanged(String nowContent) {
                AVQuery<AVObject> queryTitle = new AVQuery<>("NewsInfo");
                AVQuery<AVObject> querySummery = new AVQuery<>("NewsInfo");
                // ????????? SQL ?????? title LIKE '%lunch%'
                queryTitle.whereContains("title", nowContent);
                querySummery.whereContains("summery", nowContent);
                AVQuery<AVObject> query = AVQuery.or(Arrays.asList(queryTitle, querySummery));
                query.findInBackground().subscribe(new Observer<List<AVObject>>() {
                    @Override
                    public void onSubscribe(@NotNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NotNull List<AVObject> avObjects) {
                        mAvObjectList = avObjects;
                        refreshLayout.autoRefresh();
                    }

                    @Override
                    public void onError(@NotNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
            }
        });
    }

    @Override
    public void onRefresh() {
        ExecutorService pool = ThreadPool.getThreadPool();
        pool.execute(new Runnable() {
            @Override
            public void run() {
                //FIXME ??????????????????ui????????????
                //??????????????????ui??????,runOnUiThread()???
                Message message = new Message();
                message.what = UPDATE_UI;
                handler.sendMessage(message);
            }
        });
    }

    /**
     * ??????spinner????????????
     *
     * @param context
     * @param title
     * @param defaultItems
     * @param listener
     * @return
     */
    public static EditSpinnerDialog showEditSpinnerDialog(Context context, String title, String data, String[] defaultItems, EditSpinnerDialog.OnEditListener listener) {
        return EditSpinnerDialog.newBuilder(context)
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
