package com.sonicelab_v1.spring_boot_practice.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<User, Long> {

    // 기본적인 CRUD 기능 안 만들어도 됨 -> 다 구현되어있기 때문

    /**
     * 자동으로 코드를 쓰지 않아도 되는 경우
     * hibernate가 직접 알아서 코드로 구현해준다.
     * 1. 등록 및 수정: save(User entity);
     * - 엔티티를 DB에 저장해야한다.
     * - ID가 없을 경우 자동으로 INSERT를 하며, ID가 있을 경우 자동으로 UPDATE를 실행
     * <p>
     * 2. 단건 조회: findById(Long id)
     * - ID로 엔티티를 조회하면 Optional<Board> 타입을 반환
     * <p>
     * 3. 전체 조회: findAll();
     * - 테이블의 모든 데이터를 조회하며 List<Board>로 반환
     * <p>
     * 4. 삭제: deleteById(Long id);
     * - 특정 ID를 가진 엔티티를 삭제
     * <p>
     * 5. 데이터 갯수: count();
     * - 전체 레코드의 개수를 반환
     * <p>
     * 6. 존재 여부 확인: existById(Lond id);
     * - 해당 ID를 가진 데이터가 있는지 확인하여 boolean 을 반환
     */

    // 추상메서드로 구현
    // 구현1) 사용자명과 비밀번호로 조회(로그인용) - 단 건 조회
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.password = :password")
    Optional<User> findByUsernameAndPassword(@Param("username") String username,
                                             @Param("password") String password);

    // 구현2) 사용자명으로 사용자 조회(중복 체크용)
    @Query("SELECT u FROM User u WHERE u.username = :username")
    Optional<User> findByUsername(@Param("username") String username);

    //구현3) 이메일 중복 체크용
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);


}
