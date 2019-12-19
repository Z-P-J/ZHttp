package com.gym.easyokhttp.httpconnection.service;

import com.gym.easyokhttp.httpconnection.http.HttpRequest;
import com.gym.easyokhttp.httpconnection.http.HttpResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @funtion 业务层多线程分发处理：用于处理下载任务
 * @author lemon Guo
 */

public class HttpRunnable implements Runnable {

    private HttpRequest mHttpRequest;

    private MultiThreadRequest mRequest;

    private WorkStation mWorkStation;

    public HttpRunnable(HttpRequest httpRequest, MultiThreadRequest request, WorkStation workStation) {
        this.mHttpRequest = httpRequest;
        this.mRequest = request;
        this.mWorkStation = workStation;

    }

    @Override
    public void run() {
        try {
            mHttpRequest.getBody().write(mRequest.getData());
            HttpResponse response = mHttpRequest.execute();
            String contentType = response.getHeaders().getContentType();
            mRequest.setContentType(contentType);
            if (response.getStatus().isSuccess()) {
                if (mRequest.getResponse() != null) {
                    mRequest.getResponse().success(mRequest, new String(getData(response)));
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mWorkStation.finish(mRequest);
        }


    }

    public byte[] getData(HttpResponse response) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream((int) response.getContentLength());
        int len;
        byte[] data = new byte[512];
        try {
            while ((len = response.getBody().read(data)) != -1) {
                outputStream.write(data, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream.toByteArray();
    }
}
