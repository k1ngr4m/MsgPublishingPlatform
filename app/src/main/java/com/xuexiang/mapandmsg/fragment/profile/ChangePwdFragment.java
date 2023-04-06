

package com.xuexiang.mapandmsg.fragment.profile;

import android.view.View;

import com.umeng.common.ISysListener;
import com.xuexiang.mapandmsg.R;
import com.xuexiang.mapandmsg.core.BaseFragment;
import com.xuexiang.mapandmsg.fragment.CallBack;
import com.xuexiang.mapandmsg.utils.XToastUtils;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xui.widget.actionbar.TitleBar;
import com.xuexiang.xui.widget.edittext.materialedittext.MaterialEditText;

import org.jetbrains.annotations.NotNull;

import java.io.Console;
import java.util.Objects;

import butterknife.BindView;
import cn.leancloud.AVObject;
import cn.leancloud.AVQuery;
import cn.leancloud.AVUser;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;


/**
 * 修改页面
 *
 * @author 夕子
 */
@Page(name = "修改密码")
public class ChangePwdFragment extends BaseFragment {
    @BindView(R.id.et_old_password)
    MaterialEditText etOldPassword;
    @BindView(R.id.et_new_password)
    MaterialEditText etNewPassword;
    @BindView(R.id.et_confirm_password)
    MaterialEditText etConfirmPassword;


    AVUser user = AVUser.getCurrentUser();
    String mobile_phone_number = "";
    String password = "";
    String new_password = "";
    String confirm_new_password = "";


    @Override
    protected TitleBar initTitle() {
        TitleBar titleBar = super.initTitle();
        titleBar.addAction(new TitleBar.TextAction("完成") {
            @Override
            public void performAction(View view) {
                password = etOldPassword.getEditValue();
                new_password = etNewPassword.getEditValue();
                confirm_new_password = etConfirmPassword.getEditValue();

                /*第一种方式（旧密码错误多的话会限制功能）*/
//                AVUser.loginByMobilePhoneNumber(mobile_phone_number, password).subscribe(new Observer<AVObject>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//
//                    }
//
//                    @Override
//                    public void onNext(AVObject avObject) {
//                        if(etNewPassword.validate() && etConfirmPassword.validate())
//                            if (Objects.equals(new_password, confirm_new_password)){
//                                user.put("password", new_password);
//                                user.saveInBackground().subscribe(new Observer<AVObject>() {
//                                    @Override
//                                    public void onSubscribe(@NotNull Disposable d) {
//
//                                    }
//
//                                    @Override
//                                    public void onNext(@NotNull AVObject avObject) {
//                                        XToastUtils.success("修改成功");
//                                        CallBack.getInstance().backToAccount();
//                                        CallBack.getInstance().callMainRefreshHead();
//                                        popToBack();
//                                    }
//
//                                    @Override
//                                    public void onError(@NotNull Throwable e) {
//                                        XToastUtils.error(e.getMessage());
//                                    }
//
//                                    @Override
//                                    public void onComplete() {
//
//                                    }
//                                });
//                            }
//                        else {
//                            XToastUtils.error("两次密码不一致");
//                            }
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        XToastUtils.error(e.getMessage());
//                    }
//
//                    @Override
//                    public void onComplete() {
//
//                    }
//                });

                /*第二种方式*/
                if(etNewPassword.validate() && etConfirmPassword.validate()){
                    if (Objects.equals(new_password, confirm_new_password)){
                        AVUser.loginByMobilePhoneNumber(mobile_phone_number, password).subscribe(new Observer<AVObject>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onNext(AVObject avObject) {
                                user.put("password", new_password);
                                user.saveInBackground().subscribe(new Observer<AVObject>() {
                                    @Override
                                    public void onSubscribe(@NotNull Disposable d) {

                                    }

                                    @Override
                                    public void onNext(@NotNull AVObject avObject) {
                                        XToastUtils.success("修改成功");
                                        CallBack.getInstance().backToAccount();
                                        CallBack.getInstance().callMainRefreshHead();
                                        popToBack();
                                    }

                                    @Override
                                    public void onError(@NotNull Throwable e) {
                                        XToastUtils.error(e.getMessage());
                                    }

                                    @Override
                                    public void onComplete() {

                                    }
                                });
                            }

                            @Override
                            public void onError(Throwable e) {
                                XToastUtils.error(e.getMessage());
                            }

                            @Override
                            public void onComplete() {

                            }
                        });
                    }
                    else {
                        XToastUtils.error("两次密码不一致");
                    }
                }
            }
        });
        return titleBar;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_changepwd;
    }

    @Override
    protected void initViews() {
        mobile_phone_number = user.getMobilePhoneNumber();
    }
}