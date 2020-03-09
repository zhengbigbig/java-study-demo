package com.zbb.basicserver.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhengzhiheng on 2020/3/7 1:57 下午
 * Description:
 */

@Service
@Log
public class CustomExpiredSessionStrategy implements SessionInformationExpiredStrategy {

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onExpiredSessionDetected(SessionInformationExpiredEvent event) throws IOException, ServletException {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "fail");
        result.put("msg", "您已经在其他地方登录，被迫下线！");
        HttpServletResponse response = event.getResponse();
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(result));
        log.info("被迫下线！");
    }
}
