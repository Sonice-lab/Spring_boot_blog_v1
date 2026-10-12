package com.sonicelab_v1.spring_boot_practice.board;

import com.sonicelab_v1.spring_boot_practice._core.error.Exception400;
import com.sonicelab_v1.spring_boot_practice.user.User;
import lombok.Data;

public class BoardRequest {

    @Data
    public static class SaveDto {
        private String title;
        private String content;

        // 검증메서드
        public void validate() {
            if (title == null || title.trim().isEmpty()) {
                throw new Exception400("제목은 필수입니다.");
            }

            if (content == null || content.trim().isEmpty()) {
                throw new Exception400("내용은 필수입니다.");
            }
        }

        // 메서드 만들기
        //Dto 에서 Entity로 변환하는 편의 메서드 설계(가독성 향상 및 역할 분리)
        public Board toEntity(User user) {
            return Board.builder()
                    .title(this.title)
                    .content(this.content)
                    .user(user)
                    // 매개변수 가지고 오기
                    // 세션에서 가져온 User 객체 설정
                    .build();
        }
    }

    @Data
    public static class UpdateDto {
        private String title;
        private String content;

        //검증 메서드(선택 사항)
        public void validate() {
            if (title == null || title.trim().isEmpty()) {
                throw new Exception400("제목은 필수입니다.");
            }

            if (content == null || content.trim().isEmpty()) {
                throw new Exception400("내용은 필수입니다.");
            }
        }
    }
}
