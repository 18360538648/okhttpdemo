package com.bie.os.okhttp.listener;

/**
 * Created by os on 2017/1/19.
 */
public interface DownFileListener {
    public void onSuccess(String result);

    public void onFail(String error);

}
