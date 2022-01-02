package com.zpj.http.ssl;

import android.content.Context;
import android.text.TextUtils;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class HttpsTrustManager {

    private final TrustManager[] trustManagers = new TrustManager[] { TRUST_MANAGER };

    private final HostnameVerifier defaultHostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
    private final SSLSocketFactory defaultSSLSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();

    public static final HostnameVerifier HOSTNAME_VERIFIER = new HostnameVerifier() {

        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }

    };

    public static final TrustManager TRUST_MANAGER = new X509TrustManager() {

        private final X509Certificate[] ACCEPTED_ISSUERS = new X509Certificate[0];

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return ACCEPTED_ISSUERS;
        }
    };

    private static final class Holder {
        private static final HttpsTrustManager HTTPS_TRUST_MANAGER = new HttpsTrustManager();
    }

    private HttpsTrustManager() {

    }

    public static void install(boolean allowAllSSL) {
        Holder.HTTPS_TRUST_MANAGER.allowAllSSL(allowAllSSL);
    }

    private void allowAllSSL(boolean allow) {
        if (allow) {
            HttpsURLConnection.setDefaultHostnameVerifier(HOSTNAME_VERIFIER);

            SSLContext context = null;

            try {
                context = SSLContext.getInstance("TLS");
                context.init(null, trustManagers, new SecureRandom());
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                e.printStackTrace();
            }
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
        } else {
            HttpsURLConnection.setDefaultHostnameVerifier(defaultHostnameVerifier);
            HttpsURLConnection.setDefaultSSLSocketFactory(defaultSSLSocketFactory);
        }
    }

    /**
     * 服务器证书不是由 CA 签署的，而是自签署时，获取默认的SSL
     */
    public static SSLContext getDefaultSLLContext() {
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[] { TRUST_MANAGER }, new SecureRandom());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sslContext;
    }

    /**
     * 颁发服务器证书的 CA 未知
     *
     * @param caAlias CA证书别名
     * @param caPath 保存在assets目录下的CA证书完整路径
     */
    public static SSLContext getSSLContext(Context context, String caAlias, String caPath) {
        if (TextUtils.isEmpty(caAlias) || TextUtils.isEmpty(caPath)) {
            return null;
        }
        // Load CAs from an InputStream
        // (could be from a resource or ByteArrayInputStream or ...)
        CertificateFactory cf = null;
        try {
            cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = context.getAssets().open(caPath);
            Certificate ca;
            ca = cf.generateCertificate(caInput);
            System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());

            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry(caAlias, ca);

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);
            KeyManagerFactory kmf =
                    KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, null);

            // Create an SSLContext that uses our TrustManager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
            return sslContext;
        } catch (CertificateException | NoSuchAlgorithmException | IOException | KeyStoreException | KeyManagementException | UnrecoverableKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setCaCertificate(Context context, String caAlias, String caPath) {
        SSLContext sslContext = getSSLContext(context, caAlias, caPath);
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
    }

}
