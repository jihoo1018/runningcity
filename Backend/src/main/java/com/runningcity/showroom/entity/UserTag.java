package com.runningcity.showroom.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "user_tags",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_tag",
                columnNames = {"user_id", "tag_name"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(name = "tag_name", length = 50)
    private String tagName;

    // 정적 팩터리 메서드
    public static UserTag create(Long userId, String tagName) {
        UserTag tag = new UserTag();
        tag.userId = userId;
        tag.tagName = tagName;
        return tag;
    }
}
