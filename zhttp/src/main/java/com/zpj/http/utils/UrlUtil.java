package com.zpj.http.utils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class UrlUtil {

    /**
     * Encodes the input URL into a safe ASCII URL string
     * @param url unescaped URL
     * @return escaped URL
     */
    public static String encodeUrl(String url) {
        try {
            URL u = new URL(url);
            return encodeUrl(u).toExternalForm();
        } catch (Exception e) {
            return url;
        }
    }

    public static URL encodeUrl(URL u) {
        try {
            //  odd way to encode urls, but it works!
            String urlS = u.toExternalForm(); // URL external form may have spaces which is illegal in new URL() (odd asymmetry)
            urlS = urlS.replaceAll(" ", "%20");
            final URI uri = new URI(urlS);
            return new URL(uri.toASCIIString());
        } catch (URISyntaxException | MalformedURLException e) {
            // give up and return the original input
            return u;
        }
    }

}
