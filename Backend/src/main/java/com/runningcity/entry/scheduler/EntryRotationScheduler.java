package com.runningcity.entry.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 매일 자정마다 Entry 그룹 번호를 순환시키는 스케줄러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EntryRotationScheduler {

    private final EntryGroupHolder entryGroupHolder;

    // 매일 자정 00:00 실행
    @Scheduled(cron = "0 0 0 * * *")
    public void rotateDailyEntryGroup() {
        entryGroupHolder.rotateGroup();
        log.info("Entry 그룹이 순환되었습니다. 현재 그룹: {}", entryGroupHolder.getCurrentGroup());
    }
}
