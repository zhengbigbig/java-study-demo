package com.coding.http;

import org.springframework.util.StreamUtils;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;

// HttpClient
public class HttpUtil {

    public static String sendHttpRequest(String httpUrl, Map<String,String> params) throws Exception{
        //1. 请求地址 https://way.jd.com/jisuapi/query4
        URL url = new URL(httpUrl);
        //2. 开启连接 Http
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        //3. 设置请求的方式
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        //4. 设置请求携带的参数进行发送！
        if (params!=null && params.size()>0){
            StringBuilder sb = new StringBuilder();

            for (Map.Entry<String, String> entry : params.entrySet()) {
                sb.append("&")
                        .append(entry.getKey())
                        .append("=")
                        .append(entry.getValue());
            }
            // 去除第一个 & 的符号
            connection.getOutputStream().write(sb.substring(1).getBytes("UTF-8"));

        }
        //6.发送请求
        connection.connect();

        //7. 接受响应
        String response = StreamUtils.copyToString(connection.getInputStream(), Charset.forName("UTF-8"));

        return  response;

    }
}
