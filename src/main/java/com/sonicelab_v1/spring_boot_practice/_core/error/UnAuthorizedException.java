package com.sonicelab_v1.spring_boot_practice._core.error;

// 로그인 페이지로 이동할 수 있도록 이동
public class UnAuthorizedException extends RuntimeException {
    public UnAuthorizedException(String msg) {
        super(msg);
    }
}
