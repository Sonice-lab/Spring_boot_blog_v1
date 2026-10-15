package com.sonicelab_v1.spring_boot_practice.board;


import com.sonicelab_v1.spring_boot_practice._core.error.Exception403;
import com.sonicelab_v1.spring_boot_practice._core.error.Exception404;
import com.sonicelab_v1.spring_boot_practice.reply.ReplyJpaRepository;
import com.sonicelab_v1.spring_boot_practice.user.SessionUserDto;
import com.sonicelab_v1.spring_boot_practice.user.User;
import com.sonicelab_v1.spring_boot_practice.user.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor // DI
@Service //IoC (싱글톤 패턴으로 관리됨)
@Transactional(readOnly = true) // 동시성 제어 이슈 -> 안정성 보장 및 성능 느려짐 보완
// 모든 메서드를 읽기 전용 트랜잭션으로 실행
// 성능 최적화(변경 감지 비활성과), 데이터 수정 방지
// 데이터베이스 락(lock)을 최소화하여 동시성 성능 개선
public class BoardService {
    private final BoardJpaRepository boardJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final ReplyJpaRepository replyJpaRepository;


    // 1. 게시글 저장하기
    @Transactional //데이터 수정이 필요하므로 트랜잭션 읽기 전용 설정을 해제하고 쓰기 가능한 트랜잭션으로 실행
    public Board save(BoardRequest.SaveDto saveDto, SessionUserDto sessionUser) {
        log.info("게시글 저장 서비스 - 제목: {}, 작성자: {}",
                saveDto.getTitle(), sessionUser.getUsername());

        // Board board = saveDto.toEntity(sessionUser);
        User user = userJpaRepository.getReferenceById(sessionUser.getId());
        Board savedBoard = boardJpaRepository.save(saveDto.toEntity(user)); //pk, id, 생성시간이 포함되어있음

        log.info("게시글 저장 완료 - ID: {}, 제목: {}",
                savedBoard.getId(), savedBoard.getTitle());

        return savedBoard;
    }

    // 2. 게시글 상세조회
    // 기본키로 조회 -> 트랜잭션을 걸 필요 없음
    public BoardResponse.DetailDto findById(Long id) {
        log.info("게시글 상세 조회 서비스 - ID: {}", id);
        // 직접 정의한 쿼리 작성
        Board board = boardJpaRepository.findByIDJoinUser(id)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다."));
        log.info("게시글 상세 조회 완료 - 제목: {}, 작성자: {}",
                board.getTitle(), board.getUser().getUsername());
        return new BoardResponse.DetailDto(board);
    }

    // 3. 게시글 목록조회
    public BoardResponse.PageDto findAllPage(int page, int size, String keyword) {
        log.info("게시글 목록 조회 시작");
        int pageIndex = Math.max(0, page - 1);
        int validSize = Math.max(1, Math.min(50, size));

        // 최신글이 먼저 오도록 아이디 내림차순 정렬(주문서 생성)
        Pageable pageable = PageRequest.of(pageIndex, validSize, Sort.by(Sort.Direction.DESC, "id"));
        // isBlank: null이 아닌 상태에서 공백 제거 후 개수 확인
        Page<Board> boardPage;
        if (keyword == null || keyword.isBlank()) {
            boardPage = boardJpaRepository.findAllJoinUser(pageable);
        } else {
            boardPage = boardJpaRepository.findAllKeywordJoinUser(pageable, keyword);
        }
        return new BoardResponse.PageDto(boardPage);
    }

    // 4. 게시글 수정 화면 요청(인가 검사) - 권한체크
    @Transactional
    public BoardResponse.UpdateFormDto findByIdAndUpdateForm(Long id, Long userId) {
        Board board = boardJpaRepository.findByIDJoinUser(id)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다."));

        // 인가 검사
        if (!board.isOwner(userId)) {
            throw new Exception403("게시글 수정 권한이 없습니다.");
        }
        return new BoardResponse.UpdateFormDto(board);
    }

    // 4. 게시글 수정
    @Transactional
    public Board updateById(Long id, BoardRequest.UpdateDto updateDto, SessionUserDto sessionUser) {
        log.info("게시글 목록 수정 시작");

        //수정하기 전에 게시글을 먼저 조회해야 한다.
        // 가. 게시글 조회
        Board board = boardJpaRepository.findByIDJoinUser(id)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다."));

        // 나. 권한 체크
        if (!board.isOwner(sessionUser.getId())) {
            throw new Exception403("본인이 작성한 게시글만 수정할 수 있습니다.");
        }

        // 다. 더티체킹을 활용한 수정
        board.update(updateDto);

        // 다. 유효성 검사는 컨트롤러에서 진행
        log.info("게시글 목록 수정 완료");
        return board;
    }

    // 5. 게시글 삭제
    @Transactional
    public void deleteById(Long id, SessionUserDto sessionUser) {
        log.info("게시글 삭제 시작");
        // 조회 시작
        Board board = boardJpaRepository.findByIDJoinUser(id)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다."));

        // 권한 확인
        if (!board.isOwner(sessionUser.getId())) {
            log.warn("게시글 삭제 권한 없음 - 게시글 ID: {}, 작성자: {}, 요청자: {}",
                    id, board.getUser().getUsername(), sessionUser.getUsername());
            throw new Exception403("본인이 작성한 게시글만 삭제할 수 있습니다.");
        }

        // boardId로 댓글부터 먼저 삭제
        replyJpaRepository.deleteByBoardId(id);

        boardJpaRepository.deleteById(id);
        log.info("게시글 삭제 완료");
    }
}
