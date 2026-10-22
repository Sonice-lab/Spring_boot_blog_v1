package com.sonicelab_v1.spring_boot_practice.user;


import com.sonicelab_v1.spring_boot_practice._core.util.Define;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@RequiredArgsConstructor //DI 처리
@Slf4j
@Controller //IoC(제어의 역전) 싱글톤 패턴으로 관리됨
public class UserController {

    private final UserService userService;

    // 프로필 삭제
    @PostMapping("/user/profile-image/delete")
    public String deleteProfileImage(HttpSession session) {
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        userService.deleteProfileImage(sessionUserDto.getId());
        return "redirect:/user/detail";
    }

    // GET http://localhost:8080/user/detail
    @GetMapping("/user/detail")
    public String detail(Model model, HttpSession session) {
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        UserResponse.DetailDto user = userService.findDetail(sessionUserDto.getId());
        model.addAttribute("user", user);
        return "user/detail";
    }

    // 1. 회원가입 화면 요청 및 정보 저장(등록)
    // 주소 설계
    // GET http://localhost:8080/join
    @GetMapping("/join")
    public String joinForm() {
        return "user/join-form";
    }

    // POST http://localhost:8080/join
    @PostMapping("/join")
    public String join(UserRequest.JoinDto joinDto, Model model) {
        // 기능 코드 추가
        // 유효성 검사(인증 검사 필요 x)
        joinDto.validate();
        userService.join(joinDto);
        return "redirect:/login";
    }

    // 2. 로그인 화면 요청 및 정보 저장(등록)
    // 주소 설계
    // GET http://localhost:8080/login
    @GetMapping("/login")
    public String loginForm(@CookieValue(name = "rememberUsername", required = false) String rememberUsername, Model model) {
        // 쿠키가 존재하면 뷰로 값을 넘겨준다.
        if (rememberUsername != null) {
            model.addAttribute("rememberUsername", rememberUsername);
        }
        // templates/ <- 콘텐츠 루트 경로
        return "user/login-form";
    }

    // POST http://localhost:8080/login
    // 정보를 불러올 때 예외적으로 POST 처리 -> 보안상의 이유
    @PostMapping("/login")
    public String login(UserRequest.LoginDto loginDto, HttpSession session, HttpServletResponse response, Model model) {

        try {
            // 1. 입력데이터 검증 -> 유효성 검사
            loginDto.validate();

            // 2. 서비스 단에서 로그인 처리 (결과로 SessionUserDto 반환)
            SessionUserDto sessionUserDto = userService.login(loginDto);

            // 3. 보안상의 이유로 비밀번호는 저장하지 않고 세션에 저장
            session.setAttribute(Define.SESSION_USER, sessionUserDto);

            // 4. 미션 3 & 4 적용: 쿠키 생성 및 삭제 로직
            if (loginDto.isRememberId()) {
                // 미션 3: 체크하고 로그인 시 7일짜리 쿠키 저장
                // 주의: SessionUserDto에서 아이디를 가져오는 메서드가 getUsername()이라고 가정했습니다.
                Cookie cookie = new Cookie("rememberUsername", sessionUserDto.getUsername());
                // 7일 유지(초로 환산)
                cookie.setMaxAge(60 * 60 * 24 * 7);
                // 자바스크립트에서 읽을 수 없게 함(보안을 위해)
                cookie.setHttpOnly(true);
                // 애플리케이션 모든 경로에서 유효
                cookie.setPath("/");
                response.addCookie(cookie);

            } else {
                // 미션 4: 체크를 풀고 로그인 시 기존 쿠키 삭제
                Cookie cookie = new Cookie("rememberUsername", "");
                // 수명을 0으로 설정하여 삭제
                cookie.setMaxAge(0);
                cookie.setPath("/");
                response.addCookie(cookie);
            }

            // 5. 성공시 메인페이지로 리다이렉트
            return "redirect:/";

        } catch (Exception e) {
            // 로그인 실패 시 에러메세지와 함께 로그인 폼으로 돌려보내기
            model.addAttribute("errorMessage", e.getMessage());
            return "user/login-form";
        }
    }

    // 3. 사용자 정보 수정
    // 주소 설계
    // GET http://localhost:8080/user/update
    @GetMapping("/user/update")
    public String updateForm(Model model, HttpSession session) {

        //인증 검사
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        UserResponse.updateFormDto updateFormDto = userService.findUpdateForm(sessionUserDto.getId());
        model.addAttribute("user", updateFormDto);
        return "user/update-form";
    }

    // POST http://localhost:8080/user/update
    @PostMapping("/user/update")
    public String update(UserRequest.UpdateDto updateDto, Model model, HttpSession session) {
        updateDto.validate();
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        SessionUserDto updateSessionUserDto = userService.updateById(sessionUserDto.getId(), updateDto);
        // 세션 동기화 처리
        session.setAttribute(Define.SESSION_USER, updateSessionUserDto);
        return "redirect:/user/detail";
    }

    // 로그아웃 경로
    // GET http://localhost:8080/logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // 세션 무효화 처리
        session.invalidate();
        return "redirect:/";
    }
}
