package com.sonicelab_v1.spring_boot_practice.user;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;



@Getter
@NoArgsConstructor //기본 생성자 필수(JPA 엔티티 생성자)
@AllArgsConstructor
@Table(name="user_tb")
@Entity  //자동 테이블 생성
@Data
public class User {

    // pk 만들기
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 같은 사용자명을 두 번 가입할 수 없도록 unique 제약 걸기(이메일)
    // 유니크 설정
    @Column(unique = true)
    private String username;
    // 길이 300자로 설정
    @Column(length = 300)
    @Setter
    private String password;
    @Column(unique = true)
    private String email;

    @CreationTimestamp //now()
    private Timestamp createdAt;

    //편의성을 위한 생성자 만들기
    //id와 createdAt는 자동으로 채워지므로 빌더에서 제외한다.
    @Builder
    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    // 회원정보 수정 - 더티체킹 (변경 감지용)
    public void update(String password) {
        this.password = password;
    }

}
