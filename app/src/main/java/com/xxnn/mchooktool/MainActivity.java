package com.xxnn.mchooktool;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import okhttp3.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author weiguan
 */
public class MainActivity extends AppCompatActivity {
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"
    };
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onclick(View view) throws IOException {
        verifyStoragePermissions(MainActivity.this);
        EditText addressEditText = findViewById(R.id.address);
        String address = addressEditText.getText().toString();
        String url = String.format(address + "/send?seq=%s&command=%s&uin=%s", "123", "test", "321");
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody body = RequestBody.create(new byte[0]);
        final Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                toast("连接失败!" + e.getMessage());
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                try {
                    @SuppressLint("SdCardPath") String path = "/data/data/com.xxnn.mchooktool/";
                    makeFilePath(path, "address.txt");
                    FileWriter fileWriter = new FileWriter(path + "address.txt", false);
                    fileWriter.write(url);
                    fileWriter.close();
                    toast("连接/保存成功!");
                } catch (Exception e) {
                    toast("连接成功,保存失败!" + e.getMessage());
                }
            }
        });
    }

    public static void verifyStoragePermissions(Activity activity) {
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void toast(String text) {
        Looper.prepare();
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        Looper.loop();
    }

    public static File makeFilePath(String filePath, String fileName) throws IOException {
        File file = null;
        makeRootDirectory(filePath);
        file = new File(filePath + fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }

    // 生成文件夹
    public static void makeRootDirectory(String filePath) {
        File file = null;
        file = new File(filePath);
        if (!file.exists()) {
            file.mkdir();
        }
    }
}