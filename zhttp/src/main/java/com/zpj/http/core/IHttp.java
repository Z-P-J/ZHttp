package com.zpj.http.core;

public interface IHttp {

    interface OnRedirectListener {
        boolean onRedirect(String redirectUrl);
    }

    interface OnErrorListener {
        void onError();
    }

    interface OnSuccessListener {
        void onSuccess();
    }

}
