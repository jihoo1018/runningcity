package com.runningcity.entry.controller;

import com.runningcity.entry.dto.EntryDetailResponse;
import com.runningcity.entry.dto.EntryListResponse;
import com.runningcity.entry.dto.EntrySearchRequest;
import com.runningcity.entry.entity.Entry;
import com.runningcity.entry.exception.EntryException;
import com.runningcity.entry.service.EntryService;
import com.runningcity.global.exception.BaseException;
import com.runningcity.global.response.ApiResponse;
import com.runningcity.global.response.CommonResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/entry")
@RequiredArgsConstructor
public class EntryController {
    private final EntryService entryService;

    /**
     * 잠입 기지 리스트 조회 API
     * @param
     * @return
     */
    @GetMapping("/list/{groupNo}")
    public ApiResponse<List<EntryListResponse>> getEntryListByGroupNo(@PathVariable Long groupNo) {
        List<EntryListResponse> list = entryService.getEntryListByGroupNo(groupNo);
        return ApiResponse.success(CommonResponseCode.ENTRY_GET_LIST_SUCCESS, list);
    }

    /**
     * 잠입 기지 단일 조회 API
     * @param
     * @return
     */
    @GetMapping("/{baseId}")
    public ApiResponse<EntryDetailResponse> getEntryDetail(@PathVariable Long baseId) {
        EntryDetailResponse entryRes = entryService.getEntryById(baseId);
        return ApiResponse.success(CommonResponseCode.ENTRY_GET_SUCCESS, entryRes);
    }

}
