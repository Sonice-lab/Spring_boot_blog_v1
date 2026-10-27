package com.sonicelab_v1.spring_boot_practice.admin;


import com.sonicelab_v1.spring_boot_practice._core.util.Define;
import com.sonicelab_v1.spring_boot_practice.user.SessionUserDto;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {
    // GET http://localhost:8080/admin/dashboard
    @GetMapping("/admin/dashboard")
    public String dashBoard(Model model, HttpSession session) {

        // 로그인 했는지 안했는지 확인
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);

        model.addAttribute("adminName", sessionUserDto.getUsername());
        return "admin/dashboard";
    }
}