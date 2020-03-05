package com.demo;

import org.springframework.util.StreamUtils;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Created by zhengzhiheng on 2020/3/4 1:21 上午
 * Description: 新闻api
 */
//Url:https://way.jd.com/jisuapi/get?channel=头条&num=10&start=0&appkey=appkey
public class NewsApi {
    private static final String APP_KEY = "xxx";
    private static final String URL = "https://way.jd.com/jisuapi/get";
    private static final String CHANNEL = "女性";

    public static void main(String[] args) {
        searchNewsByChannel(CHANNEL, 0, 10);

    }

    private static void searchNewsByChannel(String channel, int start, int num) {
        HttpURLConnection connection = null;
        try {
            // 1. 访问地址
            URL url = new URL(URL);
            // 2. 连接地址
            connection = (HttpURLConnection) url.openConnection();
            // 3. 请求方式
            connection.setRequestMethod("GET");
            // 4. 携带参数
            connection.setDoOutput(true);
            StringBuilder params = new StringBuilder();
            params.append("channel=").append(channel)
                    .append("&num=").append(num)
                    .append("&start=").append(start)
                    .append("&appkey=").append(APP_KEY);
            System.out.println(params);
            // stringBuilder toString 输出字节流
            byte[] bytes = params.toString().getBytes(StandardCharsets.UTF_8);
            connection.getOutputStream().write(bytes);

            // 5. 发起请求
            connection.connect();
            // 6. 接受返回值
            String response = StreamUtils.copyToString(connection.getInputStream(), StandardCharsets.UTF_8);
            System.out.println(response);
        } catch (Exception e) {
            System.out.println("连接异常：" + e.getMessage());
        } finally {
            assert connection != null;
            connection.disconnect();
        }

    }
}
