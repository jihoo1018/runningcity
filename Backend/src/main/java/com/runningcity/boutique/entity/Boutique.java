package com.runningcity.boutique.entity;

import com.runningcity.boutique.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "boutique_items",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"category", "subcategory", "style", "color", "asset_key"}
        )
)
public class Boutique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    // ✅ 카테고리 (대분류)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ItemCategory category; // bodies, clothes, hair, head

    // ✅ 서브카테고리 (중분류)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SubCategory subcategory; // male, longsleeve, afro, eyes 등

    // ✅ 스타일 (소분류) - NULL 허용
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private Style style; // thick, thin, anger, blush 등

    @Column(length = 50)
    private String color; // black, blonde, red 등

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "asset_key", nullable = false, length = 100)
    private String assetKey;

    @Column(name = "base_path", nullable = false, length = 300)
    private String basePath;

    // ✅ 희귀도
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RarityType rarity; // common, rare, epic, legendary

    // ✅ 가격
    @Column(name = "price_cr", nullable = false)
    private Integer priceCr;

    // ✅ 획득 방법
    @Enumerated(EnumType.STRING)
    @Column(name = "obtain_method", length = 20)
    private ObtainMethod obtainMethod; // gacha, store

    // ✅ 생성·수정 시간
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;

    // ==========================================
    // 🎨 비즈니스 메서드
    // ==========================================

    /**
     * 애니메이션별 전체 경로 반환
     * basePath에 {animation} 패턴이 있으면 치환
     */
    public String getAnimationPath(String animation) {
        if (basePath != null && basePath.contains("{animation}")) {
            return basePath.replace("{animation}", animation);
        }
        return basePath;
    }

    /**
     * 모든 애니메이션 경로 반환 (walk, run, jump)
     * bodies, clothes 카테고리만 해당
     */
    public java.util.Map<String, String> getAllAnimationPaths() {
        java.util.Map<String, String> paths = new java.util.HashMap<>();

        if (category == ItemCategory.bodies || category == ItemCategory.clothes) {
            paths.put("walk", getAnimationPath("walk"));
            paths.put("run", getAnimationPath("run"));
            paths.put("jump", getAnimationPath("jump"));
        } else {
            paths.put("default", basePath);
        }

        return paths;
    }


}