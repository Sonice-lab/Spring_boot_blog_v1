package com.sonicelab_v1.spring_boot_practice.board;


import com.sonicelab_v1.spring_boot_practice._core.util.MyDateUtil;
import com.sonicelab_v1.spring_boot_practice.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

// [과정1. 엔티티 클래스 만들기]
// 데이터베이스 테이블 한 개를 자바 클래스로 그린 설계도

@Data
@Table(name = "board_tb")
@Entity
@NoArgsConstructor
@AllArgsConstructor
// @Builder
public class Board {

    // 기본키
    @Id
    // 기본키 값을 자동으로 생성(IDENTITY) -> AUTO_INCREMENT 기능 사용
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 별도 어노테이션이 없을 경우 자바의 필드명이 컬럼명이 된다.
    private String title;
    private String content;

    // 데이터를 가지고 오는 전략 중 LAZY를 활용
    // Board(N) : User(1)
    // 사용자 1명은 여러 게시글을 작성할 수 있다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // board_tb() 에 만들어질 외래키 컬럼 이름 설정
    private User user;

    // 생성자 설계(비즈니스 로직)
    // id값과 createdAt은 만들지 않아도 됨 -> JPA에서 자동으로 설정해주므로 매개변수에서 제외
    @Builder // 의도치 않은 데이터 조작 방지
    public Board(String title, String content, User user) {
        this.title = title;
        this.content = content;
        this.user = user;
    }

    // 자신의 상태값을 변경하는 메서드 추가 - 객체 지향 설계
    // 영속성 엔티티를 수정하는 메서드
    public void update(BoardRequest.UpdateDto updateDto) {
        //비즈니스 규칙 검증
        updateDto.validate();
        //영속 상태에 있는 엔티티의 필드값을 여기서 변경함
        this.title = updateDto.getTitle();
        this.content = updateDto.getContent();
        // 변경 방지(Dirty Checking) 동작 과정
        // 1. 영속성 콘텍스트가 엔티티 최초 상태를 스냅샥으로 따로 보관
        // 2. 필드값 변경 시 현재 시점 상태를 스냅샷으로 따로 보과
        // 3. 트랜잭션 커밋 시점에 변경된 필드만 UPDATE 쿼리를 자동 생성
        // 3. UPDATE board_tb SET title = ?, content = ?, where id = ?
    }

    public boolean isOwner(Long userId) {
        // 게시글 수정/삭제 권한 체크용 편의 메서드
        // 게시들을 작성한 유저와 사용자 유저가 같은가?
        return this.user.getId().equals(userId);
    }

    //now()
    @CreationTimestamp
    private Timestamp createdAt;  //sql에서는 created_at 칼럼으로 생성됨

    // 시간을 포맷팅하는 메서드 구현
    // 객체가 생성된 시점
    public String getTime() {
        return MyDateUtil.timestampFormat(createdAt);
    }




}
