package com.zpj.http.core;

import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class HttpObservable<T> {

    private final io.reactivex.Observable<T> observable;

    private IHttp.OnSubscribeListener onSubscribeListener;
    private IHttp.OnSuccessListener<T> onSuccessListener;
    private IHttp.OnErrorListener onErrorListener;
    private IHttp.OnCompleteListener onCompleteListener;

    HttpObservable(io.reactivex.Observable<T> observable) {
        this.observable = observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public HttpObservable<T> subscribeOn(Scheduler scheduler) {
        observable.subscribeOn(scheduler);
        return this;
    }

    public HttpObservable<T> observeOn(Scheduler scheduler) {
        observable.observeOn(scheduler);
        return this;
    }

    public HttpObservable<T> onSubscribe(IHttp.OnSubscribeListener listener) {
        this.onSubscribeListener = listener;
        return this;
    }

    public final HttpObservable<T> onError(IHttp.OnErrorListener listener) {
        this.onErrorListener = listener;
        return this;
    }

    public final HttpObservable<T> onSuccess(IHttp.OnSuccessListener<T> listener) {
        this.onSuccessListener = listener;
        return this;
    }

    public HttpObservable<T> onComplete(IHttp.OnCompleteListener listener) {
        this.onCompleteListener = listener;
        return this;
    }

    public HttpObservable<T> subscribe() {
        observable.subscribe(new Observer<T>() {
            @Override
            public void onSubscribe(Disposable d) {
                if (onSubscribeListener != null) {
                    onSubscribeListener.onSubscribe(d);
                }
            }

            @Override
            public void onNext(T data) {
                if (onSuccessListener != null) {
                    onSuccessListener.onSuccess(data);
                }
            }

            @Override
            public void onError(Throwable e) {
                if (onErrorListener != null) {
                    onErrorListener.onError(e);
                }
            }

            @Override
            public void onComplete() {
                if (onCompleteListener != null) {
                    onCompleteListener.onComplete();
                }
            }
        });
        return this;
    }

}
