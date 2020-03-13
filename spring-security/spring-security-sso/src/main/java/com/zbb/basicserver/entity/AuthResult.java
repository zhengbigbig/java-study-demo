package com.zbb.basicserver.entity;

import lombok.Getter;

/**
 * Created by zhengzhiheng on 2020/3/7 5:51 下午
 * Description:
 */

public class AuthResult<T> {
    public enum STATE {
        OK("ok"),
        FAIL("fail");

        private String status;

        STATE(String status) {
            this.status = status;
        }
    }

    STATE status;
    String msg;
    T data;

    public AuthResult(STATE status, String msg, T data) {
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

    public static AuthResult success(String msg, Object data) {
        return new AuthResult(STATE.OK, msg, data);
    }

    public static AuthResult failure(String msg) {
        return new AuthResult(STATE.FAIL, msg, null);
    }

    public String getStatus() {
        return status.status;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }
}
