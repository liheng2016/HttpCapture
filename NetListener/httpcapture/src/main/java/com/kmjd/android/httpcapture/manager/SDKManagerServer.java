package com.kmjd.android.httpcapture.manager;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.aliyun.sls.android.sdk.ClientConfiguration;
import com.aliyun.sls.android.sdk.LOGClient;
import com.aliyun.sls.android.sdk.LogException;
import com.aliyun.sls.android.sdk.SLSLog;
import com.aliyun.sls.android.sdk.core.auth.CredentialProvider;
import com.aliyun.sls.android.sdk.core.auth.FederationToken;
import com.aliyun.sls.android.sdk.core.auth.PlainTextAKSKCredentialProvider;
import com.aliyun.sls.android.sdk.core.auth.StsTokenCredentialProvider;
import com.aliyun.sls.android.sdk.core.callback.CompletedCallback;
import com.aliyun.sls.android.sdk.model.Log;
import com.aliyun.sls.android.sdk.model.LogGroup;
import com.aliyun.sls.android.sdk.request.PostLogRequest;
import com.aliyun.sls.android.sdk.result.PostLogResult;
import com.aliyun.sls.android.sdk.utils.IPService;
import com.kmjd.android.httpcapture.SDKManager;
import com.kmjd.android.httpcapture.bean.ALIYLogRequest;
import com.kmjd.android.httpcapture.bean.STSResult;
import com.kmjd.android.httpcapture.bean.onResultListener;
import com.kmjd.android.httpcapture.bean.onUploadLogListener;
import com.kmjd.android.httpcapture.http.getTSTTokenTask;

import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.proxy.CaptureType;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class SDKManagerServer implements SDKManager {

    public final static int HANDLER_MESSAGE_UPLOAD_FAILED = 00011;
    public final static int HANDLER_MESSAGE_UPLOAD_SUCCESS = 00012;
    public static BrowserMobProxy proxy;
    private static onUploadLogListener mLogListener;
    private static int proxyPort = 8888;

    @Override
    public void init() {
        try {
            FileUtils.forceMkdir(new File(Environment.getExternalStorageDirectory() + "/har"));
        } catch (IOException e) {
            // test.har文件不存在
        }
    }

    @Override
    public BrowserMobProxy startProxy() {
        if (proxy == null) {
            synchronized (this) {
                if (proxy == null) {
                    try {
                        proxy = new BrowserMobProxyServer();
                        proxy.setTrustAllServers(true);
                        proxy.start(proxyPort);
                    } catch (Exception e) {
                        // 防止8888已被占用
                        Random rand = new Random();
                        proxyPort = rand.nextInt(1000) + 8000;
                        proxy = new BrowserMobProxyServer();
                        proxy.setTrustAllServers(true);
                        proxy.start(proxyPort);
                    }
                    proxy.enableHarCaptureTypes(
                            CaptureType.REQUEST_HEADERS, CaptureType.REQUEST_COOKIES, CaptureType.REQUEST_CONTENT,
                            CaptureType.RESPONSE_HEADERS, CaptureType.REQUEST_COOKIES, CaptureType.RESPONSE_CONTENT);

                    String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
                            .format(new Date(System.currentTimeMillis()));
                    proxy.newHar(time);
                }
            }
        }
        return proxy;
    }

    @Override
    public int getProxyPort() {
        return proxyPort;
    }

    @Override
    public BrowserMobProxy getProxyInstance() {
        return  proxy;
    }

    @Override
    public void getSTSToken(onResultListener listener) {
        new getTSTTokenTask(listener).execute();;
    }

    /**
     * 填入必要的参数
     */
//    public String endpoint = "cn-qingdao.log.aliyuncs.com";
//    public String accesskeyID = "******";
//    public String accessKeySecret = "******";
//    public String project = "kmjd-listener";
//    public String logStore = "test";

    private Handler handler = new Handler() {
        // 处理子线程给我们发送的消息。
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case HANDLER_MESSAGE_UPLOAD_FAILED:
                    //logText.setText((String) msg.obj);
                    android.util.Log.i("zyf", "failed");
                    mLogListener.onFailed((String) msg.obj);
                    return;
                case HANDLER_MESSAGE_UPLOAD_SUCCESS:
                    //Toast.makeText(MainActivity.this, "upload success", Toast.LENGTH_SHORT).show();
                    android.util.Log.i("zyf", "success");
                    mLogListener.onSuccess("success");
                    return;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void asyncUploadLog(ALIYLogRequest data, LogGroup logGroup, onUploadLogListener listener) {
        //        主账户使用方式
        String AK = "LTAIVTV6lbGr0dWS";
        String SK = "1ZVXCywnS6SMU7USRmZ18Ep1FvkolB";
      /* PlainTextAKSKCredentialProvider credentialProvider =
                new PlainTextAKSKCredentialProvider(AK,SK);*/
        //        STS使用方式
//        String STS_AK = "STS.NKNxhFj1HfjdLbcYG9YfQPCbB";
//        String STS_SK = "EgEheqVEDbkPGs3y7hQV3GwhPem8Ln2hufKDLd8d2MMp";
//        String STS_TOKEN = "CAISkgJ1q6Ft5B2yfSjIr4j7M9Lyh+5p0aiPTkTSvUdsVel9v4bJoDz2IHBLfXBoA+kWv/kznGhS5vYYlqB6T55OSAmcNZIoSjLHE9zlMeT7oMWQweEuif/MQBqfaXPS2MvVfJ+/Lrf0ceusbFbpjzJ6xaCAGxypQ12iN+/36+JjadtFZg6jcR5BC+xKIRFvjMgVLgGSV5CXPwXtn3DbAWdxpwN4khkf06mkxdCG4Res6z+fw+QO9YD2LcrmPYs+frUHCorvgLUsK/aZi3cLt0gQpJ0b1vIUpW312fiGGERU7hm8NO7Zz8ZiNgcRZNJhS/Ed9aCmzK0p57CLz96nm0xXXuFJTzzUTZi83dHJCGc3oneg2ASRGoABN4QptqEOwgsrzl/371RZakmEOQBydalnk7ZC9oy/AX5+OBVc+VMEVVnPKqr0tdE62QFsSfWMuWnDIhfP+naxvHqnsaLXmqGLYFGw3WMH6epWPL9nJwXwlJZ7lCkq/4fYhmYP1hvOdDGcsaqvp7p0GHSi/PfjvsgTqhIH2MzbXKk=";
        mLogListener = listener;
        String endpoint = data.getEndpoint();
        String project = data.getProject();
        String logStore = data.getLogStore();
        String STS_AK = data.getAccessKeyId();
        String STS_SK = data.getAccessKeySecret();
        String STS_TOKEN = data.getSecurityToken();
        if(TextUtils.isEmpty(endpoint)||TextUtils.isEmpty(project)||TextUtils.isEmpty(logStore)||TextUtils.isEmpty(STS_AK)||TextUtils.isEmpty(STS_SK)||TextUtils.isEmpty(STS_TOKEN)){
            mLogListener.onFailed("输入参数不完全");
            return;
        }

        StsTokenCredentialProvider credentialProvider = new StsTokenCredentialProvider(STS_AK, STS_SK, STS_TOKEN);

        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
        SLSLog.enableLog(); // log打印在控制台
        LOGClient logClient = new LOGClient(endpoint, credentialProvider, conf);

        try {
            PostLogRequest request = new PostLogRequest(project, logStore, logGroup);
            logClient.asyncPostLog(request, new CompletedCallback<PostLogRequest, PostLogResult>() {
                @Override
                public void onSuccess(PostLogRequest request, PostLogResult result) {
                    Message message = Message.obtain(handler);
                    message.what = HANDLER_MESSAGE_UPLOAD_SUCCESS;
                    message.sendToTarget();
                }

                @Override
                public void onFailure(PostLogRequest request, LogException exception) {
                    Message message = Message.obtain(handler);
                    message.what = HANDLER_MESSAGE_UPLOAD_FAILED;
                    message.obj = exception.getMessage();
                    message.sendToTarget();
                }
            });
        } catch (LogException e) {
            e.printStackTrace();
        }
    }
}
