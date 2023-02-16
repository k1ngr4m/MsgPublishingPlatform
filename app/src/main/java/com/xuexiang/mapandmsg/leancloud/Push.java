

package com.xuexiang.mapandmsg.leancloud;

import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.xuexiang.mapandmsg.utils.XToastUtils;

import org.jetbrains.annotations.NotNull;

import cn.leancloud.AVPush;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * @ClassName: Push
 * @Description: 推送消息
 * @Author
 * @Date 2021/5/22
 * @Version 1.0
 */
public class Push {
    public static void pushToPublic(String message){
        AVPush push = new AVPush();

//        AVQuery<AVInstallation> query = AVInstallation.getQuery();
//        query.whereEqualTo("installationId", AVInstallation.getCurrentInstallation()
//                .getInstallationId());
//        push.setQuery(query);
        push.setChannel("public");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("action", "com.pushdemo.action");
        jsonObject.put("alert", "推送消息在这儿22222");


        push.setData(jsonObject);
        push.setPushToAndroid(true);
        push.sendInBackground().subscribe(new Observer<cn.leancloud.json.JSONObject>() {
            @Override
            public void onSubscribe(@NotNull Disposable d) {

            }

            @Override
            public void onNext(@NotNull cn.leancloud.json.JSONObject jsonObject) {
                XToastUtils.success("Send successfully.", Toast.LENGTH_SHORT);
            }

            @Override
            public void onError(@NotNull Throwable e) {
                XToastUtils.error("Send fails with :" + e.getMessage(), Toast.LENGTH_LONG);
            }

            @Override
            public void onComplete() {

            }
        });
    }
    public static void pushAll(String message){
        AVPush push = new AVPush();
        push.setChannel("public");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("action", "com.pushdemo.action");
        jsonObject.put("alert", message);
        push.setData(jsonObject);
        push.setPushToAndroid(true);
        push.sendInBackground().subscribe(new Observer<cn.leancloud.json.JSONObject>() {
            @Override
            public void onSubscribe(@NotNull Disposable d) {

            }

            @Override
            public void onNext(@NotNull cn.leancloud.json.JSONObject jsonObject) {
                XToastUtils.success("推送成功！" + jsonObject.toString());
                log("推送成功！");
            }

            @Override
            public void onError(@NotNull Throwable e) {
                XToastUtils.error("推送失败！" + e.getMessage());
                log("推送失败！" + e.getMessage());
            }

            @Override
            public void onComplete() {

            }
        });
    }
    static void log(String s){
        Log.e("Debug(Push)",s);
    }
}
