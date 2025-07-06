package com.cqupt.lark.util;

public class UrlStringAdder {
    public static String urlStrAdd(String url) {

        // 避免url报错
        String newUrl = url;
        if (!url.startsWith("https://") && !url.startsWith("http://")) {
            newUrl = "https://" + url;
        }
        return newUrl;
    }
}
