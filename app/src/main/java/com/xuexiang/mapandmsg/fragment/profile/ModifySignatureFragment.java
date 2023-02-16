
package com.xuexiang.mapandmsg.fragment.profile;

import android.view.View;

import com.xuexiang.mapandmsg.R;
import com.xuexiang.mapandmsg.core.BaseFragment;
import com.xuexiang.mapandmsg.fragment.CallBack;
import com.xuexiang.mapandmsg.utils.XToastUtils;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xui.widget.actionbar.TitleBar;
import com.xuexiang.xui.widget.edittext.MultiLineEditText;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import cn.leancloud.AVObject;
import cn.leancloud.AVUser;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;


/**
 * 修改个性签名
 *
 * @author 夕子
 */
@Page(name = "个性签名")
public class ModifySignatureFragment extends BaseFragment {

    @BindView(R.id.modify_signature_met)
    MultiLineEditText multiLineEditTextSignature;
    AVUser user = AVUser.getCurrentUser();
    String signature = "";

    @Override
    protected TitleBar initTitle() {
        TitleBar titleBar = super.initTitle();
        titleBar.addAction(new TitleBar.TextAction("完成") {
            @Override
            public void performAction(View view) {
                if (multiLineEditTextSignature.getContentText().equals(signature)) {
                    XToastUtils.toast("请修改！");
                } else {
                    user.put("signature", multiLineEditTextSignature.getContentText());
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
            }
        });
        return titleBar;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_modify_signature;
    }

    @Override
    protected void initViews() {
        signature = user.getString("signature");
        multiLineEditTextSignature.setContentText(user.get("signature") == null ? ""
                : user.get("signature").toString());
    }
}