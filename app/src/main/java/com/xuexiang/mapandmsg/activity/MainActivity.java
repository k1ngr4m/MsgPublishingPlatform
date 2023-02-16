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

package com.xuexiang.mapandmsg.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.xuexiang.mapandmsg.R;
import com.xuexiang.mapandmsg.core.BaseActivity;
import com.xuexiang.mapandmsg.core.BaseFragment;
import com.xuexiang.mapandmsg.fragment.AboutFragment;
import com.xuexiang.mapandmsg.fragment.CallBack;
import com.xuexiang.mapandmsg.fragment.SettingsFragment;
import com.xuexiang.mapandmsg.fragment.map.MapFragment;
import com.xuexiang.mapandmsg.fragment.news.NewsFragment;
import com.xuexiang.mapandmsg.fragment.profile.AccountFragment;
import com.xuexiang.mapandmsg.fragment.profile.ProfileFragment;
import com.xuexiang.mapandmsg.page.NoScrollViewPager;
import com.xuexiang.mapandmsg.utils.Utils;
import com.xuexiang.mapandmsg.utils.XToastUtils;
import com.xuexiang.xaop.annotation.SingleClick;
import com.xuexiang.xui.adapter.FragmentAdapter;
import com.xuexiang.xui.utils.ResUtils;
import com.xuexiang.xui.utils.ThemeUtils;
import com.xuexiang.xui.widget.guidview.GuideCaseQueue;
import com.xuexiang.xui.widget.guidview.GuideCaseView;
import com.xuexiang.xui.widget.imageview.ImageLoader;
import com.xuexiang.xui.widget.imageview.RadiusImageView;
import com.xuexiang.xui.widget.imageview.strategy.DiskCacheStrategyEnum;
import com.xuexiang.xutil.XUtil;
import com.xuexiang.xutil.common.ClickUtils;
import com.xuexiang.xutil.common.CollectionUtils;
import com.xuexiang.xutil.display.Colors;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import cn.leancloud.AVFile;
import cn.leancloud.AVInstallation;
import cn.leancloud.AVObject;
import cn.leancloud.AVQuery;
import cn.leancloud.AVUser;
import cn.leancloud.push.PushService;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * 主页面
 *
 * @author 夕子
 */
public class MainActivity extends BaseActivity implements View.OnClickListener, 
        BottomNavigationView.OnNavigationItemSelectedListener, ClickUtils.OnClick2ExitListener, 
        Toolbar.OnMenuItemClickListener, CallBack.OnRefreshHeadListener ,CallBack.OnBackShowToMapListener{

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.view_pager)
    NoScrollViewPager viewPager;
    /**
     * 底部导航栏
     */
    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomNavigation;
    /**
     * 侧边栏
     */
    @BindView(R.id.nav_view)
    NavigationView navView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    AVUser user = AVUser.getCurrentUser();
    private String[] mTitles;
    private RadiusImageView  ivAvatar;
    private TextView tvAvatar;
    private TextView tvSign;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initViews();

        initListeners();

        initPush();
    }

    private void initPush() {
        // 设置默认打开的 Activity
        PushService.setDefaultPushCallback(this, MainActivity.class);

        // 订阅频道，当该频道消息到来的时候，打开对应的 Activity
        PushService.subscribe(this, "public", MainActivity.class);
        PushService.subscribe(this, "private", MainActivity.class);
        PushService.subscribe(this, "protected", MainActivity.class);
        log("这个设备的id：" + AVInstallation.getCurrentInstallation().getInstallationId());
        AVInstallation.getCurrentInstallation().saveInBackground().subscribe(new Observer<AVObject>() {
            @Override
            public void onSubscribe(Disposable d) {
            }
            @Override
            public void onNext(AVObject avObject) {
                // 关联 installationId 到用户表等操作。
                user.put("installation",AVInstallation.getCurrentInstallation());
                user.saveInBackground().subscribe(new Observer<AVObject>() {
                    @Override
                    public void onSubscribe(@NotNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NotNull AVObject avObject) {
                            log("关联 installation 到用户表成功");
                    }

                    @Override
                    public void onError(@NotNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
                String installationId = AVInstallation.getCurrentInstallation().getInstallationId();
                log("installation保存成功：" + installationId);
            }
            @Override
            public void onError(Throwable e) {
                log("installationId保存成功保存失败，错误信息：" + e.getMessage());
            }
            @Override
            public void onComplete() {
            }
        });

    }

    @Override
    protected boolean isSupportSlideBack() {
        return false;
    }

    private void initViews() {
        mTitles = ResUtils.getStringArray(R.array.home_titles);
        toolbar.setTitle(mTitles[0]);
        toolbar.inflateMenu(R.menu.menu_main);
        toolbar.setOnMenuItemClickListener(this);

        initHeader();

        //主页内容填充
        BaseFragment[] fragments = new BaseFragment[]{
                new MapFragment(),
                new NewsFragment(),
                new ProfileFragment()
        };
        FragmentAdapter<BaseFragment> adapter = new FragmentAdapter<>(getSupportFragmentManager(), fragments);
        viewPager.setOffscreenPageLimit(mTitles.length - 1);
        viewPager.setAdapter(adapter);
        //GuideTipsDialog.showTips(this);
        CallBack.getInstance().setOnBackShowToMapListener(this);
    }

    private void setHead(ImageView imageView) {
        AVObject avObject = user.getAVObject("Profile");
//        AVObject avObject = new AVObject("Profile");
        AVQuery<AVObject> query = new AVQuery<>("Profile");
        query.getInBackground(avObject.getObjectId()).subscribe(new Observer<AVObject>() {
            @Override
            public void onSubscribe(Disposable disposable) {}
            @Override
            public void onNext(AVObject todo) {
               AVFile file = todo.getAVFile("head");
                if (file != null) {
                    String uri = file.getUrl();
                    ImageLoader.get().loadImage(imageView, uri, ResUtils.getDrawable(
                            R.drawable.xui_ic_default_img), DiskCacheStrategyEnum.AUTOMATIC);
                }else {
                    Log.e("tag:main","file is null");
                }

            }
            @Override
            public void onError(Throwable throwable) {}
            @Override
            public void onComplete() {}
        });

//        String username = user.getString("username");
//        String phone = user.getString("mobilePhoneNumber");
//        AVQuery<AVObject> query1 = new AVQuery<>("Profile");
//        query1.whereEqualTo("username",username);
//        AVQuery<AVObject> query2 = new AVQuery<>("Profile");
//        query2.whereEqualTo("mobilePhoneNumber",phone);
//        AVQuery<AVObject> query3 = AVQuery.or(Arrays.asList(query1, query2));
//        query3.findInBackground().subscribe(new Observer<List<AVObject>>() {
//            @Override
//            public void onSubscribe(@NotNull Disposable d) {
//
//            }
//
//            @Override
//            public void onNext(@NotNull List<AVObject> avObjects) {
//                for(AVObject avObject: avObjects){
//                    XToastUtils.success(avObject.getObjectId());
//                    log(avObject.getObjectId());
//                }

//                if(avObjects != null) {
//                    if(avObjects.get(0) != null) {
//                        if(avObjects.get(0).get("username") != null) {
//                            XToastUtils.success(avObjects.get(0).get("username").toString());
//                        }
//                    }
//                }
//                AVFile file = avObjects.get(0).getAVFile("head");
//                if (file != null) {
//                    String uri = file.getUrl();
//                    ImageLoader.get().loadImage(imageView, uri, ResUtils.getDrawable(
//                            R.drawable.xui_ic_default_img), DiskCacheStrategyEnum.AUTOMATIC);
//                }else {
//                    Log.e("tag:main","file is null");
//                }
//            }
//
//            @Override
//            public void onError(@NotNull Throwable e) {
//                XToastUtils.error("head");
//                Log.e("tag:main",e.getMessage());
//            }
//
//            @Override
//            public void onComplete() {
//
//            }
//        });

    }

    private void initHeader() {
        navView.setItemIconTintList(null);
        View headerView = navView.getHeaderView(0);
        LinearLayout navHeader = headerView.findViewById(R.id.nav_header);
        ivAvatar = headerView.findViewById(R.id.iv_avatar);
        tvAvatar = headerView.findViewById(R.id.tv_avatar);
        tvSign = headerView.findViewById(R.id.tv_sign);

        if (Utils.isColorDark(ThemeUtils.resolveColor(this, R.attr.colorAccent))) {
            tvAvatar.setTextColor(Colors.WHITE);
            tvSign.setTextColor(Colors.WHITE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ivAvatar.setImageTintList(ResUtils.getColors(R.color.xui_config_color_white));
            }
        } else {
            tvAvatar.setTextColor(ThemeUtils.resolveColor(this, R.attr.xui_config_color_title_text));
            tvSign.setTextColor(ThemeUtils.resolveColor(this, R.attr.xui_config_color_explain_text));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ivAvatar.setImageTintList(ResUtils.getColors(R.color.xui_config_color_gray_3));
            }
        }

        // 初始化数据
        setHead(ivAvatar);
        tvAvatar.setText(user.get("username").toString());
        tvSign.setText(user.get("signature") == null ? "这个家伙很懒，什么也没有留下～～"
                : user.get("signature").toString());
        navHeader.setOnClickListener(this);
    }

    protected void initListeners() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        //侧边栏点击事件
        navView.setNavigationItemSelectedListener(menuItem -> {
            if (menuItem.isCheckable()) {
                drawerLayout.closeDrawers();
                return handleNavigationItemSelected(menuItem);
            } else {
                switch (menuItem.getItemId()) {
                    case R.id.nav_settings:
                        openNewPage(SettingsFragment.class);
                        break;
                    case R.id.nav_about:
                        openNewPage(AboutFragment.class);
                        break;
                    case R.id.nav_search:
                        Intent intent = new Intent(this, SearchViewActivity.class);
                        startActivity(intent);
                    default:
                        XToastUtils.toast("点击了:" + menuItem.getTitle());
                        break;
                }
            }
            return true;
        });
        //主页事件监听
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                MenuItem item = bottomNavigation.getMenu().getItem(position);
                toolbar.setTitle(item.getTitle());
                item.setChecked(true);
                updateSideNavStatus(item);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        bottomNavigation.setOnNavigationItemSelectedListener(this);
        //更新头像监听
        CallBack.getInstance().setOnRefreshHeadListener(this);
    }

    /**
     * 处理侧边栏点击事件
     *
     * @param menuItem
     * @return
     */
    private boolean handleNavigationItemSelected(@NonNull MenuItem menuItem) {
        int index = CollectionUtils.arrayIndexOf(mTitles, menuItem.getTitle());
        if (index != -1) {
            toolbar.setTitle(menuItem.getTitle());
            //切换页面
            viewPager.setCurrentItem(index, false);
            return true;
        }
        return false;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_privacy:
                //GuideTipsDialog.showTipsForce(this);
                showTextGuideView();
                break;
            case R.id.action_about:
                openNewPage(AboutFragment.class);
                break;
            default:
                break;
        }
        return false;
    }
    public void showTextGuideView() {
//        new GuideCaseView.Builder(getActivity())
//                .picture(R.drawable.img_guidecaseview_gain_speed_gesture)
//                .build()
//                .show();
        final GuideCaseView guideStep1 = new GuideCaseView.Builder(this)
                .title("切换地图模式按钮")
                .focusRectAtPosition(980, 1450, 140, 140)
                .roundRectRadius(90)
                .build();
        final GuideCaseView guideStep2 = new GuideCaseView.Builder(this)
                .title("筛选消息范围按钮")
                .focusRectAtPosition(980, 1600, 140, 140)
                .roundRectRadius(90)
                .build();
        final GuideCaseView guideStep3 = new GuideCaseView.Builder(this)
                .title("发布消息按钮")
                .focusRectAtPosition(980, 1750, 140, 140)
                .roundRectRadius(90)
                .build();
        final GuideCaseView guideStep4 = new GuideCaseView.Builder(this)
                .title("定位按钮")
                .focusRectAtPosition(980, 1900, 140, 140)
                .roundRectRadius(90)
                .build();
        final GuideCaseView guideStep5 = new GuideCaseView.Builder(this)
                .title("放大地图按钮")
                .focusRectAtPosition(100, 1800, 140, 140)
                .roundRectRadius(90)
                .build();
        final GuideCaseView guideStep6 = new GuideCaseView.Builder(this)
                .title("缩小地图按钮")
                .focusRectAtPosition(100, 1900, 140, 140)
                .roundRectRadius(90)
                .build();
        final GuideCaseView guideStepBig = new GuideCaseView.Builder(this)
                .title("搜索地点按钮")
                .focusRectAtPosition(950, 280, 160, 160)
                .roundRectRadius(90)
                .build();
        final GuideCaseView guideStepText = new GuideCaseView.Builder(this)
                .title("这里是发布消息的位置，也是屏幕中心指针的位置")
                .focusRectAtPosition(450, 280, 800, 150)
                .roundRectRadius(60)
                .build();
        new GuideCaseQueue()
                .add(guideStep1)
                .add(guideStep2)
                .add(guideStep3)
                .add(guideStep4)
                .add(guideStep5)
                .add(guideStep6)
                .add(guideStepBig)
                .add(guideStepText)
                .show();

    }

    @SingleClick
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nav_header:
                openNewPage(AccountFragment.class);
                break;
            default:
                break;
        }
    }

    //================Navigation================//

    /**
     * 底部导航栏点击事件
     *
     * @param menuItem
     * @return
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int index = CollectionUtils.arrayIndexOf(mTitles, menuItem.getTitle());
        if (index != -1) {
            toolbar.setTitle(menuItem.getTitle());
            //切换页面
            viewPager.setCurrentItem(index, false);

            updateSideNavStatus(menuItem);
            return true;
        }
        return false;
    }

    /**
     * 更新侧边栏菜单选中状态
     *
     * @param menuItem
     */
    private void updateSideNavStatus(MenuItem menuItem) {
        MenuItem side = navView.getMenu().findItem(menuItem.getItemId());
        if (side != null) {
            side.setChecked(true);
        }
    }

    /**
     * 菜单、返回键响应
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ClickUtils.exitBy2Click(2000, this);
        }
        return true;
    }

    @Override
    public void onRetry() {
        XToastUtils.toast("再按一次退出程序");
    }

    @Override
    public void onExit() {
        XUtil.exitApp();
    }


    @Override
    public void onRefreshHead() {
        // 初始化数据
        setHead(ivAvatar);
        tvAvatar.setText(user.get("username").toString());
        tvSign.setText(user.get("signature") == null ? "这个家伙很懒，什么也没有留下～～"
                : user.get("signature").toString());
    }

    @Override
    public void onBackShowToMap() {
        //切换页面
        viewPager.setCurrentItem(0, false);
    }

    void log(String s){
        Log.e("Debug(main)",s);
    }
}
