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

package com.xuexiang.mapandmsg.fragment.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.entity.LocalMedia;
import com.xuexiang.mapandmsg.R;
import com.xuexiang.mapandmsg.core.BaseFragment;
import com.xuexiang.mapandmsg.fragment.AboutFragment;
import com.xuexiang.mapandmsg.fragment.CallBack;
import com.xuexiang.mapandmsg.fragment.SettingsFragment;
import com.xuexiang.mapandmsg.utils.Utils;
import com.xuexiang.mapandmsg.utils.XToastUtils;
import com.xuexiang.rxutil2.rxjava.RxJavaUtils;
import com.xuexiang.xaop.annotation.SingleClick;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xpage.enums.CoreAnim;
import com.xuexiang.xui.utils.ResUtils;
import com.xuexiang.xui.utils.WidgetUtils;
import com.xuexiang.xui.widget.actionbar.TitleBar;
import com.xuexiang.xui.widget.dialog.LoadingDialog;
import com.xuexiang.xui.widget.imageview.ImageLoader;
import com.xuexiang.xui.widget.imageview.RadiusImageView;
import com.xuexiang.xui.widget.imageview.strategy.DiskCacheStrategyEnum;
import com.xuexiang.xui.widget.textview.supertextview.SuperTextView;

import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.leancloud.AVFile;
import cn.leancloud.AVObject;
import cn.leancloud.AVQuery;
import cn.leancloud.AVUser;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * 个人页面
 *
 * @author 夕子
 */
@Page(anim = CoreAnim.none)
public class ProfileFragment extends BaseFragment implements SuperTextView.OnSuperTextViewClickListener {
    @BindView(R.id.riv_head_pic)
    RadiusImageView rivHeadPic;
    @BindView(R.id.menu_settings)
    SuperTextView menuSettings;
    @BindView(R.id.menu_about)
    SuperTextView menuAbout;
    List<LocalMedia> mSelectList;
    AVUser user = AVUser.getCurrentUser();
    /**
     * 加载框
     */
    LoadingDialog mLoadingDialog;
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case 10001:
                    setHead();
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    /**
     * @return 返回为 null意为不需要导航栏
     */
    @Override
    protected TitleBar initTitle() {
        return null;
    }

    /**
     * 布局的资源id
     *
     * @return
     */
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_profile;
    }

    /**
     * 初始化控件
     */
    @Override
    protected void initViews() {
        setHead();
        mLoadingDialog = WidgetUtils.getLoadingDialog(getContext())
                .setLoadingIcon(R.drawable.ic_baseline_message_24)
                .setIconScale(0.4F)
                .setLoadingSpeed(8);
        mLoadingDialog.updateMessage("照片上传中...");
    }

    @Override
    protected void initListeners() {
        menuSettings.setOnSuperTextViewClickListener(this);
        menuAbout.setOnSuperTextViewClickListener(this);

    }

    @SingleClick
    @Override
    @OnClick({R.id.profile_head_st, R.id.menu_account_set})
    public void onClick(SuperTextView view) {
        switch (view.getId()) {
            case R.id.menu_settings:
                openNewPage(SettingsFragment.class);
                break;
            case R.id.menu_about:
                openNewPage(AboutFragment.class);
                break;
            case R.id.profile_head_st:
                Utils.getPictureSelector(this)
                        .selectionMedia(mSelectList)
                        .maxSelectNum(1)
                        .selectionMode(PictureConfig.SINGLE)
                        .forResult(PictureConfig.CHOOSE_REQUEST);
                break;
            case R.id.menu_account_set:
                openNewPage(AccountFragment.class);
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            // 图片选择
            if (requestCode == PictureConfig.CHOOSE_REQUEST) {
                mSelectList = PictureSelector.obtainMultipleResult(data);
                saveHead();


            }
        }
    }
    private void setHead() {
        AVObject avObject = user.getAVObject("Profile");
        AVQuery<AVObject> query = new AVQuery<>("Profile");
        query.getInBackground(avObject.getObjectId()).subscribe(new Observer<AVObject>() {
            @Override
            public void onSubscribe(Disposable disposable) {}
//            @Override
//            public void onNext(AVObject todo) {
//                AVFile file = todo.getAVFile("head");
//                if (file != null) {
//                    String uri = file.getUrl();
//                    ImageLoader.get().loadImage(rivHeadPic, uri, ResUtils.getDrawable(
//                            R.drawable.xui_ic_default_img), DiskCacheStrategyEnum.AUTOMATIC);
//                    Log.e("tag:main","file is null");
//                }
//            }
            @Override
            public void onNext(AVObject todo) {
                String file = "http://lc-mbiWXKga.cn-n1.lcfile.com/q5b2OAvMXYIN9ozkDzWqsbpbVbwUAkzO/head.png";
                ImageLoader.get().loadImage(rivHeadPic, file);
                Log.e("tag:main","file is null");
            }
            @Override
            public void onError(Throwable throwable) {}
            @Override
            public void onComplete() {}
        });
    }

    private void saveHead() {
        //开始上传数据
        mLoadingDialog.show();
        AVFile file = null;
        String imgName = "head"
                + "."
                + mSelectList.get(0).getPictureType().substring(
                mSelectList.get(0).getPictureType().indexOf("/") + 1);
        try {
            file = AVFile.withAbsoluteLocalPath(imgName, mSelectList.get(0).getPath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        AVObject profileAvObject = (AVObject) user.get("Profile");
        if (profileAvObject != null) {
            profileAvObject.put("head", file);
            profileAvObject.saveInBackground().subscribe(new Observer<AVObject>() {
                @Override
                public void onSubscribe(@NotNull Disposable d) {

                }

                @Override
                public void onNext(@NotNull AVObject avObject) {
                    Message message = new Message();
                    message.what = 10001;
                    handler.sendMessage(message);
                    RxJavaUtils.delay(0, aLong -> mLoadingDialog.dismiss());
                    XToastUtils.success("头像保存成功！");
                    CallBack.getInstance().callMainRefreshHead();
                }

                @Override
                public void onError(@NotNull Throwable e) {
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLoadingDialog != null) {
            mLoadingDialog.dismiss();
        }
    }

}
