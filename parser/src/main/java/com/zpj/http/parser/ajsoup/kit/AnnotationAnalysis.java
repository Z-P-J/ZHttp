package com.zpj.http.parser.ajsoup.kit;


import com.zpj.http.parser.ajsoup.annotation.Select;
import com.zpj.http.parser.jsoup.select.Elements;
import com.zpj.http.parser.jsoup.utils.StringUtil;

import java.lang.annotation.Annotation;

/**
 * Created by zoudong on 2017/3/12.
 */

public class AnnotationAnalysis {

    public  static Elements analysis(Elements els, Annotation[] ans){
        for (Annotation an : ans) {
            els = select(els, an);
        }
        return els;
    }

    private static Elements select(Elements els, Annotation an) {
        if (an instanceof Select) {
            try {
                String select = ((Select) an).select();
                if (!StringUtil.isBlank(select)) {
                    els = els.select(select.trim());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return els;
    }
}
