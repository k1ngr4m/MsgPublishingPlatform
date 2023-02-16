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

package com.xuexiang.mapandmsg.fragment.newInfo;

import com.luck.picture.lib.entity.LocalMedia;
import com.xuexiang.mapandmsg.adapter.entity.NewInfo;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import cn.leancloud.AVFile;
import cn.leancloud.AVObject;
import cn.leancloud.AVQuery;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * @ClassName: NewsInfoHelper
 * @Description: 生成消息类
 * @Author
 * @Date 2021/5/15
 * @Version 1.0
 */
public class NewsInfoHelper {
    private static ArrayList<NewInfo> newInfoArrayList = new ArrayList<>();

    public static ArrayList<NewInfo> getNewInfoArrayList() {
        AVQuery<AVObject> query = new AVQuery<>("NewsInfo");
        query.findInBackground().subscribe(new Observer<List<AVObject>>() {
            @Override
            public void onSubscribe(@NotNull Disposable d) {

            }

            @Override
            public void onNext(@NotNull List<AVObject> avObjects) {
                for(AVObject newInfo: avObjects){
                    String title = (String) newInfo.get("title");
                    String date = (String) newInfo.get("date");
                    String address = (String) newInfo.get("address");
                    String phone = (String) newInfo.get("phone");
                    String contacts = (String) newInfo.get("contacts");
                    String summery = (String) newInfo.get("summery");
                    NewInfo newInfo1 = new NewInfo();
                    newInfo1.setTitle(title);
                    newInfo1.setTime(date);
                    newInfo1.setAddress(address);
                    newInfo1.setPhone(phone);
                    newInfo1.setContacts(contacts);
                    newInfo1.setSummary(summery);
                    int imageNum = 3;
                    ArrayList<LocalMedia> list = new ArrayList<>(imageNum);
                    for(int i = 0 ; i < imageNum ; i++){
                        // 图片
                        AVFile file = newInfo.getAVFile("image" + i);
                        if(file != null) {
                            LocalMedia localMedia = new LocalMedia(file.getUrl(),0,0,null);
                            list.add(localMedia);
                        }
                    }
                    newInfo1.setLocalMediaArrayList(list);

                    newInfoArrayList.add(newInfo1);
                }

            }

            @Override
            public void onError(@NotNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
        return newInfoArrayList;
    }

    /**
     * 将新的NewInfo存入List
     * @param newInfo
     */
    public static void addNewInfoInList(NewInfo newInfo){
        newInfoArrayList.add(newInfo);
    }

}
