package com.gym.easyokhttp.httpconnection.service;

/**
 * @funtion 响应抽象类MultiThreadResponse
 * @author lemon Guo
 */

public abstract class MultiThreadResponse<T> {

    public abstract void success(MultiThreadRequest request, T data);

    public abstract void fail(int errorCode, String errorMsg);
}
