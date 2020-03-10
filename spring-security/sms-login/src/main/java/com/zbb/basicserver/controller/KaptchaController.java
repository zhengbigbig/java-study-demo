package com.zbb.basicserver.controller;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.zbb.basicserver.entity.KaptchaImageVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static com.zbb.basicserver.utils.Constants.KAPTCHA_SESSION_KEY;

/**
 * Created by zhengzhiheng on 2020/3/9 7:19 下午
 * Description:
 */

@RestController
public class KaptchaController {

    @Resource
    DefaultKaptcha defaultKaptcha;

    @GetMapping("/kaptcha")
    public void kaptcha(HttpSession session, HttpServletResponse response) throws IOException {
        // 告诉浏览器的配置，做正确的处理，避免缓存等
        response.setDateHeader("Expires", 0);
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");
        response.setContentType("image/jpeg");

        // 1. 创建的核对信息
        String text = defaultKaptcha.createText();
        // 2. 存入session
        session.setAttribute(KAPTCHA_SESSION_KEY,
                new KaptchaImageVO(text, 3 * 60));
        // 3. 创建图片
        BufferedImage image = defaultKaptcha.createImage(text);
        // 4. 传给前端 使用try with
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            ImageIO.write(image, "jpg", outputStream);
            outputStream.flush();
        }
    }
}
