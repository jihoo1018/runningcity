package com.runningcity.run.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinishResponse {

    private String status;               // "FINALIZED" | "CLOSING"

    private Instant finalizedAt;         // FINALIZED만 값

    private Instant closingDeadline;     // CLOSING만 값
    private Integer serverAckedUntil;    // CLOSING 때 힌트

}
