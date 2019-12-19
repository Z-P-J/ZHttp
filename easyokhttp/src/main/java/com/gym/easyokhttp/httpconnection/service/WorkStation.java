package com.gym.easyokhttp.httpconnection.service;

import com.gym.easyokhttp.httpconnection.HttpRequestProvider;
import com.gym.easyokhttp.httpconnection.http.HttpRequest;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @funtion 业务层多线程分发处理：用于处理多线程的控制和队列的管理 MultiThreadRequest
 * @author lemon Guo
 */

public class WorkStation {

    private static final int MAX_REQUEST_SIZE = 60;

    private static final ThreadPoolExecutor sThreadPool = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new ThreadFactory() {

        private AtomicInteger index = new AtomicInteger();

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("http thread name is " + index.getAndIncrement());
            return thread;
        }
    });


    private Deque<MultiThreadRequest> mRunning = new ArrayDeque<>();

    private Deque<MultiThreadRequest> mCache = new ArrayDeque<>();

    private HttpRequestProvider mRequestProvider;

    public WorkStation() {
        mRequestProvider = new HttpRequestProvider();
    }

    public void add(MultiThreadRequest request) {

        if (mRunning.size() > MAX_REQUEST_SIZE) {
            mCache.add(request);
        } else {
            doHttpRequest(request);
        }
    }


    public void doHttpRequest(MultiThreadRequest request) {
        HttpRequest httpRequest = null;
        try {
            httpRequest = mRequestProvider.getHttpRequest(URI.create(request.getUrl()), request.getMethod());
        } catch (IOException e) {
            e.printStackTrace();
        }
        sThreadPool.execute(new HttpRunnable(httpRequest, request, this));
    }


    public void finish(MultiThreadRequest request) {
        mRunning.remove(request);
        if (mRunning.size() > MAX_REQUEST_SIZE) {
            return;
        }

        if (mCache.size() == 0) {
            return;
        }

        Iterator<MultiThreadRequest> iterator = mCache.iterator();

        while (iterator.hasNext()) {
            MultiThreadRequest next = iterator.next();
            mRunning.add(next);
            iterator.remove();
            doHttpRequest(next);
        }
    }

}
