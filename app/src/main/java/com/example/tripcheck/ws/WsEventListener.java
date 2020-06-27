package com.example.tripcheck.ws;

import com.example.tripcheck.bean.MessageBean;

/**
 * webSocket事件回调监听
 *
 * @author bohan.chen
 */
public interface WsEventListener {

    /**
     * 收到消息
     * @param data
     */
    void onRecMessage(MessageBean data);

    /**
     * 连接成功
     */
    void onConnect();

    /**
     * 设备断开
     */
    void onDisconnect();

    /**
     * 失败回调
     */
    void onFailed(String errMsg);

}
