package com.runningcity.boutique.entity;
import com.runningcity.boutique.enums.ItemCategory;
import com.runningcity.boutique.enums.ObtainMethod;
import com.runningcity.boutique.enums.RarityType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "boutique_items",
        uniqueConstraints = @UniqueConstraint(columnNames = {"category", "asset_key"}))
public class Boutique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    // ✅ ENUM 매핑 (문자열 그대로 저장)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ItemCategory category;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "asset_key", nullable = false, length = 100)
    private String assetKey;

    @Column(nullable = false, length = 200)
    private String path;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RarityType rarity; // common / rare / epic / legendary

    @Column(name = "price_cr", nullable = false)
    private Integer priceCr;

    @Column(name = "is_gacha_only")
    private Boolean isGachaOnly;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ObtainMethod obtainMethod; // gacha / store

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;

}
