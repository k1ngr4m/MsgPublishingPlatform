package com.xuexiang.mapandmsg.activity;

import android.os.Bundle;
import android.view.KeyEvent;

import com.amap.api.location.AMapLocation;
import com.xuexiang.mapandmsg.core.BaseActivity;
import com.xuexiang.mapandmsg.fragment.LoginFragment;
import com.xuexiang.xui.utils.KeyboardUtils;
import com.xuexiang.xui.utils.StatusBarUtils;
import com.xuexiang.xutil.display.Colors;

/**
 * 登录页面
 * @author 夕子
 */
public class LoginActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openPage(LoginFragment.class, getIntent().getExtras());
    }

    @Override
    protected boolean isSupportSlideBack() {
        return false;
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {

    }

    @Override
    protected void initStatusBarStyle() {
        StatusBarUtils.initStatusBarStyle(this, false, Colors.WHITE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return KeyboardUtils.onDisableBackKeyDown(keyCode) && super.onKeyDown(keyCode, event);
    }
}
