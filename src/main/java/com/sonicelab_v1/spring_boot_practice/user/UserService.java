package com.sonicelab_v1.spring_boot_practice.user;

import com.sonicelab_v1.spring_boot_practice._core.error.Exception400;
import com.sonicelab_v1.spring_boot_practice._core.error.Exception404;
import com.sonicelab_v1.spring_boot_practice._core.util.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * 사전기반 지식
 * 역할과 책임을 분리하는 방식
 * 서비스 레이어
 * 핵심개념:
 * - 비즈니스 로직을 처리하는 계층
 * - Controller와 Repository 사이의 중간 계층을 담당
 * - 서비스에서 트랜잭션 관리에 대한 부분도 같이 처리
 * 3Tier 아키텍처
 * C -> S -> R -> DB
 */

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 읽기 전용으로 트랜잭션 설정
@Service
public class UserService {

    private final UserJpaRepository userJpaRepository; //DI 주입 완료

    // 1. 회원가입 기능
    @Transactional // 쓰기 작업이므로 트랜잭션 활성화
    public UserResponse.JoinDto join(UserRequest.JoinDto joinDto) {
        log.info("회원 가입 서비스 처리 시작 - 사용자명: {}", joinDto.getUsername());

        //비즈니스 로직
        // 1. 중복체크하기 - 이미 있으면 null이 아님(ifPresent 차이점 확인)
        userJpaRepository.findByUsername(joinDto.getUsername())
                .ifPresent(user -> {
                    throw new Exception400("이미 존재하는 사용자명입니다.");
                });

        // 2. 이메일 중복 검사
        userJpaRepository.findByEmail(joinDto.getEmail())
                .ifPresent(user -> {
                    throw new Exception400("이미 사용중인 이메일입니다.");
                });

        // 3. 프로필 이미지 저장 ( 새롭게 생긴 파일 이름이 리턴 됨)
        String profileImage = FileUtil.saveImage(joinDto.getProfileImage());

        // 4. 엔티티 생성 후 기본 역할 USER 부여
        User user = joinDto.toEntity(profileImage);
        user.addRole(Role.USER);

        // 5. 프로필 파일 이름과 회원 정보 저장(cascade 로 UserRole도 함께 INSERT)
        User savedUser = userJpaRepository.save(user);
        log.info("회원가입 서비스 처리 완료 - 사용자명: {}, ID: {} ", joinDto.getUsername(), savedUser.getId());
        return new UserResponse.JoinDto(savedUser);
    }

    // 5. 로그인 처리 -> 조회 기능과 관련 있음
    public SessionUserDto login(UserRequest.LoginDto loginDto) {
        log.info("로그인 서비스 처리 시작 - 사용자명: {}", loginDto.getUsername());

        User user = userJpaRepository.findByUsernameAndPassword(
                        loginDto.getUsername(), loginDto.getPassword())
                .orElseThrow(() -> {
                    log.warn("로그인 실패 - 사용자명: {}, 원인: 인증정보 불일치", loginDto.getUsername());
                    return new Exception400("사용자명 또는 비밀번호가 올바르지 않습니다.");
                });

        log.info("로그인 성공 - 사용자명: {}", loginDto.getUsername());
        return new SessionUserDto(user);
    }

    // 6. 회원정보 수정화면: 화면에 필요한 값만 담는 DTO 변환
    public UserResponse.updateFormDto findUpdateForm(Long userId) {
        User user = findById(userId);
        return new UserResponse.updateFormDto(user);
    }

    // 7. 회원정보 수정 처리(Dirty Checking 활용)
    /**
     * 회원 정보 수정 처리 (Dirty Checking 활용)
     *
     * @param id 사용자 PK
     * @param updateDto 수정할 정보
     * @return 수정된 사용자 정보
     */
// 세션 동기화용 DTO 반환
    @Transactional
    public SessionUserDto updateById(Long id, UserRequest.UpdateDto updateDto) {
        // 1. 정보 조회
        User user = findById(id);
        // 2. 비밀번호 조회
        if (updateDto.hasPassword()) {
            user.update(updateDto.getPassword());
        }
        // 3. 새 이미지를 골랐을 때만 교체
        if (updateDto.hasProfileImage()) {
            String newImage = FileUtil.saveImage(updateDto.getProfileImage());
            String oldImage = user.getProfileImage();
            user.updateProfileImage(newImage); // DB에는 새 파일 이름 (변경 감지)
            FileUtil.deleteFile(oldImage);
        }
        // 4. 세션 동기화용 DTO 변환
        return new SessionUserDto(user);
    }

    // 8. 사용자 정보 조회
    public User findById(Long id) {
        log.info("사용자 정보 조회 - id: {}", id);
        return userJpaRepository.findById(id).orElseThrow(() ->
                new Exception404("사용자를 찾을 수 없습니다."));
    }

    // 마이페이지 조회 기능
    public UserResponse.DetailDto findDetail(Long userId) {
        User user = findById(userId);
        return new UserResponse.DetailDto(user);
    }

    @Transactional
    public void deleteProfileImage(Long userId) {
        User user = findById(userId);
        String oldImage = user.getProfileImage();
        user.updateProfileImage(null); // 1. DB에  파일 이미지 비우기 (변경 감지)
        FileUtil.deleteFile(oldImage); // 2. 폴더 실제 파일 삭제
    }
}
