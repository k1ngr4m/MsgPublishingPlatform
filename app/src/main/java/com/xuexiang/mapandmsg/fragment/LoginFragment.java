/*
 * Copyright (C) 2020 xuexiangjys(xuexiangjys@163.com)
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

package com.xuexiang.mapandmsg.fragment;

import android.content.Context;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import androidx.fragment.app.Fragment;

import com.xuexiang.mapandmsg.R;
import com.xuexiang.mapandmsg.activity.MainActivity;
import com.xuexiang.mapandmsg.core.BaseFragment;
import com.xuexiang.mapandmsg.utils.RandomUtils;
import com.xuexiang.mapandmsg.utils.SettingUtils;
import com.xuexiang.mapandmsg.utils.TokenUtils;
import com.xuexiang.mapandmsg.utils.Utils;
import com.xuexiang.mapandmsg.utils.XToastUtils;
import com.xuexiang.mapandmsg.utils.sdkinit.UMengInit;
import com.xuexiang.xaop.annotation.SingleClick;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xpage.enums.CoreAnim;
import com.xuexiang.xui.utils.CountDownButtonHelper;
import com.xuexiang.xui.utils.ResUtils;
import com.xuexiang.xui.utils.ThemeUtils;
import com.xuexiang.xui.widget.actionbar.TitleBar;
import com.xuexiang.xui.widget.button.roundbutton.RoundButton;
import com.xuexiang.xui.widget.edittext.materialedittext.MaterialEditText;
import com.xuexiang.xutil.app.ActivityUtils;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.OnClick;
import cn.leancloud.AVObject;
import cn.leancloud.AVQuery;
import cn.leancloud.AVUser;
import cn.leancloud.sms.AVSMS;
import cn.leancloud.sms.AVSMSOption;
import cn.leancloud.types.AVNull;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;


/**
 * 登录页面fragment
 * @author 夕子
 */
@Page(anim = CoreAnim.none)
public class LoginFragment extends BaseFragment {

    @BindView(R.id.et_phone_number)
    MaterialEditText etPhoneNumber;
    @BindView(R.id.et_verify_code)
    MaterialEditText etVerifyCode;
    @BindView(R.id.btn_get_verify_code)
    RoundButton btnGetVerifyCode;

    @BindView(R.id.fl_verify_code)
    FrameLayout flVerifyCode;

    @BindView(R.id.et_password)
    MaterialEditText etPassword;

    @BindView(R.id.fl_password)
    FrameLayout flPassword;

    private CountDownButtonHelper mCountDownHelper;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_login;
    }

    @Override
    protected TitleBar initTitle() {
        TitleBar titleBar = super.initTitle()
                .setImmersive(true);
        titleBar.setBackgroundColor(Color.TRANSPARENT);
        titleBar.setTitle("");
        titleBar.setLeftImageDrawable(ResUtils.getVectorDrawable(getContext(), R.drawable.ic_login_close));
        titleBar.setActionTextColor(ThemeUtils.resolveColor(getContext(), R.attr.colorAccent));
        titleBar.addAction(new TitleBar.TextAction(R.string.title_jump_login) {
            @Override
            public void performAction(View view) {
                //onLoginSuccess();
            }
        });
        return titleBar;
    }

    @Override
    protected void initViews() {
        //倒计时类
        mCountDownHelper = new CountDownButtonHelper(btnGetVerifyCode, 60);

        //隐私政策弹窗
        if (!SettingUtils.isAgreePrivacy()) {
            Utils.showPrivacyDialog(getContext(), (dialog, which) -> {
                dialog.dismiss();
                SettingUtils.setIsAgreePrivacy(true);
                UMengInit.init(getContext());
            });
        }
        getIpFromWifi(getContext());
    }

    @SingleClick
    @OnClick({R.id.btn_get_verify_code, R.id.btn_login, R.id.tv_other_login, R.id.tv_forget_password, R.id.tv_user_protocol, R.id.tv_privacy_protocol})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_get_verify_code:
                if (etPhoneNumber.validate()) {
                    getVerifyCode(etPhoneNumber.getEditValue());
                }
                break;
            case R.id.btn_login:
                if (etPhoneNumber.validate()) {
                    // 如果是验证码登录
                    if (flVerifyCode.getVisibility() == View.VISIBLE) {
                        if (etVerifyCode.validate()) {
                            loginByVerifyCode(etPhoneNumber.getEditValue(), etVerifyCode.getEditValue());
                        }
                    }else {
                        loginByPassword(etPhoneNumber.getEditValue(), etPassword.getEditValue());
                    }
                }
                break;
            case R.id.tv_other_login:
                // 换成密码登录
                if (flPassword.getVisibility() == View.GONE){
                    flPassword.setVisibility(view.VISIBLE);
                    flVerifyCode.setVisibility(view.GONE);}
                else{
                    // 换成验证码登录
                    flPassword.setVisibility(view.GONE);
                    flVerifyCode.setVisibility(view.VISIBLE);
                }
                break;
                // todo 用户设计页面再改
//            case R.id.tv_forget_password:
//                openNewPage(ChangePwdFragment.class);
//                break;
            case R.id.tv_user_protocol:
                XToastUtils.info("用户协议");
                Utils.showPrivacyDialog(getContext(), (dialog, which) -> {
                    dialog.dismiss();
                    SettingUtils.setIsAgreePrivacy(true);
                    UMengInit.init(getContext());
                });
                break;
            case R.id.tv_privacy_protocol:
                XToastUtils.info("隐私政策");
                Utils.showPrivacyDialog(getContext(), (dialog, which) -> {
                    dialog.dismiss();
                    SettingUtils.setIsAgreePrivacy(true);
                    UMengInit.init(getContext());
                });
                break;
            default:
                break;
        }
    }

    private void showDebug(String str){
        Log.e("TAG",str);
    }
    private void getUserCount(){
        AVQuery<AVUser> userQuery = AVUser.getQuery();
        userQuery.countInBackground().subscribe(new Observer<Integer>() {
            @Override
            public void onSubscribe(@NotNull Disposable d) {

            }

            @Override
            public void onNext(@NotNull Integer integer) {
                showDebug(String.valueOf(integer));
            }

            @Override
            public void onError(@NotNull Throwable e) {
                showDebug(e.getMessage());
            }

            @Override
            public void onComplete() {

            }
        });
    }

    /**
     * 注册获取验证码
     * @param phoneNumber
     */
    private void getRegisterVerifyCode(String phoneNumber){
        AVSMSOption option = new AVSMSOption();
        //设置短信签名名称
        option.setSignatureName("sign_name");
        AVSMS.requestSMSCodeInBackground("+86" + phoneNumber,
                option).subscribe(new Observer<AVNull>() {
            @Override
            public void onSubscribe(Disposable disposable) {
            }
            @Override
            public void onNext(AVNull avNull) {
                Log.d("TAG","Result: succeed to request SMSCode.");
            }
            @Override
            public void onError(Throwable throwable) {
                Log.d("TAG","Result: failed to request SMSCode. cause:"
                        + throwable.getMessage());
            }
            @Override
            public void onComplete() {
            }
        });
    }
    /**
     * 获取验证码
     */
    private void getVerifyCode(String phoneNumber) {
        AVUser.requestLoginSmsCodeInBackground("+86" + phoneNumber
        ).subscribe(new Observer<AVNull>() {
            @Override
            public void onSubscribe(Disposable disposable) {}

            @Override
            public void onNext(AVNull avNull) {
                XToastUtils.success("验证码发送成功！");
            }
            @Override
            public void onError(Throwable throwable) {
                XToastUtils.error("验证码发送失败！");
                Log.e("TAG:","Result: failed to request SMSCode. cause:" +
                        throwable.getMessage()
                + "\n hashCode:" + throwable.hashCode());
            }
            @Override
            public void onComplete() {}
        });
        //倒计时开始
        mCountDownHelper.start();
    }

    /**
     * 根据验证码注册
     *
     * @param phoneNumber 手机号
     * @param verifyCode  验证码
     */
    private void loginByVerifyCode(String phoneNumber, String verifyCode) {
        AVUser.signUpOrLoginByMobilePhoneInBackground("+86" + phoneNumber,
                verifyCode).subscribe(new Observer<AVUser>() {
            @Override
            public void onSubscribe(Disposable disposable) {}
            @Override
            public void onNext(AVUser user) {
                user.put("ip", getIpFromWifi(getContext()));
                //在_user与profile之间简历一对一的关系
                if(user.get("Profile") == null){
                    AVObject avObject = new AVObject("Profile");
                    avObject.put("username",user.getUsername());
                    avObject.saveInBackground().subscribe(new Observer<AVObject>() {
                        @Override
                        public void onSubscribe(@NotNull Disposable d) {

                        }

                        @Override
                        public void onNext(@NotNull AVObject avObject) {
                            user.put("Profile",avObject);
                            user.saveInBackground().subscribe();
                        }

                        @Override
                        public void onError(@NotNull Throwable e) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
                }

                // 登录成功
                XToastUtils.success("登录成功！");
                onLoginSuccess();
            }
            @Override
            public void onError(Throwable throwable) {
                // 验证码不正确
                XToastUtils.error("登录失败！" +
                        throwable.getMessage());
                showDebug("登录失败！" +
                        throwable.getMessage());
            }
            @Override
            public void onComplete() {}
        });

        // TODO: 2020/8/29 这里只是界面演示而已

    }

    /**
     * 根据密码登录
     * @param phoneNumber 手机号
     * @param password 密码
     */

    private void loginByPassword(String phoneNumber, String password) {
        AVUser.loginByMobilePhoneNumber("+86" + phoneNumber,password).subscribe(new Observer<AVUser>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(AVUser avUser) {
                avUser.put("ip", getIpFromWifi(getContext()));
                // 登录成功
                XToastUtils.success("登录成功！");
                onLoginSuccess();
            }

            @Override
            public void onError(Throwable e) {
                // 密码不正确
                XToastUtils.error("登录失败！" +
                        e.getMessage());
                showDebug("登录失败！" +
                        e.getMessage());
            }

            @Override
            public void onComplete() {

            }
        });

    }

    /**
     * 登录成功的处理
     */
    private void onLoginSuccess() {
        String token = RandomUtils.getRandomNumbersAndLetters(16);
        if (TokenUtils.handleLoginSuccess(token)) {
            popToBack();//弹出栈顶的Fragment。如果Activity中只有一个Fragment时，Activity也退出
            ActivityUtils.startActivity(MainActivity.class);
        }
    }

    @Override
    public void onDestroyView() {
        if (mCountDownHelper != null) {
            mCountDownHelper.recycle();
        }
        super.onDestroyView();
    }

    private String getIpFromWifi(Context context){
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        String ipAddr = intToIp(ipAddress);
        XToastUtils.toast(ipAddr);
        System.out.println("IP Address: " + ipAddress);
        return ipAddr;
    }

    private String intToIp(int i) {
        return (i & 0xFF) + "." + (( i >> 8 ) & 0xFF) + "." +( ( i >> 16 ) & 0xFF) + "." + ( i >> 24  & 0xFF);
    }

}

