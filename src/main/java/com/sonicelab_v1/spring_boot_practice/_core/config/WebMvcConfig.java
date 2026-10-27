package com.sonicelab_v1.spring_boot_practice._core.config;


import com.sonicelab_v1.spring_boot_practice._core.interceptor.AdminInterceptor;
import com.sonicelab_v1.spring_boot_practice._core.interceptor.IpBlockInterceptor;
import com.sonicelab_v1.spring_boot_practice._core.interceptor.LoginInterceptor;
import com.sonicelab_v1.spring_boot_practice._core.interceptor.SessionUserModelInterceptor;
import com.sonicelab_v1.spring_boot_practice._core.util.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Paths;


@RequiredArgsConstructor // 의존성 주입
@Configuration // IoC 스프링 설정 클래스임을 표시(빈으로도 등록 가능)
public class WebMvcConfig implements WebMvcConfigurer {

    // 로그인 인터셉트와의 관계 -> 연관관계
    private final IpBlockInterceptor ipBlockInterceptor;
    private final LoginInterceptor loginInterceptor;
    private final SessionUserModelInterceptor sessionUserModelInterceptor;
    private final AdminInterceptor adminInterceptor;

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
                .addPathPatterns("/user/**", "/board/**", "/reply/**", "/admin/**")
                // 인터셉터에서 제외할 URL 패턴을 지정할 수 있다.
                .excludePathPatterns("/board/list", "/board/{id:\\d+}", "/error");

        // \\d+는 정규 표현식으로 1개 이상의 숫자를 의미
        // 예시) /board/1, /board/123 (상세보기)는 로그인이 없어도 접근 가능
        // /board/1/update 처럼 뒤에 경로가 더 붙는 경우 제외대상이 아니게 된다.

        // 3. 관리자 검사: 로그인 검사 다음에 등록 (등록한 순서대로 실행됨)
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/admin/**");
    }

    // 업로드 폴더의 파일을 /images/** 주소로 제공
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 업로드 폴더 경로를 file:///.... 형식의 주소로 변환(운영체제에 맞게 자동 변환)
        String location = Paths.get(FileUtil.UPLOAD_DIR).toUri().toString();

        // 윈도우 기준 예시 -> C:\Users\사용자명\blog_uploads\ -> 이 주소로 바꿈
        // 방어적 코드: 폴더 위치는 반드시 /로 끝나야 그 안의 파일을 찾을 수 있음
        if(!location.endsWith("/")) {
            location = location + "/";
        }

        // 핵심 키워드 ->
        registry.addResourceHandler("/images/**")
                .addResourceLocations(location);

    }
}
