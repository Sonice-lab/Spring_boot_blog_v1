package com.sonicelab_v1.spring_boot_practice._core.interceptor;


import com.sonicelab_v1.spring_boot_practice._core.util.Define;
import com.sonicelab_v1.spring_boot_practice.user.SessionUserDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 세션의 로그인 정보를 화면 데이터(Model)에 담아주는 인터셉터
 * 특징
 * - 컨트롤러마다 Model.addAttribute("sessionUser", sessionUserDto)를 매번 작성하지 않아도 된다.
 * - 세션 전체가 아닌 sessionUserDto 하나만 화면에 전달된다.
 */

@Component // IoC - 제어의 역전
public class SessionUserModelInterceptor implements HandlerInterceptor {

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {

        // 뷰랜더링 되기 직전
        // 1. 화면을 반환하는 요청만 처리
        // 컨트롤러에 들어왔다가 응답을 할 때 데이터만 반환하는 요청을 무시, 즉, 동작시키지 않는다.
        // @ResponseBody, @RestController 요청 무시
        // -> 이러한 요청을 애초에 화면에 그리지 않으므로 HTML이 필요없는 요소(데이터만 내리기 때문에)
        // -> "딱 화면을 그릴 때만 공통 데이터를 넣어주겠다"는 명확한 목적 -> 필터링
        if(modelAndView == null) {
            return; //요청을 들어주지않고 돌려보낸다.
        }

        // 2. 리다이렉트 응답은 화면을 그리지 않으므로 건너뜀(동작시키지 않음)
        String viewName = modelAndView.getViewName();
        if(viewName != null && viewName.startsWith("redirect:")) {
            return;
        }

        // 3. 세션이 있을 때(로그인 했을 때)만 조회, 없으면 새로 만들지 않음
        // 중요!
        HttpSession session = request.getSession(false);
        if(session == null) {
            return;
        }

        // 4. 로그인 정보가 있을 경우 화면에 데이터 추가
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        if(sessionUserDto != null) {
            modelAndView.addObject(Define.SESSION_USER, sessionUserDto);
        }

    }
}
