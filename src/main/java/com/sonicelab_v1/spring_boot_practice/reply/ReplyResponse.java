package com.sonicelab_v1.spring_boot_practice.reply;


import com.sonicelab_v1.spring_boot_practice._core.util.MyDateUtil;
import com.sonicelab_v1.spring_boot_practice.user.SessionUserDto;
import lombok.Data;

public class ReplyResponse {

    // 1. 댓글 목록 응답 DTO
    @Data
    public static class ListDto{
        // 댓글을 불러오기 위해서는 아래와 같은 부분에 대해 확인이 필요하다.
        // 1) 작성자id(pk)
        // 2) 작성자명
        // 3) 댓글내용
        // 4) 포맷된 시간
        // 5) 내 댓글인지의 확인 여부

        // 필드 작성
        private Long id; //pk
        private String username;
        private String comment;
        private String createdAt; // 포맷된 작성일
        // 세션 아이디(로그인 한 아이디)와 댓글 작성자 아이디가 일치하는가?(삭제 버튼 표시용)
        private boolean isOwner;

        // 생성자 설계 -> 외부에서 값을 사용하기 위함
        public ListDto(Reply reply, Long sessionUserId) {
            this.id = reply.getId();
            this.comment = reply.getComment();
            // join fetch로 바로 가져와서 활용
            this.username = reply.getUser().getUsername();
            if(reply.getCreateAt() != null) {
                this.createdAt = MyDateUtil.timestampFormat(reply.getCreateAt());
            }
            this.isOwner = reply.isOwner(sessionUserId);
        }
    }
}
