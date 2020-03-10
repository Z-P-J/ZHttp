package com.zpj.http.demo;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.zpj.http.ZHttp;
import com.zpj.http.core.Connection;
import com.zpj.http.core.HttpHeader;
import com.zpj.http.core.ObservableTask;
import com.zpj.http.core.IHttp;
import com.zpj.http.parser.html.nodes.Document;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Proxy;
import java.util.Map;

import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView contentText = findViewById(R.id.text_content);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
////                    long time3 = System.currentTimeMillis();
////                    Log.d(TAG, "time3=" + time3);
////                    OkHttpClient client = new OkHttpClient();
////                    Request request = new Request.Builder()
////                            .url("https://www.baidu.com/")
////                            .build();
////                    try (Response response = client.newCall(request).execute()) {
////                        final String content = response.body().string();
////                        long time4 = System.currentTimeMillis();
////                        Log.d(TAG, "time4=" + time4);
////                        Log.d(TAG, "delta2=" + (time4 - time3));
//////                        contentText.post(new Runnable() {
//////                            @Override
//////                            public void run() {
//////                                contentText.setText(content);
//////                            }
//////                        });
////                    }
//
//
//
//
//
////                    long time1 = System.currentTimeMillis();
////                    Log.d(TAG, "time1=" + time1);
////                    final String body = ZHttp.get("https://www.baidu.com/").toStr();
////                    long time2 = System.currentTimeMillis();
////                    Log.d(TAG, "time2=" + time2);
////                    Log.d(TAG, "delta1=" + (time2 - time1));
////                    contentText.post(new Runnable() {
////                        @Override
////                        public void run() {
////                            contentText.setText(body);
////                        }
////                    });
//
//                    // https://api.heweather.com/x3/weather
////                    "city", "beijing"
////                    "key", "d17ce22ec5404ed883e1cfcaca0ecaa7"
//                    final String b = ZHttp.get("https://api.heweather.com/x3/weather")
//                            .data("city", "beijing")
//                            .data("key", "d17ce22ec5404ed883e1cfcaca0ecaa7")
//                            .onRedirect(new IHttp.OnRedirectListener() {
//                                @Override
//                                public boolean onRedirect(String redirectUrl) {
//                                    Log.d("onRedirect", "redirectUrl=" + redirectUrl);
//                                    return true;
//                                }
//                            })
//                            .toStr();
//                    contentText.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            contentText.setText(b);
//                        }
//                    });
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();

        ZHttp.get("https://api.heweather.com/x3/weather")
                .data("city", "beijing")
                .data("key", "d17ce22ec5404ed883e1cfcaca0ecaa7")
                .onRedirect(new IHttp.OnRedirectListener() {
                    @Override
                    public boolean onRedirect(String redirectUrl) {
                        Log.d("onRedirect", "redirectUrl=" + redirectUrl);
                        return true;
                    }
                })
                .toStr()
                .onNext(new ObservableTask.OnNextListener<String, String>() { // 网络请求嵌套
                    @Override
                    public ObservableTask<String> onNext(String data) {
                        Log.d(TAG, "data=" + data);
                        return ZHttp.get("https://api.heweather.com/x3/weather")
                                .data("city", "beijing")
                                .data("key", "d17ce22ec5404ed883e1cfcaca0ecaa7")
                                .onRedirect(new IHttp.OnRedirectListener() {
                                    @Override
                                    public boolean onRedirect(String redirectUrl) {
                                        Log.d("onRedirect", "redirectUrl=" + redirectUrl);
                                        return true;
                                    }
                                })
                                .toStr();
                    }
                })
                .onSubscribe(new IHttp.OnSubscribeListener() {
                    @Override
                    public void onSubscribe(Disposable d) throws Exception {

                    }
                })
                .onSuccess(new IHttp.OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String data) {
                        contentText.setText(data);
                    }
                })
                .onError(new IHttp.OnErrorListener() {
                    @Override
                    public void onError(Throwable throwable) {
                        Toast.makeText(MainActivity.this, "出错了：" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .onComplete(new IHttp.OnCompleteListener() {
                    @Override
                    public void onComplete() throws Exception {

                    }
                })
                .subscribe();

    }
}
