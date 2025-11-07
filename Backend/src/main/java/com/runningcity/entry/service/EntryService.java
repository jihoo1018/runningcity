package com.runningcity.entry.service;

import com.runningcity.entry.dto.EntryDetailResponse;
import com.runningcity.entry.dto.EntryListResponse;
import com.runningcity.entry.entity.Entry;
import com.runningcity.entry.repository.EntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EntryService {
    private final EntryRepository entryRepository;

    /** groupNo 기준 목록 조회 */
    public List<EntryListResponse> getEntryListByGroupNo(Long groupNo) {
        return entryRepository.findByGroupNo(groupNo)
                .stream()
                .map(EntryListResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /** baseId 단일 조회 */
    public EntryDetailResponse getEntryById(Long baseId) {
        Entry entry = entryRepository.findById(baseId)
                .orElseThrow(() -> new IllegalArgumentException("baseId '" + baseId +"'에 해당하는 잠입 기지가 존재하지 않습니다."));
        return EntryDetailResponse.fromEntity(entry);
    }
}
