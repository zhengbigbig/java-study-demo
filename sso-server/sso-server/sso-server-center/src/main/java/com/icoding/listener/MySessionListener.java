package com.icoding.listener;

import com.icoding.db.MockDB;
import com.icoding.utils.HttpUtil;
import com.icoding.vo.ClientInfoVo;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.List;

public class MySessionListener implements HttpSessionListener {

    // Session创建的时候执行的操作
    public void sessionCreated(HttpSessionEvent se) {

    }

    // Session销毁的时候执行的操作
    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        String token = (String) session.getAttribute("token");

        //销毁表中的数据
        MockDB.T_TOKEN.remove(token);
        List<ClientInfoVo> clientInfoLists = MockDB.T_CLIENT_INFO.remove(token);

        for (ClientInfoVo vo : clientInfoLists) {
            try {
                // 服务器端通知所有的客户端进行session 的注销！
                HttpUtil.sendHttpRequest(vo.getClientUrl(),vo.getJsessionid());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
