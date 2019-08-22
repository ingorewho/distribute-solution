package com.distribute.common.response;

import java.io.Serializable;

/**
 * @Author renzhiqiang
 * @Description Api响应
 * @Date 2019-08-21
 **/
public class ApiResponse<T> implements Serializable {
    private String msg;

    private Integer code;

    private T data;

    private static final Integer SUCCESS = 200;

    private static final Integer FAILED = -1;

    public ApiResponse(String msg, Integer code, T data) {
        this.msg = msg;
        this.code = code;
        this.data = data;
    }

    public static<T> ApiResponse<T> buildSucess(T data) {
        return new ApiResponse<>(null, SUCCESS, data);
    }

    public static<T> ApiResponse<T> buildFailed(String msg) {
        return new ApiResponse<>(msg, FAILED, null);
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
