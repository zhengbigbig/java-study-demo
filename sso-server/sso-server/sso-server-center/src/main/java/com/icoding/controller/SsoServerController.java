package com.icoding.controller;

import com.icoding.db.MockDB;
import com.icoding.vo.ClientInfoVo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
public class SsoServerController {

    @RequestMapping("/index")
    public String index(){
        return "login";
    }

    // redirectUrl 我从哪里来~
    @RequestMapping("/login")
    public String login(String username,String password,
                        String redirectUrl,HttpSession session,Model model){
        System.out.println(username+","+password);

        //登录成功
        if ("admin".equals(username) && "123456".equals(password)){
            // 1. 服务器端创建令牌
            String token = UUID.randomUUID().toString();
            System.out.println("token创建成功=>"+token);
            // 2. 创建全局会话，将令牌信息存入
            session.setAttribute("token",token+"xxx");
            session.setAttribute("xxx","yyy");
            // 3. 存在数据库中
            MockDB.T_TOKEN.add(token);
            // 4. 返回给用户，来时的地方
            model.addAttribute("token",token);
            model.addAttribute("zzz","zzz");
            return "redirect:"+redirectUrl; //  ?token = xxxxx
        }
        System.out.println("用户名或密码错误！");
        model.addAttribute("redirectUrl",redirectUrl);
        // 登录的操作，保存token数据  redis。。。 mock
        return "login";
    }

    // checkLogin
    @RequestMapping("/checkLogin")
    public String checkLogin(String redirectUrl, HttpSession session,Model model){

        //1. 是否存在会话。
        String token = (String) session.getAttribute("token");
        if (StringUtils.isEmpty(token)){
            // 没有全局会话，需要登录，跳转到登录页面，需要携带我从哪里来~
            model.addAttribute("redirectUrl",redirectUrl);
            return "login";
        }else { // 存在全局会话
            // 取出令牌 token，返回给客户端
            model.addAttribute("token",token);
            return "redirect:" + redirectUrl; //model  ?token=xxxx
        }

    }

    //verify
    @RequestMapping("/verify")
    @ResponseBody
    public String verifyToken(String token,String clientUrl,String jsessionid){
        if (MockDB.T_TOKEN.contains(token)){
            System.out.println("服务器端token校验成功！"+token);
            // 存表操作！
            List<ClientInfoVo> clientInfoVoList = MockDB.T_CLIENT_INFO.get(token);
            if (clientInfoVoList==null){
                clientInfoVoList = new ArrayList<ClientInfoVo>();
                // 当前的用户信息
                MockDB.T_CLIENT_INFO.put(token,clientInfoVoList);
            }

            ClientInfoVo vo = new ClientInfoVo();
            vo.setClientUrl(clientUrl);
            vo.setJsessionid(jsessionid);
            clientInfoVoList.add(vo);

            return "true";
        }
        return "false";
    }


    // Session  手动注销，自动注销！ 监听器！
    //logOut
    @RequestMapping("/logOut")
    public String logOut(HttpSession session){
        session.invalidate();
        //客户端的session 应该在在这里通知销毁吗？
        //存在自动注销，我们需要在监听器中实现
        return "login";
    }



}
