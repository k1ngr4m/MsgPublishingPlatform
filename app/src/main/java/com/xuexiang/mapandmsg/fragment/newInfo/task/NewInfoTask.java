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

package com.xuexiang.mapandmsg.fragment.newInfo.task;

import com.xuexiang.mapandmsg.adapter.entity.NewInfo;

/**
 * @ClassName: NewsReleaseTask
 * @Description: 发布消息
 * @Author
 * @Date 2021/5/15
 * @Version 1.0
 */
public class NewInfoTask {
    private static NewInfoTask newInfoTask;
    private NewInfo newInfo;
    public void setNewInfo(NewInfo newInfo) {
        this.newInfo = newInfo;
    }

    private OnDataListener onDataListener;
    private OnDataShowMapListener onDataShowMapListener;
    public void setOnDataListener(OnDataListener onDataListener) {
        this.onDataListener = onDataListener;
    }
    public void setOnDataShowMapListener(OnDataShowMapListener onDataShowMapListener) {
        this.onDataShowMapListener = onDataShowMapListener;
    }

    /**
     * GetData : NewsRelease -> Map
     */
    public interface OnDataListener {
        /**
         * 返回数据回调
         * @param objectId
         */
        void onDataGet(String objectId);

        /**
         * 删除NewInfo回调
         * @param makerId
         */
        void onDeleteNewInfo(String makerId);
    }
    public static NewInfoTask getInstance() {
        if (newInfoTask == null) {
            newInfoTask = new NewInfoTask();
        }
        return newInfoTask;
    }

    /**
     * GetData: ShowNews <-> Map
     */
    public interface OnDataShowMapListener{
        /**
         * 从map发送数据到show
         * @param newInfo
         */
        public void onDataMapToShow(NewInfo newInfo);
    }

    private NewInfoTask() {
    }

    /**
     * 消息发布
     */
    public void NewsRelease(String objectId){
        onDataListener.onDataGet(objectId);
    }
    public void DeleteNewInfo(String makerId){
        onDataListener.onDeleteNewInfo(makerId);
    }
    public void NewInfoSendToShow(){
        onDataShowMapListener.onDataMapToShow(newInfo);
    }

    /**
     * 销毁定位资源
     */
    public void onDestroy() {
        newInfoTask = null;
    }

}
