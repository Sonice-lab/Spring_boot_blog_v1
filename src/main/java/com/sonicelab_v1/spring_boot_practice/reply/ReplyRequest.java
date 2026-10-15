package com.sonicelab_v1.spring_boot_practice.reply;

import com.sonicelab_v1.spring_boot_practice._core.error.Exception400;
import com.sonicelab_v1.spring_boot_practice.board.Board;
import com.sonicelab_v1.spring_boot_practice.user.User;
import lombok.Data;

public class ReplyRequest {

    // 댓글 정보를 통째로 가져오기 위한 DTO 설계
    // 댓글 작성 DTO
    @Data
    public static class SaveDto {
        // 어떤 게시글에 대한 댓글인지 명시
        private Long boardId;// 댓글을 달 게시글 id -> 화면에서는 보이지 않도록 input의 hidden기능 활용
        private String comment;

        // 엉망으로 작성되는 부분이 요청되는 것을 방지
        public void validate() {
            if (boardId == null) {
                throw new Exception400("게시글 정보가 없습니다.");
            }
            if (comment == null || comment.trim().isEmpty()) {
                throw new Exception400("댓글 내용을 입력해주세요.");
            }
            if (comment.length() > 500) {
                throw new Exception400("댓글은 500자 이하로 입력해주세요.");
            }
        }

        // 편의메서드 구현
        // toEntity
        public Reply toEntity(Board board, User user) {
            return Reply.builder()
                    .comment(this.comment)
                    .board(board)
                    .user(user)
                    .build();
        }
    }
}
