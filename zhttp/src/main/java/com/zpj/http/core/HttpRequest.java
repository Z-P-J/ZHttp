package com.zpj.http.core;

import com.zpj.http.parser.html.nodes.Document;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;

import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;

public abstract class HttpRequest implements IHttp.Request {

    protected final HttpConfig config;

    HttpRequest(HttpConfig config) {
        this.config = config;
    }

    @Override
    public HttpConfig config() {
        return config;
    }

    @Override
    public IHttp.Response response() throws Exception {
        return config.httpFactory().createResponse(this);
    }

    @Override
    public final HttpObserver<IHttp.Response> execute() {
        return new HttpObserver<>(new ObservableOnSubscribe<IHttp.Response>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<IHttp.Response> emitter) throws Exception {
                emitter.onNext(syncExecute());
                emitter.onComplete();
            }
        });
    }

    @Override
    public final HttpObserver<String> toStr() {
        return new HttpObserver<>(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> emitter) throws Exception {
                emitter.onNext(syncToStr());
                emitter.onComplete();
            }
        });
    }

    @Override
    public final HttpObserver<Document> toHtml() {
        return new HttpObserver<>(new ObservableOnSubscribe<Document>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Document> emitter) throws Exception {
                emitter.onNext(syncToHtml());
                emitter.onComplete();
            }
        });
    }

    @Override
    public final HttpObserver<JSONObject> toJsonObject() {
        return new HttpObserver<>(new ObservableOnSubscribe<JSONObject>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<JSONObject> emitter) throws Exception {
                emitter.onNext(syncToJsonObject());
                emitter.onComplete();
            }
        });
    }

    @Override
    public final HttpObserver<JSONArray> toJsonArray() {
        return new HttpObserver<>(new ObservableOnSubscribe<JSONArray>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<JSONArray> emitter) throws Exception {
                emitter.onNext(syncToJsonArray());
                emitter.onComplete();
            }
        });
    }

    @Override
    public final HttpObserver<Document> toXml() {
        return new HttpObserver<>(new ObservableOnSubscribe<Document>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Document> emitter) throws Exception {
                emitter.onNext(syncToXml());
                emitter.onComplete();
            }
        });
    }

}
