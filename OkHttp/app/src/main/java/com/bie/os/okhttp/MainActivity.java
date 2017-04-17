package com.bie.os.okhttp;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.bie.os.okhttp.listener.help.ProgressHelper;
import com.bie.os.okhttp.listener.impl.UIProgressListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends Activity {
    private TextView tv;
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    //上传文件类型
    public static final MediaType MEDIA_TYPE_MARKDOWN
            = MediaType.parse("text/x-markdown; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getByOKHttp();
//        postByOKHttp();

    }
    // Get请求

    public void getByOKHttp() {
        // 创建okhttpclient对象
        OkHttpClient mOkHttpClient = new OkHttpClient();
        // 创建Request对象
        final Request request = new Request.Builder().url("http://192.168.10.181/v1/lcomments?user_id=123").addHeader("Content-Type", "application/json").build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String html = response.body().string();
                Log.i("lsw", "code" + response.code());
                Log.i("lsw", html);
            }
        });

    }

    public void postByOKHttp() {
        // 创建okhttpclient对象
        OkHttpClient mOkHttpClient = new OkHttpClient();
        RequestBody body = RequestBody.create(JSON, "json字符串");
        Request request = new Request.Builder().url("请求地址").post(body).addHeader("Content-Type", "application/json;charset=utf-8").build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String str = response.body().string();
                Log.i("lsw", str);
            }
        });
    }

    private void postAsynFile() {
        OkHttpClient mOkHttpClient = new OkHttpClient();
        File file = new File("/sdcard/wangshu.txt");
        Request request = new Request.Builder()
                .url("https://api.github.com/markdown/raw")
                .post(RequestBody.create(MEDIA_TYPE_MARKDOWN, file))
                .build();

        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i("wangshu", response.body().string());
            }
        });
    }

    private void downAsynFile() {
        OkHttpClient mOkHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url("下载地址").build();
        // 下载监听(对应的有上传监听addProgressRequestListener在PhoteActivity)
        ProgressHelper.addProgressResponseListener(mOkHttpClient, new UIProgressListener() {
            @Override
            public void onUIProgress(long currentBytes, long contentLength, boolean done) {
                int process = (int) ((100 * currentBytes) / contentLength);
                Log.i("lsw", "进度:" + process);
            }

            @Override
            public void onUIStart(long currentBytes, long contentLength, boolean done) {
                super.onUIStart(currentBytes, contentLength, done);
                Log.i("lsw", "开始监听");
            }

            @Override
            public void onUIFinish(long currentBytes, long contentLength, boolean done) {
                super.onUIFinish(currentBytes, contentLength, done);
                Log.i("lsw", "结束监听");
            }
        }).newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) {
                InputStream inputStream = response.body().byteStream();
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(new File("/sdcard/wangshu.jpg"));
                    byte[] buffer = new byte[2048];
                    int len = 0;
                    while ((len = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, len);
                    }
                    fileOutputStream.flush();
                } catch (IOException e) {
                    Log.i("wangshu", "IOException");
                    e.printStackTrace();
                }

                Log.d("wangshu", "文件下载成功");
            }
        });
    }
    //设置超时和缓存
//    File sdcache = getExternalCacheDir();
//    int cacheSize = 10 * 1024 * 1024;
//    OkHttpClient.Builder builder = new OkHttpClient.Builder()
//            .connectTimeout(15, TimeUnit.SECONDS)
//            .writeTimeout(20, TimeUnit.SECONDS)
//            .readTimeout(20, TimeUnit.SECONDS)
//            .cache(new Cache(sdcache.getAbsoluteFile(), cacheSize));
//    OkHttpClient mOkHttpClient=builder.build();
}
