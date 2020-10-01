package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    @NotEmpty
    private String name;

    @Embedded
    private Address address;

    @JsonIgnore     // 양방향 연관관계가 있을 때 한쪽은 JsonIgnore 해야 순환참조, 무한루프에 빠지지 않는다.
    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();
}
