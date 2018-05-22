package com.kmjd.android.zxhzm.netlistener.netlistener;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmjd.android.httpcapture.SDKManager;
import com.kmjd.android.httpcapture.manager.SDKManagerServer;

import net.lightbody.bmp.core.har.HarCookie;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarLog;
import net.lightbody.bmp.core.har.HarNameValuePair;
import net.lightbody.bmp.core.har.HarPostDataParam;
import net.lightbody.bmp.core.har.HarRequest;
import net.lightbody.bmp.core.har.HarResponse;

import java.io.IOException;

public class HarDetailActivity extends AppCompatActivity {

    private int pos;
    private SDKManager sdkManager;
    private LinearLayout mLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_har_detail);

        initToolbar();
        mLayout = findViewById(R.id.ll_detailLayout);
        pos = getIntent().getIntExtra("pos", -1);
        sdkManager = new SDKManagerServer();
        initHarLog(pos);
    }

    /**
     * <p>initToolbar</p>
     * @Description 初始化Toolbar
     */
    private void initToolbar(){
        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("数据详情");
        //关键下面两句话，设置了回退按钮，及点击事件的效果
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * <p>initHarLog</p>
     * @param pos
     * @Description 初始化log日志
     */
    public void initHarLog(int pos) {
        HarLog harLog = sdkManager.getProxyInstance().getHar().getLog();
        HarEntry harEntry = harLog.getEntries().get(pos);

        HarRequest harRequest = harEntry.getRequest();
        HarResponse harResponse = harEntry.getResponse();

        addItem("Overview");
        addItem("URL", harRequest.getUrl());

        addItem("Method", harRequest.getMethod());
        addItem("Code", harResponse.getStatus() + "");
        addItem("TotalTime", harEntry.getTime() + "ms");
        addItem("Size", harResponse.getBodySize() + "Bytes");

        if (harRequest.getQueryString().size() > 0) {
            addItem("Request Query");
            for (HarNameValuePair pair : harRequest.getQueryString()) {
                addItem(pair.getName(), pair.getDecodeValue());
            }
        }

        addItem("Request Header");
        for (HarNameValuePair pair : harRequest.getHeaders()) {
            // 不显示cookie
            if (!pair.getName().equals("Cookie")) {
                addItem(pair.getName(), pair.getDecodeValue());
            }
        }

        if (harRequest.getCookies().size() > 0) {
            addItem("Request Cookies");
            for (HarCookie cookie : harRequest.getCookies()) {
                addItem(cookie.getName(), cookie.getDecodeValue());
            }
        }

        if (harRequest.getPostData() != null) {
            if(harRequest.getPostData().getText()!= null
                    && harRequest.getPostData().getText().length()>0) {
                addItem("Request Content");
                addItem("PostData", harRequest.getPostData().getText());
            }

            if(harRequest.getPostData().getParams()!= null
                    &&  harRequest.getPostData().getParams().size()>0){
                addItem("Request PostData");

                for (HarPostDataParam pair : harRequest.getPostData().getParams()) {
                    addItem(pair.getName(), pair.getValue());
                }
            }
        }

        addItem("Response Header");
        for (HarNameValuePair pair : harResponse.getHeaders()) {
            if (!pair.getName().equals("Cookie")) {
                addItem(pair.getName(), pair.getDecodeValue());
            }
        }

        if (harResponse.getCookies().size() > 0) {
            addItem("Response Cookies");
            for (HarCookie cookie : harResponse.getCookies()) {
                addItem(cookie.getName(), cookie.getDecodeValue());
            }
        }

        if ((harResponse.getRedirectURL() != null && harResponse.getRedirectURL().length() > 0) ||
                (harResponse.getContent().getText() != null && harResponse.getContent().getText().length() > 0)) {
            addItem("Response Content");
        }
        if (harResponse.getRedirectURL() != null && harResponse.getRedirectURL().length() > 0) {
            addItem("RedirectURL", harResponse.getRedirectURL());
        }
        if (harResponse.getContent().getText() != null && harResponse.getContent().getText().length() > 0) {
            addContentItem("Content", harResponse.getContent().getText(), pos);
        }
    }

    /**
     * <p>addContentItem</p>
     * @param title
     * @param value
     * @param pos
     * @Description  添加响应内容一栏
     */
    public void addContentItem(String title, final String value, final int pos) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_detail, null);

        TextView textView = view.findViewById(R.id.tv_title);
        textView.setText(title);

        TextView valueTextView = view.findViewById(R.id.tv_value);
        if (TextUtils.isEmpty(value)) {
            valueTextView.setText("");
        } else {
            valueTextView.setText(value.substring(0, value.length() > 50 ? 50 : value.length()));
        }

        if (title.equals("Content")) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (value != null && value.length() > 10) {
                       showDialog(value);
                    }
                }
            });
        }
        mLayout.addView(view);
    }

    /**
     * <p>jsonFormatter</p>
     * @param uglyJSONString
     * @return  格式化json字符串
     * @throws Exception
     */
    public String jsonFormatter(String uglyJSONString) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Object obj = mapper.readValue(uglyJSONString, Object.class);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (IOException e) {
            e.printStackTrace();
            return  null;
        }
    }

    /**
     * <p>showDialog</p>
     * @param value
     * @Description 弹出提示框
     */
    private void showDialog(String value){
        View textEntryView = LayoutInflater.from(HarDetailActivity.this).inflate(R.layout.alert_textview, null);
        TextView edtInput =  textEntryView.findViewById(R.id.tv_content);
        edtInput.setText(value);

        AlertDialog.Builder builder = new AlertDialog.Builder(HarDetailActivity.this);
        builder.setCancelable(true);
        builder.setView(textEntryView);
        builder.setPositiveButton("确认", null);
        builder.show();
    }

    /**
     * <p>addItem</p>
     * @param title
     * @param value
     * @Description  添加普通信息显示一栏
     */
    public void addItem(String title, final String value) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_detail, null);

        TextView textView =  view.findViewById(R.id.tv_title);
        textView.setText(title);

        TextView valueTextView =  view.findViewById(R.id.tv_value);
        if (TextUtils.isEmpty(value)) {
            valueTextView.setText("");
        } else {
            valueTextView.setText(value.substring(0, value.length() > 50 ? 50 : value.length()));
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (value != null && value.length() > 10) {
                    showDialog(value);
                }
            }
        });

        mLayout.addView(view);
    }

    /**
     * <p>addItem</p>
     * @param cateName
     * @Description  添加类型一栏
     */
    public void addItem(String cateName) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_cate, null);
        TextView textView =  view.findViewById(R.id.tv_catetitle);
        textView.setText(cateName);
        mLayout.addView(view);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
