package com.sonicelab_v1.spring_boot_practice._core.config;


import com.sonicelab_v1.spring_boot_practice._core.interceptor.IpBlockInterceptor;
import com.sonicelab_v1.spring_boot_practice._core.interceptor.LoginInterceptor;
import com.sonicelab_v1.spring_boot_practice._core.interceptor.SessionUserModelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@RequiredArgsConstructor // 의존성 주입
@Configuration // IoC 스프링 설정 클래스임을 표시(빈으로도 등록 가능)
public class WebMvcConfig implements WebMvcConfigurer {

    // 로그인 인터셉트와의 관계 -> 연관관계
    private final IpBlockInterceptor ipBlockInterceptor;
    private final LoginInterceptor loginInterceptor;
    private final SessionUserModelInterceptor sessionUserModelInterceptor;

    //loginInterceptor를 시스템에 등록
    //내가 정의한 인터셉트를 설정 클래스에 등록할 수 있다.
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // 0. 세션의 로그인 정보를 화면 데이터(Model)에 담아주는 인터셉터
        registry.addInterceptor(sessionUserModelInterceptor)
                        .addPathPatterns("/**"); //모든 경로에 대한 부분


        // 1. 차단한 IP 접근의 경우 접속을 막기 위한 인터셉터
        registry.addInterceptor(ipBlockInterceptor)
                .addPathPatterns("/**");

        // 2. 로그인이 되지 않고는 접근할 수 없는 경우 로그인 요청 관련 인터셉터
        registry.addInterceptor(loginInterceptor)
                // 인터셉터가 동작할  URL 패턴을 지정
                .addPathPatterns("/user/**", "/board/**", "/reply/**")
                // 인터셉터에서 제외할 URL 패턴을 지정할 수 있다.
                .excludePathPatterns("/board/list", "/board/{id:\\d+}", "/error");

        // \\d+는 정규 표현식으로 1개 이상의 숫자를 의미
        // 예시) /board/1, /board/123 (상세보기)는 로그인이 없어도 접근 가능
        // /board/1/update 처럼 뒤에 경로가 더 붙는 경우 제외대상이 아니게 된다.
    }
}
