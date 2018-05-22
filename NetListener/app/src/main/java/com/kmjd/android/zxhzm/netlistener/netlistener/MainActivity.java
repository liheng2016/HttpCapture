package com.kmjd.android.zxhzm.netlistener.netlistener;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.security.KeyChain;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.aliyun.sls.android.sdk.model.Log;
import com.aliyun.sls.android.sdk.model.LogGroup;
import com.kmjd.android.httpcapture.SDKManager;
import com.kmjd.android.httpcapture.bean.ALIYLogRequest;
import com.kmjd.android.httpcapture.bean.STSResult;
import com.kmjd.android.httpcapture.bean.onResultListener;
import com.kmjd.android.httpcapture.bean.onUploadLogListener;
import com.kmjd.android.httpcapture.manager.SDKManagerServer;
import com.kmjd.android.httpcapture.util.MyLogger;
import com.kmjd.android.zxhzm.netlistener.netlistener.utils.ProxyUtils;
import com.kmjd.android.zxhzm.netlistener.netlistener.utils.SPUtils;


import android.widget.Toast;


import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;

public class MainActivity extends AppCompatActivity {

    private WebView mWebView;
    private SwipeRefreshLayout refreshLayout;
    private Button btnUpload, btnGetToken;
    private SDKManager sdkManager;
    private boolean isGetToken = false;
    public String baseUserAgent = "Mozilla/5.0 (Linux; Android 5.0.2) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/37.0.0.0";
    private boolean isSetProxy = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        initView();
        MyLogger.zLog().d("日志测试");
        installCert();
        sdkManager = new SDKManagerServer();
        btnUpload = findViewById(R.id.btn_uploadlog);
        btnGetToken = findViewById(R.id.btn_getToken);
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isGetToken) {
                    uploadLog();
                } else {
                    Toast.makeText(MainActivity.this, "请先获取STS凭证！", Toast.LENGTH_LONG).show();
                }
            }
        });
        btnGetToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSTSToken();
            }
        });
    }

    /**
     * <p>initView</p>
     *
     * @Description 初始化界面
     */
    private void initView() {
        mWebView = findViewById(R.id.wv_content);

        WebSettings mSettings = mWebView.getSettings();
        mSettings.setJavaScriptEnabled(true);
        mSettings.setAllowFileAccess(true);
        //mSettings.setUserAgentString(baseUserAgent);
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setWebViewClient(new WebViewClient());
        //initProxyWebView();
        mWebView.loadUrl("http://119.81.201.156:6026");

        refreshLayout = findViewById(R.id.srl_content);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mWebView.reload();
                refreshLayout.setRefreshing(false);
            }
        });
    }

    /**
     * <p>getSTSToken</p>
     *
     * @Description 获取STS凭证信息
     */
    private void getSTSToken() {
        sdkManager.getSTSToken(new onResultListener() {
            @Override
            public void onSuccess(STSResult result) {
                isGetToken = true;
                SPUtils.putString(MainActivity.this, "AccessKeyId", result.getAccessKeyId());
                SPUtils.putString(MainActivity.this, "AccessKeySecret", result.getAccessKeySecret());
                SPUtils.putString(MainActivity.this, "SecurityToken", result.getSecurityToken());
            }

            @Override
            public void onFailed(STSResult result) {
                isGetToken = false;
            }
        });
    }

    /**
     * <p>uploadLog</p>
     *
     * @Description 上传日志数据
     */
    private void uploadLog() {
        ALIYLogRequest request = new ALIYLogRequest();
        request.setProject("kmjd-listener");
        request.setEndpoint("cn-qingdao.log.aliyuncs.com");
        request.setLogStore("test");
        String ak = SPUtils.getString(MainActivity.this, "AccessKeyId", "");
        String sk = SPUtils.getString(MainActivity.this, "AccessKeySecret", "");
        String token = SPUtils.getString(MainActivity.this, "SecurityToken", "");
        if (TextUtils.isEmpty(ak) || TextUtils.isEmpty(sk) || TextUtils.isEmpty(token)) {
            Toast.makeText(MainActivity.this, "获取STS凭证失败！", Toast.LENGTH_LONG).show();
            return;
        }
        request.setAccessKeyId(ak);
        request.setAccessKeySecret(sk);
        request.setSecurityToken(token);

        /* 创建logGroup */
        LogGroup logGroup = new LogGroup("sls test", "android");
        /* 存入一条log */
        Log log = new Log();
        log.PutContent("current time ", "" + System.currentTimeMillis() / 1000);
        log.PutContent("content", "this is a log from android");
        logGroup.PutLog(log);
        sdkManager.asyncUploadLog(request, logGroup, new onUploadLogListener() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(MainActivity.this, "上传数据成功！", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailed(String message) {
                Toast.makeText(MainActivity.this, "上传数据失败：" + message, Toast.LENGTH_LONG).show();
            }
        });
    }



    /**
     * <p>initProxyWebView</p>
     * @Description  初始化webview
     */
    private void initProxyWebView(){
        if(!isSetProxy){
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    if (ProxyUtils.setProxy(mWebView, "127.0.0.1", sdkManager.getProxyPort())) {
                        mWebView.loadUrl("http://119.81.201.156:6026");
                        isSetProxy = true;
                        android.util.Log.i("zyf", "onResume====设置代理成功");
                    } else {
                        Toast.makeText(mWebView.getContext(), "Set proxy fail!", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    /**
     * <p>installCert</p>
     *
     * @Description 安装证书
     */
    public void installCert() {
        final String CERTIFICATE_RESOURCE = Environment.getExternalStorageDirectory() + "/har/littleproxy-mitm.pem";
        Boolean isInstallCert = SPUtils.getBoolean(this, "isInstallNewCert", false);

        if (!isInstallCert) {
            Toast.makeText(this, "必须安装证书才可实现HTTPS抓包", Toast.LENGTH_LONG).show();
            try {
                byte[] keychainBytes;
                FileInputStream is = null;
                try {
                    is = new FileInputStream(CERTIFICATE_RESOURCE);
                    keychainBytes = new byte[is.available()];
                    is.read(keychainBytes);
                } finally {
                    IOUtils.closeQuietly(is);
                }

                Intent intent = KeyChain.createInstallIntent();
                intent.putExtra(KeyChain.EXTRA_CERTIFICATE, keychainBytes);
                intent.putExtra(KeyChain.EXTRA_NAME, "NetworkDiagnosis CA Certificate");
                startActivityForResult(intent, 3);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //WebSettings webSettings = mWebView.getSettings();
       // webSettings.setUserAgentString(baseUserAgent);
       // initProxyWebView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_log:
                startActivity(new Intent(MainActivity.this, PreviewActivity.class));
                break;
            case R.id.action_wifi:
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 3) {
            if (resultCode == Activity.RESULT_OK) {
                SPUtils.putBoolean(this, "isInstallNewCert", true);
                Toast.makeText(this, "安装成功", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "安装失败", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}
