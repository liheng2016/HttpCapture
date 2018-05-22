package com.kmjd.android.httpcapture.bean;

public interface onUploadLogListener {

    void onSuccess(String message);

    void onFailed(String message);
}
