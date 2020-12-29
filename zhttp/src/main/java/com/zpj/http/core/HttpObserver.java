package com.zpj.http.core;

import android.app.Activity;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.view.View;

import com.zpj.rxlife.LifecycleTransformer;
import com.zpj.rxlife.RxLife;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class HttpObserver<T> {

    private final Observable<T> observable;
    private Disposable disposable;

    private Scheduler subscribeScheduler;
    private Scheduler observeScheduler;
    private final List<LifecycleTransformer<T>> composerList;

    private IHttp.OnSubscribeListener onSubscribeListener;
    private IHttp.OnSuccessListener<T> onSuccessListener;
    private IHttp.OnErrorListener onErrorListener;
    private IHttp.OnCompleteListener onCompleteListener;

    public interface OnFlatMapListener<T, R> {
        void onNext(T data, ObservableEmitter<R> emitter) throws Exception;
    }

    public interface OnNextListener<T, R> {
        HttpObserver<R> onNext(T data) throws Exception;
    }

    public HttpObserver(final ObservableOnSubscribe<T> observableOnSubscribe) {
        this(Observable.create(observableOnSubscribe));
    }

    public HttpObserver(Observable<T> observable) {
        this.observable = observable;
        this.composerList = new ArrayList<>();
    }

    public HttpObserver<T> subscribeOn(Scheduler scheduler) {
        this.subscribeScheduler = scheduler;
        return this;
    }

    public HttpObserver<T> observeOn(Scheduler scheduler) {
        this.observeScheduler = scheduler;
        return this;
    }

    public <R> HttpObserver<R> compose(ObservableTransformer<? super T, ? extends R> composer) {
        return new HttpObserver<R>(this.observable.compose(composer))
                .subscribeOn(subscribeScheduler)
                .observeOn(observeScheduler);
    }

    public HttpObserver<T> bindTag(Object tag) {
        return bindTag(tag, true);
    }

    public HttpObserver<T> bindTag(Object tag, boolean disposeBefore) {
        this.composerList.add(RxLife.<T>bindTag(tag, disposeBefore));
        return this;
    }

    public HttpObserver<T> bindToLife(LifecycleOwner lifecycleOwner) {
        return bindToLife(lifecycleOwner, Lifecycle.Event.ON_DESTROY);
    }

    public HttpObserver<T> bindToLife(LifecycleOwner lifecycleOwner, Lifecycle.Event event) {
        this.composerList.add(RxLife.<T>bindLifeOwner(lifecycleOwner, event));
        return this;
    }

    public HttpObserver<T> bindView(View view) {
        this.composerList.add(RxLife.<T>bindView(view));
        return this;
    }

    public HttpObserver<T> bindActivity(Activity activity) {
        this.composerList.add(RxLife.<T>bindActivity(activity));
        return this;
    }


    public HttpObserver<T> onSubscribe(IHttp.OnSubscribeListener listener) {
        this.onSubscribeListener = listener;
        return this;
    }

    public final HttpObserver<T> onError(IHttp.OnErrorListener listener) {
        this.onErrorListener = listener;
        return this;
    }

    public final HttpObserver<T> onSuccess(IHttp.OnSuccessListener<T> listener) {
        this.onSuccessListener = listener;
        return this;
    }

    public HttpObserver<T> onComplete(IHttp.OnCompleteListener listener) {
        this.onCompleteListener = listener;
        return this;
    }

    public final <R> HttpObserver<R> onNext(final OnNextListener<T, R> listener) {
//        initScheduler();
        Observable<R> o = observable
                .flatMap(new Function<T, ObservableSource<R>>() {
                    @Override
                    public ObservableSource<R> apply(@NonNull final T t) throws Exception {
                        if (listener != null) {
                            HttpObserver<R> httpObservable = listener.onNext(t);
                            if (httpObservable != null) {
                                return httpObservable.observable;
                            }
                        }
                        return Observable.empty();
                    }
                });
        return wrapObservable(new HttpObserver<>(o));
    }

    public final <R> HttpObserver<R> flatMap(final OnFlatMapListener<T, R> listener) {
//        initScheduler();
        Observable<R> o = observable
                .flatMap(new Function<T, ObservableSource<R>>() {
                    @Override
                    public ObservableSource<R> apply(@NonNull final T t) throws Exception {
                        return Observable.create(new ObservableOnSubscribe<R>() {
                            @Override
                            public void subscribe(@NonNull ObservableEmitter<R> emitter) throws Exception {
                                if (listener != null) {
                                    listener.onNext(t, emitter);
                                }
                                emitter.onComplete();
                            }
                        }).subscribeOn(subscribeScheduler).observeOn(observeScheduler);
                    }
                });
        return wrapObservable(new HttpObserver<>(o));
    }

    public Disposable subscribeWithDisposable() {
        if (disposable != null) {
            cancel();
//            return disposable;
        }
        disposable = getObservable()
                .subscribe(new Consumer<T>() {
                    @Override
                    public void accept(T t) throws Exception {
                        if (onSuccessListener != null) {
                            onSuccessListener.onSuccess(t);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                        if (onErrorListener != null) {
                            onErrorListener.onError(throwable);
                        }
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {
                        if (onCompleteListener != null) {
                            onCompleteListener.onComplete();
                        }
                    }
                }, new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        if (onSubscribeListener != null) {
                            onSubscribeListener.onSubscribe(disposable);
                        }
                    }
                });
        return disposable;
    }

    public void subscribe() {
        getObservable()
                .subscribe(new Observer<T>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        if (onSubscribeListener != null) {
                            try {
                                onSubscribeListener.onSubscribe(d);
                            } catch (Exception e) {
                                onError(e);
                            }
                        }
                    }

                    @Override
                    public void onNext(@NonNull T data) {
                        if (onSuccessListener != null) {
                            try {
                                onSuccessListener.onSuccess(data);
                            } catch (Exception e) {
                                onError(e);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        if (onErrorListener != null) {
                            onErrorListener.onError(e);
                        }
                    }

                    @Override
                    public void onComplete() {
                        if (onCompleteListener != null) {
                            try {
                                onCompleteListener.onComplete();
                            } catch (Exception e) {
                                onError(e);
                            }
                        }
                    }
                });
    }

    private void initScheduler() {
        if (subscribeScheduler == null) {
            subscribeScheduler = Schedulers.io();
        }
        if (observeScheduler == null) {
            observeScheduler = AndroidSchedulers.mainThread();
        }
    }

    private Observable<T> getObservable() {
        initScheduler();
        Observable<T> o = observable;
        for (LifecycleTransformer<T> composer : composerList) {
            o = o.compose(composer);
        }
        return o.subscribeOn(subscribeScheduler)
                .observeOn(observeScheduler);
    }

    private <R> HttpObserver<R> wrapObservable(HttpObserver<R> observer) {
        return observer.subscribeOn(subscribeScheduler)
                .observeOn(observeScheduler)
                .onSubscribe(onSubscribeListener)
                .onError(onErrorListener)
//                .onSuccess(onSuccessListener)
                .onComplete(onCompleteListener);
    }

    public void cancel() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        disposable = null;
    }

}
