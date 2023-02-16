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

package com.xuexiang.mapandmsg.fragment.Thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName: ThreadPool
 * @Description: 从线程池得到线程
 * @Author
 * @Date 2021/5/21
 * @Version 1.0
 */
public class ThreadPool {
    private static ExecutorService pool;

    public static ExecutorService getThreadPool(){
        if(pool == null){
            //maximumPoolSize设置为2 ，拒绝策略为AbortPolic策略，直接抛出异常
            pool = new ThreadPoolExecutor(1, 2, 1000,
                    TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>(),
                    Executors.defaultThreadFactory(),new ThreadPoolExecutor.AbortPolicy());
        }
       return pool;
    }


}



