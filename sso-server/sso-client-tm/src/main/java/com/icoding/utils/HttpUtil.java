package com.icoding.utils;

import org.springframework.util.StreamUtils;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;

public class HttpUtil {
    public static String sendHttpRequest(String httpURL, Map<String,String> params) throws Exception {
        //1. 定义需要访问的地址 "https://way.jd.com/he/freeweather"
        URL url = new URL(httpURL);
        //2. 开启连接
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        //3. 设置请求的方式
        connection.setRequestMethod("POST");
        //4. 输出参数
        connection.setDoOutput(true);
        //5. 拼接参数信息 city=beijing&appkey=06642046425c68a351817b5b020b591f
        if (params!=null&&params.size()>0){
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String,String> entry:params.entrySet()){
                sb.append("&").append(entry.getKey()).append("=").append(entry.getValue());
            }
            //6. 去除最前面的 &写出参数
            connection.getOutputStream().write(sb.substring(1).toString().getBytes("UTF-8"));
        }
        //7. 发起请求
        connection.connect();
        //8. 接收对方响应的信息,可以使用Spring的 StreamUtils 工具类
        String response = StreamUtils.copyToString(connection.getInputStream(), Charset.forName("UTF-8"));
        return response;
    }
}
