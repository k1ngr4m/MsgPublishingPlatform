package com.xuexiang.mapandmsg.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * @ClassName: Note
 * @Description: 封装notation
 * @Author jap191
 * @Date 2021/5/22
 * @Version 1.0
 */
public class Note {
    /**
     * Notification 通知类
     */
    public static class GT_Notification {

        private static int NOTIFYID = 0x1997; //通知id
        private static String CHANEL_ID = "mapandmsg.push";
        private static String CHANEL_DESCRIPTION = "GT 描述";
        private static String CHANEL_NAME = "GT_Android复习";

        /**
         * 设置 通知类的 code
         *
         * @param NotifyId
         * @return
         */
        public GT_Notification setNotifyId(int NotifyId) {
            NOTIFYID = NotifyId;
            return this;
        }

        /**
         * 设置 通知类的 id
         *
         * @param ChanelId
         * @return
         */
        public GT_Notification setChanelId(String ChanelId) {
            CHANEL_ID = ChanelId;
            return this;
        }

        /**
         * 设置 通知
         *
         * @param ChanelDescription
         * @return
         */
        public GT_Notification setChanelDescription(String ChanelDescription) {
            CHANEL_DESCRIPTION = ChanelDescription;
            return this;
        }

        /**
         * 设置通知 名字
         *
         * @param ChanelName
         * @return
         */
        public GT_Notification setChanelName(String ChanelName) {
            CHANEL_NAME = ChanelName;
            return this;
        }

        private Context context;

        /**
         * 实例化 通知类
         *
         * @param context
         */
        public GT_Notification(Context context) {
            this.context = context;
        }

        /**
         * 初始化 通知类
         *
         * @param icon       图标
         * @param title      标题
         * @param text       内容
         * @param time       时间
         * @param voiceTF    是否设置声音振动
         * @param autoCancel 设置通知打开后自动消失
         * @param cla        设置跳转的 页面
         * @return 返回 通知类
         */
        public NotificationManagerCompat sendingNotice(int icon, String title, String text, int time, boolean voiceTF, boolean autoCancel, Class<?> cla) {

            /**
             * 由于 Notification.Builder 仅支持 Android 4.1及之后的版本，为了解决兼容性问题， Notification.Builder 仅支持 API 26 与 26 之前的版本
             * Google 在 Android Support v4 中加入了 NotificationCompat.Builder 类
             */
            String channelId = createNotificationChannel(context.getApplicationContext());//创建Notification Channel
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId);//创建Notification并与Channel关联

            builder.setSmallIcon(icon);//设置通知图标
            builder.setAutoCancel(autoCancel);//设置通知打开后自动消失
            builder.setContentTitle(title);//设置标题
            builder.setContentText(text);//设置内容
            if (time == 0) {
                builder.setWhen(System.currentTimeMillis());//设置系统当前时间为发送时间
            } else {
                builder.setWhen(time);//设置用户设置的发送时间
            }
            if (voiceTF) {
                builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);//设置默认的声音与默认的振动
            }

            if (cla != null) {    //如果 cla 不为空就设置跳转的页面
                Intent intent = new Intent(context, cla);
                PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                builder.setContentIntent(pi);//设置通知栏 点击跳转
            }

            //发布通知
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context.getApplicationContext());
            notificationManagerCompat.notify(NOTIFYID, builder.build());
            return notificationManagerCompat;
        }

        /**
         * 创建通知
         *
         * @param context
         * @return
         */
        private String createNotificationChannel(Context context) {
            // O (API 26)及以上版本的通知需要NotificationChannels。
            if (context != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                // 初始化NotificationChannel。
                NotificationChannel notificationChannel = new NotificationChannel(CHANEL_ID, CHANEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.setDescription(CHANEL_DESCRIPTION);

                // 向系统添加 NotificationChannel。试图创建现有通知
                // 通道的初始值不执行任何操作，因此可以安全地执行
                // 启动顺序
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                assert notificationManager != null;
                notificationManager.createNotificationChannel(notificationChannel);

                return CHANEL_ID;
            } else {
                return null; // 为pre-O(26)设备返回 null
            }
        }

    }

}
