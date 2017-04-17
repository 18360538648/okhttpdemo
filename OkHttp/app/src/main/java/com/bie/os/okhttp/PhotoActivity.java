package com.bie.os.okhttp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bie.os.okhttp.listener.help.ProgressHelper;
import com.bie.os.okhttp.listener.impl.UIProgressListener;
import com.bie.os.okhttp.photopicker.PhotoPicker;
import com.bie.os.okhttp.photopicker.PhotoPreview;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Luosiwei on 2017/3/17.
 */
public class PhotoActivity extends Activity {
    private Button btnPickPhoteWithCamera;
    private PhotoAdapter photoAdapter;
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    private ArrayList<String> selectedPhotos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        photoAdapter = new PhotoAdapter(this, selectedPhotos);

        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(4, OrientationHelper.VERTICAL));
        recyclerView.setAdapter(photoAdapter);
        btnPickPhoteWithCamera = (Button) findViewById(R.id.btn_pick_phote);
        btnPickPhoteWithCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhotoPicker.builder()
                        .setPhotoCount(100) // 设置每次最后选择图片张数
                        .setGridColumnCount(3)
                        .setPreviewEnabled(true)
                        .start(PhotoActivity.this);
            }
        });

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (photoAdapter.getItemViewType(position) == PhotoAdapter.TYPE_ADD) {
                    PhotoPicker.builder()
                            .setPhotoCount(PhotoAdapter.MAX)
                            .setShowCamera(true)
                            .setPreviewEnabled(false)
                            .setSelected(selectedPhotos)
                            .start(PhotoActivity.this);
                } else {
                    PhotoPreview.builder()
                            .setPhotos(selectedPhotos)
                            .setCurrentItem(position)
                            .start(PhotoActivity.this);
                }
            }
        }));
    }

    /**
     * 展示选择的图片
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK &&
                (requestCode == PhotoPicker.REQUEST_CODE || requestCode == PhotoPreview.REQUEST_CODE)) {

            List<String> photos = null;
            if (data != null) {
                photos = data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
            }
            selectedPhotos.clear();

            if (photos != null) {
                Log.i("lsw", "photos:" + photos);
                String path = photos.get(0);
//                byte[] bytes=getByte(path);
                postAsynFile(path);
                selectedPhotos.addAll(photos);
            }
            photoAdapter.notifyDataSetChanged();
        }
    }

    private void postAsynFile(String path) {
        File file=new File(path);
        RequestBody fileBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("data", "small.jpg", fileBody)
                .addFormDataPart("type",""+1)
                .build();
        OkHttpClient mOkHttpClient = new OkHttpClient.Builder().writeTimeout(60, TimeUnit.SECONDS).connectTimeout(60, TimeUnit.SECONDS).build();
        //这个是ui线程回调，可直接操作UI
        final UIProgressListener uiProgressRequestListener = new UIProgressListener() {
            @Override
            public void onUIProgress(long bytesWrite, long contentLength, boolean done) {
                int process = (int) ((100 * bytesWrite) / contentLength);
                Log.i("lsw", "进度:" + process);

//                ProgressDialogUtil.progressDialog.setProgress(process);
            }

            @Override
            public void onUIStart(long bytesWrite, long contentLength, boolean done) {
                super.onUIStart(bytesWrite, contentLength, done);
                Log.i("lsw", "开始监听");
            }

            @Override
            public void onUIFinish(long bytesWrite, long contentLength, boolean done) {
                super.onUIFinish(bytesWrite, contentLength, done);
                Log.i("lsw", "结束监听");
            }
        };
        Request request = new Request.Builder()
                .url("http://192.168.10.181/v1/files")
                .post(ProgressHelper.addProgressRequestListener(requestBody, uiProgressRequestListener))
                .build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i("lsw","code:"+response.code());
                Log.i("lsw","response:"+response.body().string());
                String downurl=null;
//                try {
//                    JSONObject jsonObject=new JSONObject(response.body().string());
//                    downurl=jsonObject.getString("url");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
                Looper.prepare();
                Toast.makeText(getApplicationContext(),"上传图片成功，下载地址:"+downurl,Toast.LENGTH_LONG).show();
            }
        });
    }

    public byte[] getByte(String fileName) {
        byte[] bytes = new byte[0];
        if (fileName != null && !fileName.isEmpty()) {
            File file = new File(fileName);
            long fileLength = file.length();
            bytes = new byte[(int) fileLength];
            try {
                BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
                // 将数据读取出来保存在byte数组中
                bufferedInputStream.read(bytes);
                bufferedInputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return bytes;
    }
}
