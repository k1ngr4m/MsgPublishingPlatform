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
import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.entity.LocalMedia;
import com.xuexiang.mapandmsg.R;
import com.xuexiang.mapandmsg.activity.MainActivity;
import com.xuexiang.mapandmsg.adapter.pictureselector.ImageSelectGridAdapter;
import com.xuexiang.mapandmsg.amap.task.RegeocodeTask;
import com.xuexiang.mapandmsg.fragment.CallBack;
import com.xuexiang.mapandmsg.fragment.newInfo.task.NewInfoTask;
import com.xuexiang.mapandmsg.leancloud.Push;
import com.xuexiang.mapandmsg.notification.Note;
import com.xuexiang.mapandmsg.utils.Utils;
import com.xuexiang.mapandmsg.utils.XToastUtils;
import com.xuexiang.rxutil2.rxjava.RxJavaUtils;
import com.xuexiang.xaop.annotation.SingleClick;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xpage.base.XPageFragment;
import com.xuexiang.xpage.enums.CoreAnim;
import com.xuexiang.xrouter.annotation.AutoWired;
import com.xuexiang.xui.utils.WidgetUtils;
import com.xuexiang.xui.widget.button.ButtonView;
import com.xuexiang.xui.widget.dialog.LoadingDialog;
import com.xuexiang.xui.widget.edittext.MultiLineEditText;
import com.xuexiang.xui.widget.edittext.materialedittext.MaterialEditText;
import com.xuexiang.xui.widget.spinner.editspinner.EditSpinner;
import com.xuexiang.xutil.common.ObjectUtils;


import org.jetbrains.annotations.NotNull;

import java.io.Console;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

import butterknife.BindView;
import butterknife.OnClick;
import cn.leancloud.AVFile;
import cn.leancloud.AVOSCloud;
import cn.leancloud.AVObject;
import cn.leancloud.AVUser;
import cn.leancloud.types.AVGeoPoint;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * 发布消息输入框页面
 *
 * @author 夕子
 */
@Page(name = "消息发布", anim = CoreAnim.slide)
public class NewInfoReleaseFragment extends XPageFragment implements ImageSelectGridAdapter.OnAddPicClickListener {
    public static final String KEY_IS_NEED_BACK = "is_need_back";
    public static final String KEY_ADDRESS_LATITUDE_LONGITUDE = "poi__address_latitude_longitude";
    private static final String CHANNELID_PUBLIC = "public";
    @AutoWired(name = KEY_IS_NEED_BACK)
    boolean isNeedBack;

    @BindView(R.id.news_release_submit_bv)
    ButtonView buttonViewSubmit;
    @BindView(R.id.news_release_cancel_bv)
    ButtonView buttonViewCancel;
    @BindView(R.id.news_release_reset_bv)
    ButtonView buttonViewReset;
    @BindView(R.id.news_release_title_me)
    MaterialEditText materialEditTextTitle;
    @BindView(R.id.news_release_summery_mul_ed)
    MultiLineEditText multiLineEditTextSummery;
    @BindView(R.id.news_release_time)
    MaterialEditText materialEditTextTime;
    @BindView(R.id.news_release_address)
    MaterialEditText materialEditTextAddress;
    @BindView(R.id.news_release_phone_number)
    MaterialEditText materialEditTextPhoneNumber;
    @BindView(R.id.news_release_contacts)
    MaterialEditText materialEditTextContacts;
    @BindView(R.id.news_release_effective_time)
    Spinner spinner_effective_time;
    @BindView(R.id.news_release_random_position)
    Spinner spinner_random_position;

    //加载框
    LoadingDialog mLoadingDialog;
    //图片

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    private ImageSelectGridAdapter mAdapter;
    private List<LocalMedia> mSelectList = new ArrayList<>();

    private NewInfoTask mNewsReleaseTask;
    private LatLng latLng;
    AVUser user = AVUser.getCurrentUser();

    private RegeocodeTask mRegeocodeTask;
    private LatLonPoint latLonPoint;
    private GeocodeSearch mGeocodeSearch;
    private RegeocodeAddress mRegeocodeAddress;
    private static final float SEARCH_RADIUS = 50;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_news_release;
    }

    @Override
    protected void initViews() {
        Bundle arguments = getArguments();
        String str = arguments.getString(
                NewInfoReleaseFragment.KEY_ADDRESS_LATITUDE_LONGITUDE);
        String[] strings = str.split("\\|");
        String address = strings[0];
        latLng = new LatLng(Double.parseDouble(strings[1]),Double.parseDouble(strings[2]));

        //时间
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.CHINA);
        String currentTime = dateFormat.format(new Date());
        materialEditTextTime.setText(currentTime);
        //地址
        materialEditTextAddress.setText(address);
        //电话
        String phone = user.getMobilePhoneNumber().substring(3);
        materialEditTextPhoneNumber.setText(phone);
        //联系人
        materialEditTextContacts.setText(user.getUsername());

        //数据返回任务
        mNewsReleaseTask = NewInfoTask.getInstance();

        //有效时间默认值
        String[] items = getResources().getStringArray(R.array.news_release_effective_time);
        //声明一个下拉列表的数组适配器
        ArrayAdapter<String> visibleAdapter = new ArrayAdapter<>(Objects.requireNonNull(getActivity()), R.layout.item_select, items);
        //设置数组适配器的布局样式
        visibleAdapter.setDropDownViewResource(R.layout.item_dropdown);
        spinner_effective_time.setAdapter(visibleAdapter);
        spinner_effective_time.setSelection(0);
//        Spinner_number_visible.setOnItemClickListener(new MySelectedListener());

        //随机生成位置默认值
        String[] items_position = getResources().getStringArray(R.array.news_release_random_position);
        //声明一个下拉列表的数组适配器
        ArrayAdapter<String> visibleAdapter_position = new ArrayAdapter<>(Objects.requireNonNull(getActivity()), R.layout.item_select, items_position);
        //设置数组适配器的布局样式
        visibleAdapter_position.setDropDownViewResource(R.layout.item_dropdown);
        spinner_random_position.setAdapter(visibleAdapter_position);
        spinner_random_position.setSelection(0);



        //图片
        GridLayoutManager manager = new GridLayoutManager(getActivity(), 3, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(mAdapter = new ImageSelectGridAdapter(getActivity(), this));
        mAdapter.setSelectList(mSelectList);
        mAdapter.setSelectMax(8);
        mAdapter.setOnItemClickListener((position, v) -> PictureSelector.create(NewInfoReleaseFragment.this)
                .themeStyle(R.style.XUIPictureStyle)
                .openExternalPreview(position, mSelectList));

        mLoadingDialog = WidgetUtils.getLoadingDialog(Objects.requireNonNull(getContext()))
                .setLoadingIcon(R.drawable.ic_baseline_message_24)
                .setIconScale(0.4F)
                .setLoadingSpeed(8);
        mLoadingDialog.updateMessage("数据提交中...");
    }

    @Override
    protected void initListeners() {

    }

    @SuppressLint("NonConstantResourceId")
    @SingleClick
    @OnClick({R.id.news_release_cancel_bv, R.id.news_release_reset_bv, R.id.news_release_submit_bv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.news_release_cancel_bv:
                popToBack();
                break;
            case R.id.news_release_reset_bv:
                materialEditTextTitle.setText("");
                materialEditTextPhoneNumber.setText("");
                materialEditTextContacts.setText("");
                multiLineEditTextSummery.setContentText("");
                break;
            case R.id.news_release_submit_bv:
                submit();

                break;
            default:
                break;
        }
    }

    private void submit() {
        if (materialEditTextTitle.validate()) {
            if (materialEditTextPhoneNumber.validate()) {
                if (materialEditTextContacts.validate()) {
                    if (TextUtils.isEmpty(multiLineEditTextSummery.getContentText())) {
                        multiLineEditTextSummery.setContentText("无详细信息");
                    }
                    //开始上传数据
                    mLoadingDialog.show();
                    AVObject newsInfo = new AVObject("NewsInfo");
                    String date_time = materialEditTextTime.getEditValue();
                    newsInfo.put("title", materialEditTextTitle.getEditValue());
                    newsInfo.put("date", date_time);
//                    newsInfo.put("address", materialEditTextAddress.getEditValue());
                    newsInfo.put("phone", materialEditTextPhoneNumber.getEditValue());
                    newsInfo.put("contacts", materialEditTextContacts.getEditValue());
                    newsInfo.put("summery", multiLineEditTextSummery.getContentText());
//                    newsInfo.put("effective_time", spinner_effective_time.getSelectedItem().toString());
                    //有效时间转化成日期
                    String effective_date = CalDate(date_time);
                    newsInfo.put("effective_date", effective_date);

                    List<AVFile> avFileList = new ArrayList<>();
                    for (int i = 0; i < mSelectList.size(); i++) {
                        String imgName = "img"
                                + i
                                + "."
                                + mSelectList.get(i).getPictureType().substring(
                                mSelectList.get(i).getPictureType().indexOf("/") + 1);
                        try {
                            avFileList.add(AVFile.withAbsoluteLocalPath(imgName, mSelectList.get(i).getPath()));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        String avName = "image" + i;
                        newsInfo.put(avName, avFileList.get(i));
                    }
                    //地理点
                    AVGeoPoint point = new AVGeoPoint(latLng.latitude, latLng.longitude);
                    String random_position = spinner_random_position.getSelectedItem().toString();
                    point = RandomPoint(point, random_position);
                    newsInfo.put("location", point);

                    // 逆地理编码
                    double latitude = point.getLatitude();
                    double longitude = point.getLongitude();
                    String addr_res = getAddress(latitude, longitude);
                    System.out.println(addr_res);
                    newsInfo.put("address", addr_res);

                    //用户
                    AVObject owner = AVUser.getCurrentUser().getAVObject("Profile");
                    newsInfo.put("owner", owner);
                    newsInfo.saveInBackground().subscribe(new Observer<AVObject>() {
                        @Override
                        public void onSubscribe(@NotNull Disposable d) {

                        }

                        @Override
                        public void onNext(@NotNull AVObject avObject) {
                            XToastUtils.success("保存成功！");
                            String id = avObject.getObjectId();
                            // 设置返回的数据，类似Activity里的setResult
                            mNewsReleaseTask.NewsRelease(id);
                            CallBack.getInstance().callRefresh();
                            //发布推送
                            Push.pushAll(id);
                            new Note.GT_Notification(AVOSCloud.getContext())
                                    .setNotifyId(0x1)
                                    .setChanelId("1")
                                    .setChanelDescription("GT通知")
                                    .setChanelName("GT 名字")
                                    /**
                                     * 初始化 通知类
                                     *
                                     * @param icon       图标
                                     * @param title      标题
                                     * @param text       内容
                                     * @param time       设置发送通知的时间
                                     * @param voiceTF    是否设置声音振动
                                     * @param autoCancel 设置通知打开后自动消失
                                     * @param cla        设置单击后跳转的 页面
                                     * @return 返回 通知类
                                     */
                                    .sendingNotice(R.mipmap.ic_app1,
                                            AVOSCloud.getContext().getResources().getString(R.string.app_name),
                                            "有新的消息发布！点击打开应用查看。", 0, true, true, MainActivity.class);
                            //结束上传数据dialog
                            RxJavaUtils.delay(0, aLong -> mLoadingDialog.dismiss());
                            // 返回操作
                            popToBack();
                        }

                        @Override
                        public void onError(@NotNull Throwable e) {
                            //结束上传数据
                            RxJavaUtils.delay(0, aLong -> mLoadingDialog.dismiss());
                            XToastUtils.error("保存出错!");
                            Log.e("tag", e.getMessage());
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
                }
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            // 图片选择
            if (requestCode == PictureConfig.CHOOSE_REQUEST) {
                mSelectList = PictureSelector.obtainMultipleResult(data);
                mAdapter.setSelectList(mSelectList);
                mAdapter.notifyDataSetChanged();
            }
        }
    }
    @Override
    public void onAddPicClick() {
        Utils.getPictureSelector(this)
                .selectionMedia(mSelectList)
                .forResult(PictureConfig.CHOOSE_REQUEST);
    }
    @Override
    public void onDestroyView() {
        mLoadingDialog.recycle();
        super.onDestroyView();
    }

    //有效时间转化成日期
    public String CalDate(String date_time){
        String effective_date = null;
        String effective_time = spinner_effective_time.getSelectedItem().toString();
        if (effective_time.equals("无限制")){
            effective_date = "9999年12月31日 23:59:59";
        } else {
            effective_time = effective_time.replace("小时", "");
            int effective_time_temp = Integer.parseInt(effective_time);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                //String转换成日期
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss", Locale.CHINA);
                //SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                LocalDateTime date = LocalDateTime.parse(date_time, formatter);
                //加上有效日期
                LocalDateTime effective_date_temp = date.plusHours(effective_time_temp);
                //日期转换成String
                effective_date = effective_date_temp.format(formatter);
            }
        }
        return effective_date;
    }

    //随机生成位置
    public AVGeoPoint RandomPoint(AVGeoPoint point, String position) {
        double lat = point.getLatitude();   //纬度
        double lng = point.getLongitude();  //经度
        if (!Objects.equals(position, "否")){
            boolean isThousand = Objects.equals(position, "1000米");
            Random rand = new Random();
            // 根据是否生成1000米内的随机位置，计算生成随机位置的半径
            double radius = isThousand ? 1000 : 100;
            // 生成随机位置的纬度
            double latOffset = rand.nextDouble() * 2 - 1;
            double latDelta = latOffset / 111111.0 * radius;
            double newLat = lat + latDelta;
            // 生成随机位置的经度
            double lngOffset = rand.nextDouble() * 2 - 1;
            double lngDelta = lngOffset / (111111.0 * Math.cos(Math.toRadians(lat))) * radius;
            double newLng = lng + lngDelta;
            point.setLatitude(newLat);
            point.setLongitude(newLng);
            System.out.println("原来的经纬度:" + lat + "/" + lng);
            System.out.println("新的的经纬度:" + newLat + "/" + newLng);
        }
        return point;
    }

    //放入经纬度就可以了
    public String getAddress(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude,
                    longitude, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                String data = address.toString();
//                int startCity = data.indexOf("1:\"") + "1:\"".length();
//                int endCity = data.indexOf("\"", startCity);
//                String city = data.substring(startCity, endCity);
//
//                int startPlace = data.indexOf("feature=") + "feature=".length();
//                int endplace = data.indexOf(",", startPlace);
//                String place = data.substring(startPlace, endplace);
//                return city + place ;
                int startAddress = data.indexOf("addressLines=[0:\"") + "addressLines=[0:\"".length();
                int endAddress = data.indexOf("\"", startAddress);
                String addr = data.substring(startAddress, endAddress);
                return addr;

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "获取失败";
    }
}