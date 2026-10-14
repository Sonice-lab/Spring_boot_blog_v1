package com.sonicelab_v1.spring_boot_practice.reply;


import com.sonicelab_v1.spring_boot_practice.board.Board;
import com.sonicelab_v1.spring_boot_practice.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

/**
 * 댓글 엔티티 -> 단방향 연관관계로 구축 예정
 * 게시판과 유저와의 연관관계 성립
 * 1. 하나의 게시글에는 여러 개의 댓글이 달릴 수 있다. -> N(댓글):1(게시글)
 * 2. 유저는 여러개의 댓글을 달 수 있다. -> N(댓글):1(유저)
 */

@Getter // 다른 곳에서 필드값을 가지고 갈 수 있다.
@NoArgsConstructor
@Table(name = "reply_tb")
@Entity
public class Reply {

    //외부에서 사용하기 위한 생성자 만들기
    // 자바에서는 이러한 기능을 생성자 오버로딩이라고 한다.
    // id와 createdAt은 자동으로 채워지므로 빌드에서 제외
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //auto_increment 할당을 위함
    private Long id;
    @Column(length = 500,  nullable = false) // 길이, not null 설정
    private String comment;

    //pc -> db
    @CreationTimestamp
    private Timestamp createAt;

    // 단방향 N:1 관계 - 외래키 user_id를 가짐
    @ManyToOne(fetch = FetchType.LAZY)  //관계차수 셋팅 -> 필요할 때 데이터를 가지고 오겠다.
    @JoinColumn(name = "user_id", nullable = false) //컬럼 이름 명명
    private User user; // 외래키는 객체로 표현

    // 단방향 N:1 관계 - 외래키 board_id 를 가짐
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    // 편의메서드 선언 - 댓글 작성자가 본인인지 확인
    public boolean isOwner(Long userId) {
        if(userId == null) {
            return false;
        }
        return this.user.getId().equals(userId); // true면 내 댓글, false 면 내 댓글이 아님
    }

}
