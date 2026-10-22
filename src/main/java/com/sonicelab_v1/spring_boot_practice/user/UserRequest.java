package com.sonicelab_v1.spring_boot_practice.user;

import com.sonicelab_v1.spring_boot_practice._core.error.Exception400;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

public class UserRequest {

    //회원정보 수정용 DTO 설계
    @Data
    public static class UpdateDto {
        private String password;
        private MultipartFile profileImage; // 새 프로필 이미지 (선택 사항)
        // 비밀번호를 입력했는지 (null, "", "   " ,공백만 입력 모두 -> 입력 안함으로 볼 예정)
        public boolean hasPassword() {
            // 비밀번호 입력시 --> true 반환
            return password != null && !password.isBlank();
        }

        // 새 프로필 사진을 골랐는지
        public boolean hasProfileImage() {
            // 새 파일 선택 시 --> true 반환
            return profileImage != null && !profileImage.isEmpty();
        }

        public void validate() {
            if (!hasPassword() && !hasProfileImage()) {
                throw new Exception400("변경할 비밀번호나 프로필 사진을 입력해주세요");
            }
            if (hasPassword() && password.length() < 4) {
                throw new Exception400("비밀번호는 4글자 이상이어야 합니다");
            }
            // 필요하다면 길이 수 제한, 특수문자 포함여부 (정규식) 활용 가능
        }
    }

    //회원가입용 DTO 설계
    @Data
    public static class JoinDto {
        private String username;
        private String password;
        private String email;
        private MultipartFile profileImage; // 프로필 이미지 (선택 사항)

        //회원가입 시 데이터 검증 메서드를 똑같이 추가하기
        public void validate() {
            if (username == null || username.trim().isEmpty()) {
                throw new Exception400("사용자명은 필수입니다.");
            }

            if (password == null || password.trim().isEmpty()) {
                throw new Exception400("패스워드는 필수입니다.");
            }

            if (email == null || email.trim().isEmpty()) {
                throw new Exception400("이메일은 필수입니다.");
            }

            // 간단하게 이메일 형식만 검증하기
            if (!email.contains("@")) {
                throw new Exception400("올바른 이메일 형식이 아닙니다.");
            }
        }

        //DTO에서 User 엔티티로 변환하는 메서드
        public User toEntity(String profileImageFilename) {
            // 만약 JoinDto의 정보가 전부 들어왔다면 User 객체로 반환해야함
            return User.builder()
                    .username(username)
                    .password(password)
                    .email(email)
                    .profileImage(profileImageFilename)
                    .build();
        }

    } //end of JoinDto

    @Data
    public static class LoginDto {
        private String username;
        private String password;

        // 아이디 저장을 위한 필드 추가
        private boolean rememberId;

        // 회원가입 시 데이터 검증 메서드를 똑같이 추가하기
        public void validate() {
            if (username == null || username.trim().isEmpty()) {
                throw new Exception400("사용자명은 필수입니다.");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new Exception400("패스워드명은 필수입니다.");
            }
        }
    }
}
