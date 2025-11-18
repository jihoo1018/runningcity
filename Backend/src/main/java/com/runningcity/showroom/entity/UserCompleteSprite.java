package com.runningcity.showroom.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "user_complete_sprites")
@Getter
@NoArgsConstructor
public class UserCompleteSprite {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sprite_id")
    private Long spriteId;
    
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;
    
    @Column(name = "sprite_name", nullable = false, length = 100)
    private String spriteName;
    
    @Column(name = "sprite_path", nullable = false, length = 300)
    private String spritePath;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}