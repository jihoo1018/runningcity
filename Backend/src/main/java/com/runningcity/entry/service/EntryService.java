package com.runningcity.entry.service;

import com.runningcity.entry.dto.EntryDetailResponse;
import com.runningcity.entry.dto.EntryListResponse;
import com.runningcity.entry.entity.Entry;
import com.runningcity.entry.repository.EntryRepository;
import com.runningcity.entry.scheduler.EntryGroupHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EntryService {

    private final EntryRepository entryRepository;
    private final EntryGroupHolder entryGroupHolder;

    /** 오늘 활성화 잠입 기지 목록 조회 */
    public List<EntryListResponse> getTodayEntryList() {
        Long currentGroup = entryGroupHolder.getCurrentGroup();
        return this.getEntryListByGroupNo(currentGroup);
    }

    /** groupNo 기준 목록 조회 */
    public List<EntryListResponse> getEntryListByGroupNo(Long groupNo) {
        return entryRepository.findByGroupNo(groupNo)
                .stream()
                .map(EntryListResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /** 전체 목록 조회 */
    public List<EntryListResponse> getAllEntryList() {
        return entryRepository.findAll()
                .stream()
                .map(EntryListResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /** groupNo 기준으로 묶은 전체 목록 조회 */
    public Map<Integer, List<EntryListResponse>> getAllGroupedByGroupNo() {
        List<Entry> allEntries = entryRepository.findAll();
        return allEntries.stream()
                .collect(Collectors.groupingBy(
                        Entry::getGroupNo,
                        Collectors.mapping(EntryListResponse::fromEntity, Collectors.toList())
                ));
    }

    /** baseId 단일 조회 */
    public EntryDetailResponse getEntryByBaseId(Long baseId) {
        Entry entry = entryRepository.findByBaseId(baseId)
                .orElseThrow(() -> new IllegalArgumentException("baseId '" + baseId +"'에 해당하는 잠입 기지가 존재하지 않습니다."));
        return EntryDetailResponse.fromEntity(entry);
    }
}
