package com.kmjd.android.httpcapture;

import android.support.annotation.Nullable;

import com.aliyun.sls.android.sdk.model.LogGroup;
import com.kmjd.android.httpcapture.bean.ALIYLogRequest;
import com.kmjd.android.httpcapture.bean.STSResult;
import com.kmjd.android.httpcapture.bean.onResultListener;
import com.kmjd.android.httpcapture.bean.onUploadLogListener;

import net.lightbody.bmp.BrowserMobProxy;

public interface SDKManager {

    /**
     * <p>init</p>
     * @Description 初始化环境
     */
    void init();

    /**
     * <p>startProxy</p>
     * @Description  启动代理
     */
    BrowserMobProxy startProxy();

    /**
     * <p>getProxyPort</p>
     * @return  代理端口
     */
    int getProxyPort();

    /**
     * <p>getProxyInstance</p>
     * @return  获取BrowserMobProxy实例
     */
    BrowserMobProxy getProxyInstance();

    /**
     * <p>getSTSToken</p>
     * @return  返回STS凭证信息
     */
    void getSTSToken(onResultListener listener);

    /**
     * <p>asyncUploadLog</p>
     * @param logGroup
     * @Description 异步上传日志
     */
    void asyncUploadLog(ALIYLogRequest request, LogGroup logGroup, onUploadLogListener listener);
}
