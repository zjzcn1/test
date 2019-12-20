package com.zjzcn.test.util;

import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class HttpClient {

    private static final Logger log = LoggerFactory.getLogger(HttpClient.class);

    private final OkHttpClient httpClient;

    private static final MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");

    private static final int DEFAULT_CONNECT_TIMEOUT_SECONDS = 30;
    private static final int DEFAULT_READ_TIMEOUT_SECONDS = 30;
    private static final int DEFAULT_WRITE_TIMEOUT_SECONDS = 30;

    public HttpClient() {
        this(DEFAULT_CONNECT_TIMEOUT_SECONDS, DEFAULT_READ_TIMEOUT_SECONDS, DEFAULT_WRITE_TIMEOUT_SECONDS);
    }

    public HttpClient(int connectTimeout, int readTimeout, int writeTimeout) {
        httpClient = new OkHttpClient.Builder()
                .followRedirects(true)
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .connectionPool(new ConnectionPool(10, 10, TimeUnit.MINUTES))
                .build();
    }

    public String post(String url, String data) {
        return send(url, "POST", data);
    }

    public String get(String url) {
        return send(url, "GET", null);
    }

    private String send(String url, String method, String data) {

        MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
        requestBody.addFormDataPart("data","dd");
        requestBody.addFormDataPart("token","testValue");

        log.debug("[http] ---->: url={}, req={}", url, data);
        RequestBody body = null;
        if (data != null) {
            body = RequestBody.create(JSON_TYPE, data);
        }
        Request request = new Request.Builder()
                .url(url)
                .method(method, body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String resp = null;
            if (response.body() != null) {
                resp = response.body().string();
            }
            log.debug("[http] <----: url={}, resp={}", url, resp);
            if (response.code() >= 400) {
                throw new HttpException(request + "\n" + response);
            }
            return resp;
        } catch (IOException e) {
            throw new HttpException(e);
        }
    }

    private String buildUrl(String baseUrl, String url) {
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        if (url.startsWith("/")) {
            url = url.substring(1);
        }
        return baseUrl + "/" + url;
    }

    public static class HttpException extends RuntimeException {

        public HttpException(String message) {
            super(message);
        }

        public HttpException(Throwable cause) {
            super(cause);
        }

        public HttpException(String message, Throwable cause) {
            super(message, cause);
        }

    }
}
