/*
 * Copyright (C) 2021 xuexiangjys(xuexiangjys@163.com)
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

package com.xuexiang.mapandmsg.utils.sdkinit;

import android.app.Application;

import cn.leancloud.AVLogger;
import cn.leancloud.AVOSCloud;
import cn.leancloud.im.AVIMOptions;
import cn.leancloud.push.PushService;

;

/**
 * @ClassName: LeanCloudInit
 * @Description: leanCloud基础库初始化
 * @Author
 * @Date 2021/5/19
 * @Version 1.0
 */
public final class LeanCloudInit {


//    private static String serverURL = "https://8bewmwgs.lc-cn-n1-shared.com";
//    private static String appId = "8BeWmWGsDQXc1oLgwFgbqWcn-gzGzoHsz";
//    private static String appKey = "vCOw2kV0IMS0OLRAKWecYUy1";
    private static String serverURL = "https://mbiwxkga.lc-cn-n1-shared.com";
    private static String appId = "mbiWXKga8dWD4RxRBFWlr7pl-gzGzoHsz";
    private static String appKey = "yxiJsnKnrad9peMzISVipZP5";

    //17858621764
    //333781

    private LeanCloudInit() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * 初始化基础库SDK
     */
    public static void init(Application application) {
        // 在 AVOSCloud.initialize() 之前调用
        initDebug(application);
        // AVOSCloud.initialize()
        initKey(application);
        // 在 AVOSCloud#initialize 之后调用，禁止自动发送推送服务的 login 请求。
        initShutPush(application);

    }

    /**
     * 对于只使用 LeanCloud 即时通讯服务，而不使用推送服务的应用来说，在初始化的时候设置 AVIMOptions#disableAutoLogin4Push 选项，可以加快即时通讯用户登录的过程。设置方法如下
     * @param application
     */
    private static void initShutPush(Application application) {
        // 在 AVOSCloud#initialize 之后调用，禁止自动发送推送服务的 login 请求。
        AVIMOptions.getGlobalOptions().setDisableAutoLogin4Push(true);
    }

    /**
     *Android 平台初始化key
     * @param application
     */
    private static void initKey(Application application) {
        // 提供 this、App ID、App Key、Server Host 作为参数
        // 注意这里千万不要调用 cn.leancloud.core.AVOSCloud 的 initialize 方法，否则会出现 NetworkOnMainThread 等错误。
        AVOSCloud.initialize(application, appId, appKey,serverURL);
        PushService.setDefaultChannelId(application, "1");
    }

    /**
     * 在应用开发阶段，你可以选择开启 SDK 的调试日志（debug log）来方便追踪问题。调试日志开启后，SDK 会把网络请求、错误消息等信息输出到 IDE 的日志窗口，或是浏览器 Console 或是 LeanCloud 控制台的云引擎日志中。
     * @param application
     */
    private static void initDebug(Application application){
        // 在 AVOSCloud.initialize() 之前调用
        AVOSCloud.setLogLevel(AVLogger.Level.DEBUG);
    }
}
