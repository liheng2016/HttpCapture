package com.kmjd.android.zxhzm.netlistener.netlistener;

import android.content.Context;
import android.os.Environment;
import android.support.multidex.MultiDexApplication;

import com.kmjd.android.httpcapture.SDKManager;
import com.kmjd.android.httpcapture.manager.SDKManagerServer;

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

/**
 * Created by zym on 2017/3/15.
 */

public class MainApplication extends MultiDexApplication {
    public static Context applicationContext;
    public static BrowserMobProxy proxy;
    public static int proxyPort = 8888;
    public static Boolean isInitProxy = false;
    public SDKManager sdkManager;

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = getApplicationContext();
        //init();
        sdkManager = new SDKManagerServer();
        sdkManager.init();
        new Thread(new Runnable() {
            @Override
            public void run() {
                sdkManager.startProxy();
                isInitProxy = true;
            }
        }).start();
    }

    private void init() {
        try {
            FileUtils.forceMkdir(new File(Environment.getExternalStorageDirectory() + "/har"));
        } catch (IOException e) {
            // test.har文件不存在
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                startProxy();
            }
        }).start();
    }



    public void startProxy(){
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
        isInitProxy = true;
    }

}
