package com.sonicelab_v1.spring_boot_practice.board;


import com.sonicelab_v1.spring_boot_practice._core.error.Exception403;
import com.sonicelab_v1.spring_boot_practice._core.util.Define;
import com.sonicelab_v1.spring_boot_practice.user.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Slf4j
@RequiredArgsConstructor //DI 처리
@Controller
public class BoardController {

    // DI 처리
    private final BoardService boardService;

    //주의) Httpsession 은 상수 선언할 경우 프로그램이 너무 무거워지니까
    // 필요할 때마다 의존 관계를 이용해서 활용할 것

    // 1. 게시글 목록 조회
    @GetMapping({"/", "/board/list"})
    public String list(Model model) {
        List<Board> boardList = boardService.findAll();
        model.addAttribute("boardList", boardList);
        return "board/list";
    }

    // 2. 게시글 상세보기
    // GET http://localhost:8080/board/3
    // excludePathPatterns로 제외되어 로그인 접근 가능
    // @PathVariable: URL 경로(Path) 자체에 포함된 변수 값을 서버로 가져올 때
    //          특정 데이터(리소스) 1개를 콕 집어서 식별할 때
    @GetMapping({"/board/{id}"})
    public String detail(@PathVariable(name = "id") Long id, Model model) {
        Board board = boardService.findById(id);
        model.addAttribute("board", board);
        return "board/detail";
    }

    // 3. 게시글 저장하기
    // GET   http://localhost:8080/board/save  (화면 요청)
    @GetMapping({"/board/save"})
    public String saveForm(HttpSession session) {
        return "board/save-form";
    }

    @PostMapping({"/board/save"})
    public String save(BoardRequest.SaveDto saveDto, HttpSession session) {
        saveDto.validate();
        User sessionUser = (User) session.getAttribute(Define.SESSION_USER);
        boardService.save(saveDto, sessionUser);
        return "redirect:/";
    }

    // todo - 인가 처리(추후) 서비스단으로 이동 예정
    // 4. 게시글 수정하기
    // GET   http://localhost:8080/board/1/update  (수정 화면 요청)
    @GetMapping({"board/{id}/update"})
    public String updateForm(@PathVariable Long id, Model model, HttpSession session) {
        Board board = boardService.findById(id);
        User sessionUser = (User) session.getAttribute(Define.SESSION_USER);
        if (!board.isOwner(sessionUser.getId())) {
            throw new Exception403("수정할 권한이 없습니다.");
        }
        model.addAttribute("board", board);
        return "board/update-form";
    }

    // POST   http://localhost:8080/board/1/update  (게시글 수정 기능 요청)
    // object를 통으로 받기
    @PostMapping({"board/{id}/update"})
    public String update(@PathVariable Long id,
                         BoardRequest.UpdateDto updateDto,
                         HttpSession session) {

        //유효성 검사
        updateDto.validate();
        //서비스 계층에서 업데이트로 넘김
        User sessionUser = (User) session.getAttribute(Define.SESSION_USER);
        boardService.updateById(id, updateDto, sessionUser);

        // 성공했다면 - PRG 패턴 구현 -> 수정 완료 후 해당 게시글 상세보기로 리다이렉트 처리
        // PRG 패턴: POST, Redirect, GET의 앞 글자를 딴 설계 방식
        // 사용자가 폼 데이터를 중복해서 전송(저장/결제 등)하는 것을 막아주는 중요한 핵심 기법
        return "redirect:/board/" + id;

    }

    // 5. 게시글 삭제
    @PostMapping("board/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session) throws Exception403 {
        User sessionUser = (User) session.getAttribute(Define.SESSION_USER);
        boardService.deleteById(id, sessionUser);
        // 삭제 성공 후 메인 페이지 리다이렉트
        return "redirect:/";
    }
}
