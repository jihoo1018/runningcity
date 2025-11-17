package com.runningcity.showroom.dto;

import com.runningcity.showroom.entity.PrivacySetting;
import com.runningcity.showroom.entity.UserEquippedItem;
import com.runningcity.showroom.entity.UserTag;
import com.runningcity.user.dto.UserResponse;
import com.runningcity.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyOfficeResponse {
    private double  totalDist;
    private double  maxDist;
    private double  avgPace;
    private double  bestPace;
    private Long totalEntryCnt;
    private List<UserEquippedItemResponse> equippedItemList;

    public static MyOfficeResponse from(List<UserEquippedItemResponse> list) {
        return MyOfficeResponse.builder()
//                .totalDist(entity.totalDist())
//                .maxDist(entity.maxDist())
//                .avgPace(entity.avgPace())
//                .bestPace(entity.bestPace())
//                .totalEntryCnt(entity.totalEntryCnt())
                .equippedItemList(list)
                .build();
    }

    public static MyOfficeResponse create(double totalDist, double maxDist, double avgPace, double bestPace, Long totalEntryCnt, List<UserEquippedItemResponse> list) {
        return MyOfficeResponse.builder()
                .totalDist(totalDist)
                .maxDist(maxDist)
                .avgPace(avgPace)
                .bestPace(bestPace)
                .totalEntryCnt(totalEntryCnt)
                .equippedItemList(list)
                .build();
    }

}
