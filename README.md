# ZHttp
 Android网络请求框架（完善中），基于Jsoup开发，支持Jsoup所有功能，支持Html和XML解析，支持get、post、put、head、delete等网络请求，支持RxJava，支持链接重定向监听，上传文件等功能

## How to use?

    1. 添加依赖
    dependencies {
        implementation 'com.zpj.http:ZHttp:1.0.1'

        // 如果需要使用RxJava，请添加以下依赖
        implementation 'io.reactivex.rxjava2:rxjava:2.2.17'
        implementation 'io.reactivex.rxjava2:rxandroid:2.1.1'
    }

    2. 简单使用
    new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // IHttp.Response对象
                        IHttp.Response res = ZHttp.get("Your url")
                                .data("city", "chongqing")
                                .data("key", "123456")
                                .onRedirect(new IHttp.OnRedirectListener() {
                                    @Override
                                    public boolean onRedirect(String redirectUrl) {
                                        Log.d("onRedirect", "redirectUrl=" + redirectUrl);
                                        return true;
                                    }
                                })
                                .cookie("...")
                                .userAgent("...")
                                .referer("..")
                                .ignoreHttpErrors(true)
                                .ignoreContentType(true)
                                .timeout(10000)
                                .proxy(Proxy.NO_PROXY)
                                .validateTLSCertificates(true)
                                .syncExecute();
                        String setCookie = res.header(HttpHeader.SET_COOKIE);
                        Map<String, String> cookies =  res.cookies();
                        int statusCode = res.statusCode();
                        String statusMsg = res.statusMessage();
                        String content = res.body();


                        String str = ZHttp.get("Your url")
                                .syncToStr();
                        Log.d("str", "str=" + str);

                        Document doc = ZHttp.post("Your url")
                                .syncToHtml();
                        doc.select("selector").text();

                        Document xml = ZHttp.post("Your url")
                                .syncToXml();
                        doc.select("selector").text();

                        JSONObject jsonObject = ZHttp.connect("Your url")
                                .method(Connection.Method.GET)
                                .syncToJsonObject();

                        JSONArray jsonArray = ZHttp.get("Your url")
                                .syncToJsonArray();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();

    3. 配合RxJava使用，直接使用，不用自己创建线程
    ZHttp.get("https://api.heweather.com/x3/weather")
                    .data("city", "beijing")
                    .data("key", "d17ce22ec5404ed883e1cfcaca0ecaa7")
                    .onRedirect(new IHttp.OnRedirectListener() {
                        @Override
                        public boolean onRedirect(int redirectCount, String redirectUrl) {
                            Log.d("onRedirect", "redirectUrl=" + redirectUrl);
                            return true;
                        }
                    })
                    .toStr()
                    .onNext(new HttpObserver.OnNextListener<String, String>() { // 网络请求嵌套
                        @Override
                        public HttpObserver<String> onNext(String data) {
                            Log.d(TAG, "data=" + data);
                            return ZHttp.get("https://api.heweather.com/x3/weather")
                                    .data("city", "beijing")
                                    .data("key", "d17ce22ec5404ed883e1cfcaca0ecaa7")
                                    .onRedirect(new IHttp.OnRedirectListener() {
                                        @Override
                                        public boolean onRedirect(int redirectCount, String redirectUrl) {
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

## TODO
    1. 支持OKHTTP3
    2. 结合GSON
    3. 支持通过注解来转换内容为Java Object