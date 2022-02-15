package com.xxnn.mchooktool;

import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import de.robv.android.xposed.XposedBridge;
import okhttp3.*;

import java.io.FileWriter;
import java.io.IOException;

/**
 * @author weiguan
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onclick(View view) {
        EditText addressEditText = findViewById(R.id.address);
        String address = addressEditText.getText().toString();
        String url = String.format(address + "/send?seq=%s&command=%s&uin=%s", "123", "test", "321");
        Toast.makeText(this, url, Toast.LENGTH_SHORT).show();
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
                    FileWriter fileWriter = new FileWriter("address.txt", false);
                    fileWriter.write(url);
                    fileWriter.close();
                    toast("连接/保存成功!");
                } catch (Exception e) {
                    toast("连接成功,保存失败!");
                }
            }
        });
    }

    public void toast(String text) {
        Looper.prepare();
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        Looper.loop();
    }
}