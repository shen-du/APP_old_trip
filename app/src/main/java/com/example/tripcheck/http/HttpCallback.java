package com.example.tripcheck.http;

/**
 * 网络请求回调
 *
 * @author bohan.chen
 */
public interface HttpCallback<T> {

    /**
     * 请求成功
     */
    void onSuccess(T data);

    /**
     * 失败回调
     */
    void onError(int code, String errMsg);

    /**
     * 完成回调
     */
    void onFinish();

}
