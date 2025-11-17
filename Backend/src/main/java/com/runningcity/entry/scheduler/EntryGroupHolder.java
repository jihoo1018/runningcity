package com.runningcity.entry.scheduler;

import org.springframework.stereotype.Component;

/**
 * 현재 활성화된 Entry 그룹을 메모리에 저장 (0~2 순환 - 지금은 3개뿐이므로)
 * - 서버 재시작 시 기본값은 1
 * - 매일 자정 스케줄러에 의해 갱신됨
 */
@Component
public class EntryGroupHolder {

    private static final Long MAX_GROUP = 3L;
    private Long currentGroup = 1L;

    public synchronized Long getCurrentGroup() {
        return currentGroup;
    }

    public synchronized void rotateGroup() {
        currentGroup = (++currentGroup % MAX_GROUP);
    }
}
