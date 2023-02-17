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

package com.xuexiang.mapandmsg.fragment.newInfo;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.entity.LocalMedia;
import com.xuexiang.mapandmsg.R;
import com.xuexiang.mapandmsg.adapter.pictureselector.ImageSelectGridAdapter;
import com.xuexiang.mapandmsg.fragment.CallBack;
import com.xuexiang.mapandmsg.fragment.newInfo.task.NewInfoTask;
import com.xuexiang.mapandmsg.utils.XToastUtils;
import com.xuexiang.xaop.annotation.SingleClick;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xpage.base.XPageFragment;
import com.xuexiang.xpage.enums.CoreAnim;
import com.xuexiang.xui.widget.button.ButtonView;
import com.xuexiang.xui.widget.dialog.DialogLoader;
import com.xuexiang.xui.widget.edittext.materialedittext.MaterialEditText;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.OnClick;
import cn.leancloud.AVFile;
import cn.leancloud.AVObject;
import cn.leancloud.AVQuery;
import cn.leancloud.AVUser;
import cn.leancloud.types.AVNull;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * 展示消息页面
 *
 * @author 夕子
 */
@Page(name = "当前消息", anim = CoreAnim.slide)
public class ShowNewInfoFragment extends XPageFragment {
    public static final String KEY_MAP_DATA_TO_SHOW = "map_data_to_show";
    private static final String LEY_DELETE_MARKER_ID = "delete_marker_id";
    @BindView(R.id.show_news_title_me)
    MaterialEditText materialEditTextTitle;
    @BindView(R.id.show_news_time)
    MaterialEditText materialEditTextTime;
    @BindView(R.id.show_news_address)
    MaterialEditText materialEditTextAddress;
    @BindView(R.id.show_news_phone_number)
    MaterialEditText materialEditTextPhoneNumber;
    @BindView(R.id.show_news_contacts)
    MaterialEditText materialEditTextContacts;
    @BindView(R.id.show_news_summery_rb)
    TextView textViewSummery;
    @BindView(R.id.show_news_delete_bv)
    ButtonView buttonViewDelete;
    @BindView(R.id.show_news_route_bv)
    ButtonView buttonViewRoute;
    @BindView(R.id.show_news_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.show_news_effective_date)
    MaterialEditText materialEditTextEffectiveDate;

    private String newsInfoID;
    private Dialog mDialog;
    private ImageSelectGridAdapter mAdapter;
    private List<LocalMedia> mSelectList = new ArrayList<>();
    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what > 0) {
                buttonViewDelete.setVisibility(View.VISIBLE);
            } else {
                buttonViewDelete.setVisibility(View.INVISIBLE);
            }

        }
    };
    private PoiItem item;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_show_new_info;
    }

    @Override
    protected void initViews() {
        Bundle bundle = getArguments();
        newsInfoID = bundle.getString(KEY_MAP_DATA_TO_SHOW, "");

        if (!TextUtils.isEmpty(newsInfoID)) {
            AVQuery<AVObject> avQuery = new AVQuery<>("NewsInfo");
            avQuery.include("location");
            avQuery.getInBackground(newsInfoID).subscribe(new Observer<AVObject>() {
                @Override
                public void onSubscribe(@NotNull Disposable d) {

                }

                @Override
                public void onNext(@NotNull AVObject avObject) {
                    //阅读量+1
                    avObject.increment("read",1);
                    avObject.saveInBackground().subscribe();
                    AVObject owner = AVUser.getCurrentUser().getAVObject("Profile");
                    avQuery.whereEqualTo("objectId", newsInfoID);
                    avQuery.whereEqualTo("owner", owner);
                    avQuery.countInBackground().subscribe(new Observer<Integer>() {
                        @Override
                        public void onSubscribe(@NotNull Disposable d) {

                        }

                        @Override
                        public void onNext(@NotNull Integer integer) {
                            ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
                            singleThreadExecutor.execute(new Runnable() {
                                @Override
                                public void run() {
                                    //FIXME 这里直接更新ui是不行的
                                    //还有其他更新ui方式,runOnUiThread()等
                                    Message message = new Message();
                                    message.what = integer;
                                    handler.sendMessage(message);
                                }
                            });
                        }

                        @Override
                        public void onError(@NotNull Throwable e) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
                    setNewInfoOnText(avObject);
                }

                @Override
                public void onError(@NotNull Throwable e) {

                }

                @Override
                public void onComplete() {

                }
            });

        } else {
            XToastUtils.error("获取的newsInfoID为空！");
        }


        //图片
        GridLayoutManager manager = new GridLayoutManager(getActivity(), 3, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(mAdapter = new ImageSelectGridAdapter(getActivity()));
        mAdapter.setSelectList(mSelectList);
        mAdapter.setSelectMax(3);
        mAdapter.setOnItemClickListener((position, v) -> PictureSelector.create(ShowNewInfoFragment.this)
                .themeStyle(R.style.XUIPictureStyle)
                .openExternalPreview(position, mSelectList));
    }

    @Override
    protected void initListeners() {
    }

    @SuppressLint("NonConstantResourceId")
    @SingleClick
    @OnClick({R.id.show_news_back_bv, R.id.show_news_delete_bv,R.id.show_news_route_bv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.show_news_back_bv:
                CallBack.getInstance().callRefresh();
                popToBack();
                break;
            case R.id.show_news_delete_bv:
                String str = "是否确确认删除该消息？";
                showDeleteDialog(str);
                break;
            case R.id.show_news_route_bv:
                CallBack.getInstance().callBackShowToMap();
                CallBack.getInstance().sendPoiItem(item);
                popToBack();
                break;
            default:
                break;
        }
    }

    private void showDeleteDialog(String s) {
        mDialog =  DialogLoader.getInstance().showConfirmDialog(
                getContext(),
                getString(R.string.lab_tip_title),
                s,
                getString(R.string.lab_yes),
                (dialog, which) -> {
                    dialog.dismiss();
                    //从云端删除NewsInfo
                    AVObject todo = AVObject.createWithoutData("NewsInfo", newsInfoID);
                    todo.deleteInBackground().subscribe(new Observer<AVNull>() {
                        @Override
                        public void onSubscribe(@NotNull Disposable d) {

                        }

                        @Override
                        public void onNext(@NotNull AVNull avNull) {
                            XToastUtils.success("删除成功！");
                            CallBack.getInstance().callRefresh();
                            NewInfoTask.getInstance().DeleteNewInfo(newsInfoID);
                            popToBack();
                        }

                        @Override
                        public void onError(@NotNull Throwable e) {
                            XToastUtils.error("删除错误！" + e.getMessage());
                        }

                        @Override
                        public void onComplete() {

                        }
                    });

                },
                getString(R.string.lab_no),
                (dialog, which) -> {
                    dialog.dismiss();
                }
        );
    }

    public void setNewInfoOnText(AVObject newInfo) {
        String title = (String) newInfo.get("title");
        String date = (String) newInfo.get("date");
        String address = (String) newInfo.get("address");
        String phone = (String) newInfo.get("phone");
        String contacts = (String) newInfo.get("contacts");
        String summery = (String) newInfo.get("summery");
        String effective_date = (String) newInfo.get("effective_date");
        materialEditTextTitle.setText(title);
        materialEditTextTime.setText(date);
        materialEditTextAddress.setText(address);
        materialEditTextPhoneNumber.setText(phone);
        materialEditTextContacts.setText(contacts);
        textViewSummery.setText(summery);
        materialEditTextEffectiveDate.setText(effective_date);
        int imageNum = 8;
        for (int i = 0; i < imageNum; i++) {
            // 图片
            AVFile file = newInfo.getAVFile("image" + i);
            if (file != null) {
                LocalMedia localMedia = new LocalMedia(file.getUrl(), 0, 0, null);
                mSelectList.add(localMedia);
            }
        }
        mAdapter.setSelectList(mSelectList);
        mAdapter.notifyDataSetChanged();
        item = new PoiItem(null,new LatLonPoint(newInfo.getAVGeoPoint("location").getLatitude()
                ,newInfo.getAVGeoPoint("location").getLongitude()),null,null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }
}