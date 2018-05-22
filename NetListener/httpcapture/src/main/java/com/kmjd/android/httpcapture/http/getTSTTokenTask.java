package com.kmjd.android.httpcapture.http;

import android.os.AsyncTask;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.kmjd.android.httpcapture.bean.STSResult;
import com.kmjd.android.httpcapture.bean.onResultListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class getTSTTokenTask extends AsyncTask<Void, Void, STSResult> {

    private onResultListener mListener;

    public getTSTTokenTask(onResultListener listener) {
        mListener = listener;
    }

    @Override
    protected STSResult doInBackground(Void... voids) {

        BufferedReader br = null;
        STSResult result = new STSResult();
        try {
            URL url = new URL("http://192.168.2.245:8080/NetlistenerWeb/getTokenServer?ak=LTAISZOX5P2vuDh1&sk=MOZMBTwJNhRFWxQeEg0eVSb5tRcs5G");
            HttpURLConnection httpconn = (HttpURLConnection) url.openConnection();
            httpconn.setRequestProperty("Charset", "utf-8");
            httpconn.setRequestMethod("GET");
            httpconn.setConnectTimeout(5000);
            httpconn.connect();
            int stat = httpconn.getResponseCode();
            String msg = "";
            if (stat == 200) {
                br = new BufferedReader(new InputStreamReader(httpconn.getInputStream()));
                msg = br.readLine();
                result = JSON.parseObject(msg, new TypeReference<STSResult>() {});
            } else {
                result.setStatusCode(stat);
                msg = "请求失败";
                result.setErrorMessage(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setStatusCode(-1);
            result.setErrorMessage(e.getMessage());
        }  finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(STSResult stsResult) {
        super.onPostExecute(stsResult);
        if (stsResult.getStatusCode() == 200) {
            mListener.onSuccess(stsResult);
        } else {
            mListener.onFailed(stsResult);
        }
    }
}
