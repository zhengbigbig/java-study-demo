package com.zbb.basicserver.entity;

import lombok.Getter;

/**
 * Created by zhengzhiheng on 2020/3/7 5:51 下午
 * Description:
 */

@Getter
public class AuthResult<T> {
    public enum STATE {
        OK("ok"),
        FAIL("fail");

        private String status;

        STATE(String status) {
            this.status = status;
        }
    }

    STATE state;
    String msg;
    T data;

    public AuthResult(STATE state, String msg, T data) {
        this.state = state;
        this.msg = msg;
        this.data = data;
    }

    public static AuthResult success(String msg, Object data) {
        return new AuthResult(STATE.OK, msg, data);
    }

    public static AuthResult failure(String msg) {
        return new AuthResult(STATE.FAIL, msg, null);
    }
}
