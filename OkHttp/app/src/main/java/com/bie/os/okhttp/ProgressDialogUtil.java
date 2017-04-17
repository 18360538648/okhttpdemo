package com.bie.os.okhttp;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by Luosiwei on 2017/3/17.
 */
public class ProgressDialogUtil {
    public static ProgressDialog progressDialog;

    public static void show(Context context, String msg) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(msg);
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(100);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }
}
