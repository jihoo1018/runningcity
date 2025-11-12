package com.runningcity.boutique.entity;


import com.runningcity.boutique.enums.RarityType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "boutique_rarity_rates")
public class BoutiqueRarityRate {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RarityType rarity;  // PK (Enum 매핑)

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal probability; // NUMERIC(5,2) 매핑

    @Column(columnDefinition = "TEXT")
    private String description;
}
