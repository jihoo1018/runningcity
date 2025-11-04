package com.runningcity.run.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadPointsResponse {
    private Integer ackedUntilSeq;
    private Integer maxInsertedSeq;
}