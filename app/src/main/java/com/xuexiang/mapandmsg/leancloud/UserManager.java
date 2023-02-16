package com.xuexiang.mapandmsg.leancloud;

import android.util.Log;

import com.xuexiang.mapandmsg.utils.XToastUtils;

import cn.leancloud.AVUser;
import cn.leancloud.sms.AVSMS;
import cn.leancloud.sms.AVSMSOption;
import cn.leancloud.types.AVNull;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * 管理登录用户
 * @author 夕子
 */
public class UserManager {
    private  static  UserManager userManager;
    private UserManager(){}
    public static UserManager get(){
        if(userManager == null){
            userManager = new UserManager();
        }
        return userManager;
    }

    private static AVUser user = new AVUser();
    public void saveUser(AVUser user){
        UserManager.user = user;
    }
    public AVUser getUser(){
        return UserManager.user;
    }
    public void removeUser(){
        UserManager.user = null;
    }

    /**
     * 根据电话获取验证码
     * @param phoneNumber
     */
    public void getVerifyCodeByPhone(String phoneNumber){
        AVSMSOption option = new AVSMSOption();
        //设置短信签名名称
        option.setSignatureName("sign_name");
        AVSMS.requestSMSCodeInBackground("+86" + phoneNumber, option).subscribe(
                new Observer<AVNull>() {
                    @Override
                    public void onSubscribe(Disposable disposable) {
                    }
                    @Override
                    public void onNext(AVNull avNull) {
                        XToastUtils.success("Result: succeed to request SMSCode.");
                    }
                    @Override
                    public void onError(Throwable throwable) {
                        XToastUtils.error("Result: failed to request SMSCode. cause:" +
                                throwable.getMessage());
                        Log.e("TAG:","Result: failed to request SMSCode. cause:" +
                                throwable.getMessage());
                    }
                    @Override
                    public void onComplete() {
                    }
                });
    }
    /**
     * 通过手机号注册
     * @param phoneNumber
     * @param verifyCode
     */
    public void registerByPhone(String phoneNumber, String verifyCode){
        AVUser.signUpOrLoginByMobilePhoneInBackground("+86" + phoneNumber,
                verifyCode).subscribe(new Observer<AVUser>() {
            @Override
            public void onSubscribe(Disposable disposable) {}
            @Override
            public void onNext(AVUser user) {
                // 注册成功
                XToastUtils.success("注册成功。objectId：" + user.getObjectId());
                loginByPhone(phoneNumber,verifyCode);
            }
            @Override
            public void onError(Throwable throwable) {
                // 验证码不正确
                XToastUtils.error("验证码错误！");
            }
            @Override
            public void onComplete() {}
        });
    }

    /**
     * 通过手机号登录
     * @param phoneNumber
     * @param verifyCode
     */
    public boolean loginByPhone(String phoneNumber, String verifyCode){
        final boolean[] isSuccessLogin = {false};
        AVUser.loginByMobilePhoneNumber("+86" + phoneNumber,
                verifyCode).subscribe(new Observer<AVUser>() {
            @Override
            public void onSubscribe(Disposable disposable) {}
            @Override
            public void onNext(AVUser user) {
                // 登录成功
                XToastUtils.success("登录成功!");
                isSuccessLogin[0] = true;
                get().saveUser(user);
            }
            @Override
            public void onError(Throwable throwable) {
                // 登录失败（可能是密码错误）
            }
            @Override
            public void onComplete() {}
        });
        return isSuccessLogin[0];
    }
}
