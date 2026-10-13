package com.sonicelab_v1.spring_boot_practice.user;


import com.sonicelab_v1.spring_boot_practice._core.util.Define;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@RequiredArgsConstructor //DI 처리
@Slf4j
@Controller //IoC(제어의 역전) 싱글톤 패턴으로 관리됨
public class UserController {

    private final UserService userService;

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
    public String loginForm() {
        return "user/login-form";
    }

    // POST http://localhost:8080/login
    // 정보를 불러올 때 예외적으로 POST 처리 -> 보안상의 이유
    @PostMapping("/login")
    public String login(UserRequest.LoginDto loginDto, HttpSession session) {

        // 입력데이터 검증 -> 유효성 검사
        loginDto.validate();
        SessionUserDto sessionUserDto = userService.login(loginDto);

        // 보안상의 이유로 비밀번호는 저장하고 싶지 않음
        session.setAttribute(Define.SESSION_USER, sessionUserDto);

        // 성공시 메인페이지로 리다이렉트
        return "redirect:/";
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
    @PostMapping("user/update")
    public String update(Model model, HttpSession session, UserRequest.UpdateDto updateDto) {

        // 1. 유효성 검사
        updateDto.validate();

        // 2. 인증 검사 -> 세션 유효기간이 만료되었을 때 필요
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        SessionUserDto updateSessionUserDto = userService.updateById(sessionUserDto.getId(), updateDto);

        // 3. 세션 동기화 -> 수정된 비밀번호 정보를 업데이트하여 세션에 반영
        // 동기화 처리
        session.setAttribute(Define.SESSION_USER, updateSessionUserDto);

        // 4. 성공 후 메인페이지로 리다이렉트
        return "redirect:/";
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
