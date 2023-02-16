

package com.xuexiang.mapandmsg.fragment.profile;

import android.view.View;

import com.xuexiang.mapandmsg.R;
import com.xuexiang.mapandmsg.core.BaseFragment;
import com.xuexiang.mapandmsg.fragment.CallBack;
import com.xuexiang.mapandmsg.utils.XToastUtils;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xui.widget.actionbar.TitleBar;
import com.xuexiang.xui.widget.edittext.materialedittext.MaterialEditText;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import cn.leancloud.AVObject;
import cn.leancloud.AVUser;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;


/**
 * 修改页面
 *
 * @author 夕子
 */
@Page(name = "用户名")
public class ModifyNameFragment extends BaseFragment {
    @BindView(R.id.modify_username_mt)
    MaterialEditText materialEditTextUsername;
    AVUser user = AVUser.getCurrentUser();
    String name = "";

    @Override
    protected TitleBar initTitle() {
        TitleBar titleBar = super.initTitle();
        titleBar.addAction(new TitleBar.TextAction("完成") {
            @Override
            public void performAction(View view) {
                if (materialEditTextUsername.validate()) {
                    if (name.equals(materialEditTextUsername.getEditValue())) {
                        XToastUtils.toast("请修改！");
                    } else {
                        user.put("username", materialEditTextUsername.getEditValue());
                        user.saveInBackground().subscribe(new Observer<AVObject>() {
                            @Override
                            public void onSubscribe(@NotNull Disposable d) {

                            }

                            @Override
                            public void onNext(@NotNull AVObject avObject) {
                                AVObject profileAVObject = (AVObject) user.get("Profile");
                                if (profileAVObject != null) {
                                    AVObject todo = AVObject.createWithoutData("Profile", profileAVObject.getObjectId());
                                    todo.put("username", materialEditTextUsername.getEditValue());
                                    todo.saveInBackground().subscribe();
                                    XToastUtils.success("修改成功");
                                    CallBack.getInstance().backToAccount();
                                    CallBack.getInstance().callMainRefreshHead();
                                    popToBack();
                                }else {
                                    XToastUtils.error("同步profile失败");
                                }
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
                }
            }
        });
        return titleBar;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_modify_name;
    }

    @Override
    protected void initViews() {
        name = user.getString("username");
        materialEditTextUsername.setText(user.get("username") == null ? ""
                : user.get("username").toString());
    }
}