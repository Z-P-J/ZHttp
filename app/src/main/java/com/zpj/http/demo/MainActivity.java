package com.zpj.http.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zpj.http.ZHttp;
import com.zpj.http.core.IHttp;
import com.zpj.http.engine.urlconnection.HttpUrlConnectionEngine;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ViewGroup view;

    private TextView contentText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.activity_main, null, false);
        setContentView(view);

        contentText = findViewById(R.id.text_content);
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

//        ZHttp.get("https://api.heweather.com/x3/weather")
//                .data("city", "beijing")
//                .data("key", "d17ce22ec5404ed883e1cfcaca0ecaa7")
//                .onRedirect(new IHttp.OnRedirectListener() {
//                    @Override
//                    public boolean onRedirect(int redirectCount, String redirectUrl) {
//                        Log.d("onRedirect", "redirectCount=" + redirectCount + " redirectUrl=" + redirectUrl);
//                        return true;
//                    }
//                })
//                .toJsonObject()
//                .bindToLife(this, Lifecycle.Event.ON_PAUSE)
//                .onSubscribe(new IHttp.OnSubscribeListener() {
//                    @Override
//                    public void onSubscribe(Disposable d) throws Exception {
//
//                    }
//                })
//                .onSuccess(new IHttp.OnSuccessListener<JSONObject>() {
//                    @Override
//                    public void onSuccess(JSONObject data) throws Exception {
//                        contentText.setText(data.toString());
//                    }
//                })
//                .onError(new IHttp.OnErrorListener() {
//                    @Override
//                    public void onError(Throwable throwable) {
//                        Toast.makeText(MainActivity.this, "出错了：" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .onComplete(new IHttp.OnCompleteListener() {
//                    @Override
//                    public void onComplete() throws Exception {
//                        Log.d(TAG, "onComplete");
//                    }
//                })
//                .subscribe();

//        ZHttp.get("https://book.douban.com/subject/30133440/")
//                .toHtml()
//                .bindToLife(this, Lifecycle.Event.ON_PAUSE)
//                .onSuccess(new IHttp.OnSuccessListener<Document>() {
//                    @Override
//                    public void onSuccess(Document data) throws Exception {
//                        contentText.setText(data.toString());
//                    }
//                })
//                .onError(new IHttp.OnErrorListener() {
//                    @Override
//                    public void onError(Throwable throwable) {
//                        Toast.makeText(MainActivity.this, "出错了：" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .subscribe();

//        ZHttp.config()
//                .baseUrl("http://tt.tljpxm.com")
//                .ignoreContentType(true)
//                .init();
//
//        ZHttp.get("/app/faxian.jsp?index=faxian")
//                .toHtml()
//                .bindToLife(this, Lifecycle.Event.ON_PAUSE)
//                .onSuccess(new IHttp.OnSuccessListener<Document>() {
//                    @Override
//                    public void onSuccess(Document data) throws Exception {
//                        contentText.setText(data.toString());
//                    }
//                })
//                .onError(new IHttp.OnErrorListener() {
//                    @Override
//                    public void onError(Throwable throwable) {
//                        Toast.makeText(MainActivity.this, "出错了：" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .subscribe();

        ZHttp.config()
                .httpFactory(new HttpUrlConnectionEngine())
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.182 Safari/537.36 Edg/88.0.705.81")
                .ignoreContentType(true)
                .init();
//
//        ZHttp.get("https://account.xiaomi.com/pass/serviceLogin?callback=https%3A%2F%2Fi.mi.com%2Fsts%3Fsign%3DmF32YtfY7XReThOa0pZzXhZXJ0U%253D%26followup%3Dhttps%253A%252F%252Fi.mi.com%252F%26sid%3Di.mi.com&sid=i.mi.com&_locale=zh_CN&_snsNone=true\n")
//                .toHtml()
//                .onSuccess(new IHttp.OnSuccessListener<Document>() {
//                    @Override
//                    public void onSuccess(Document doc) throws Exception {
////                        Element element = doc.selectFirst("a.login-btn-2MOTB");
//                        Log.d(TAG, "element=" + doc.toString());
////                        String loginUrl = element.attr("href");
////                        Log.d(TAG, "loginUrl=" + loginUrl);
//
//                    }
//                })
//                .subscribe();

        ZHttp.post("https://account.xiaomi.com/pass/serviceLoginAuth2")
                .data("needTheme", "false")
                .data("showActiveX", "false")
                .data("serviceParam", "{\"checkSafePhone\":false,\"checkSafeAddress\":false,\"lsrp_score\":0.0}")
                .data("callback", "https://i.mi.com/sts?sign=mF32YtfY7XReThOa0pZzXhZXJ0U%3D&followup=https%3A%2F%2Fi.mi.com%2F&sid=i.mi.com")
                .data("qs", "%3Fcallback%3Dhttps%253A%252F%252Fi.mi.com%252Fsts%253Fsign%253DmF32YtfY7XReThOa0pZzXhZXJ0U%25253D%2526followup%253Dhttps%25253A%25252F%25252Fi.mi.com%25252F%2526sid%253Di.mi.com%26sid%3Di.mi.com%26_locale%3Dzh_CN%26_snsNone%3Dtrue")
                .data("sid", "i.mi.com")
                .data("_sign", "9jXUgB/pG9gyrojgrYozJpnskkE=")
                .data("user", "15086601665")
                .data("cc", "+86")
                .data("hash", "3E3B13DB9E139005D57AF059BF9FAF8F")
                .data("_json", "true")
                .enqueue(new IHttp.Callback() {
                    @Override
                    public void onFailure(IHttp.Connection conn, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(IHttp.Connection conn, IHttp.Response response) throws IOException {
                        Log.d("MainActivity", "cookies=" + response.cookieStr());
                        Log.d("MainActivity", "body=" + response.bodyString());
//                        ZHttp.get("https://i.mi.com/sts?sign=mF32YtfY7XReThOa0pZzXhZXJ0U%3D&followup=https%3A%2F%2Fi.mi.com%2F&sid=i.mi.com")
//                                .cookie(res.cookieStr())
//                                .toStr()
//                                .onSuccess(new IHttp.OnSuccessListener<String>() {
//                                    @Override
//                                    public void onSuccess(String body) throws Exception {
//                                        Log.d("MainActivity", "https://i.mi.com/#/ body=" + body);
//                                    }
//                                })
//                                .onError(new IHttp.OnErrorListener() {
//                                    @Override
//                                    public void onError(Throwable throwable) {
//                                        throwable.printStackTrace();
//                                    }
//                                })
//                                .subscribe();
                    }
                });

//        ZHttp.get("https://api.heweather.com/x3/weather")
//                .data("city", "beijing")
//                .data("key", "d17ce22ec5404ed883e1cfcaca0ecaa7")
//                .onRedirect(new IHttp.OnRedirectListener() {
//                    @Override
//                    public boolean onRedirect(int redirectCount, String redirectUrl) {
//                        Log.d("onRedirect", "redirectCount=" + redirectCount + " redirectUrl=" + redirectUrl);
//                        return true;
//                    }
//                })
//                .toStr()
//                .onNext(new HttpObserver.OnNextListener<String, String>() { // 网络请求嵌套
//                    @Override
//                    public HttpObserver<String> onNext(String data) {
//                        Log.d(TAG, "data=" + data);
//                        try {
//                            Thread.sleep(10000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                        return ZHttp.get("https://api.heweather.com/x3/weather")
//                                .data("city", "beijing")
//                                .data("key", "d17ce22ec5404ed883e1cfcaca0ecaa7")
//                                .toStr();
//                    }
//                })
//                .bindToLife(this, Lifecycle.Event.ON_PAUSE)
//                .onSubscribe(new IHttp.OnSubscribeListener() {
//                    @Override
//                    public void onSubscribe(Disposable d) throws Exception {
//
//                    }
//                })
//                .onSuccess(new IHttp.OnSuccessListener<String>() {
//                    @Override
//                    public void onSuccess(String data) {
//                        contentText.setText(data);
//                    }
//                })
//                .onError(new IHttp.OnErrorListener() {
//                    @Override
//                    public void onError(Throwable throwable) {
//                        Toast.makeText(MainActivity.this, "出错了：" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .onComplete(new IHttp.OnCompleteListener() {
//                    @Override
//                    public void onComplete() throws Exception {
//                        Log.d(TAG, "onComplete");
//                    }
//                })
//                .subscribe();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
