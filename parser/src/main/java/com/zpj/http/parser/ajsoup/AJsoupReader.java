package com.zpj.http.parser.ajsoup;


import com.zpj.http.parser.Jsoup;
import com.zpj.http.parser.ajsoup.data.ClassDescriptor;
import com.zpj.http.parser.ajsoup.data.ClassReader;
import com.zpj.http.parser.ajsoup.data.TypeLiteral;
import com.zpj.http.parser.ajsoup.kit.AnalysisDecoder;
import com.zpj.http.parser.ajsoup.kit.AnnotationAnalysis;
import com.zpj.http.parser.jsoup.nodes.Document;
import com.zpj.http.parser.jsoup.select.Elements;


/**
 * Created by zoudong on 2017/3/10.
 */

public class AJsoupReader {
    public static final boolean isDebug=false;
    public static ThreadLocal<AJsoupReader> jsp = new ThreadLocal<AJsoupReader>() {
        @Override
        protected AJsoupReader initialValue() {
            return new AJsoupReader();
        }
    };

    public static final <T> T deserialize(Document document, Class<T> clazz) {
        AJsoupReader context = jsp.get();
        ClassDescriptor classDescriptor = ClassReader.getClassDescriptor(clazz, true);

        if (classDescriptor.clazz_anno == null)
            throw new RuntimeException(clazz + " you must used  once Annotation ");
        Elements elements = AnnotationAnalysis.analysis(document.children(), classDescriptor.clazz_anno);
        T val = context.read(clazz, new AJsoupReaderContext(elements, classDescriptor.clazz_anno));
        return val;
    }
    public static final <T> T deserialize(String document, Class<T> clazz) {
        AJsoupReader context = jsp.get();
        ClassDescriptor classDescriptor = ClassReader.getClassDescriptor(clazz, true);
        Document parse = Jsoup.parse(document);
        if (classDescriptor.clazz_anno == null)
            throw new RuntimeException(clazz + " you must used  once Annotation ");
       if (isDebug) System.out.println("deserialize: "+classDescriptor.clazz_anno[0].toString() );
        Elements elements = AnnotationAnalysis.analysis(parse.children(), classDescriptor.clazz_anno);
        if (isDebug) System.out.println("---->" +elements.html());
        T val = context.read(clazz, new AJsoupReaderContext(elements, classDescriptor.clazz_anno));
        return val;
    }
    @SuppressWarnings("unchecked")
    public final <T> T read(Class<T> clazz, AJsoupReaderContext iterator) {
        return (T) AnalysisDecoder.getDecoder(TypeLiteral.create(clazz).getDecoderCacheKey(), clazz).decode(iterator);
    }
}
