package com.xuexiang.mapandmsg.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.xuexiang.mapandmsg.R;
import com.xuexiang.mapandmsg.activity.MainActivity;
import com.xuexiang.mapandmsg.notification.Note;
import com.xuexiang.mapandmsg.utils.XToastUtils;

import org.json.JSONObject;

import cn.leancloud.AVOSCloud;

/**
 * @author 夕子
 */
public class CustomReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        XToastUtils.success("收到推送");
        Log.e("duebug(Broadcast)","收到推送");
        try {
            if ("com.pushdemo.action".equals(intent.getAction())) {
                JSONObject json = new JSONObject(intent.getExtras().getString("com.avos.avoscloud.Data"));
                final String message = json.getString("alert");

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
                                message, 0, true, true, MainActivity.class);


            }
        } catch (Exception ignored) {

        }
    }
}
