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

package com.xuexiang.mapandmsg.leancloud;

import android.os.Parcelable;

import cn.leancloud.AVObject;
import cn.leancloud.AVParcelableObject;
import cn.leancloud.annotation.AVClassName;

/**
 * @ClassName: PraiseList
 * @Description: 点赞表
 * @Author
 * @Date 2021/5/23
 * @Version 1.0
 */
@AVClassName("PraiseList")
public class PraiseList extends AVObject {
    public static final Parcelable.Creator CREATOR = AVParcelableObject.AVObjectCreator.instance;
    public static final String USERNAME = "username";
    public static final String NEWSINFO = "NewsInfo";
    public PraiseList() {
    }
    public String getUsername() {
        return getString(USERNAME);
    }
    public void setUsername(String username) {
        put(USERNAME, username);
    }
    public AVObject getNewsInfo() {
        return getAVObject(NEWSINFO);
    }
    public void setNewsInfo(AVObject newsInfo) {
        put(NEWSINFO, newsInfo);
    }
}
