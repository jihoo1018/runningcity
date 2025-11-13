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
import java.util.Map;

@RestController
@RequestMapping("/entry")
@RequiredArgsConstructor
public class EntryController {
    private final EntryService entryService;

    /**
     * 그룹별로 묶인 전체 잠입 기지 리스트 조회 API
     * @param
     * @return
     */
    @GetMapping("/list/all")
    public ApiResponse<Map<Integer, List<EntryListResponse>>> getAllEntryList() {
//        List<EntryListResponse> list = entryService.getAllEntryList();
        return ApiResponse.success(CommonResponseCode.ENTRY_GET_LIST_SUCCESS, entryService.getAllGroupedByGroupNo());
    }

    /**
     * 오늘 활성화 잠입 기지 리스트 조회 API
     * @param
     * @return
     */
    @GetMapping("/list/today")
    public ApiResponse<List<EntryListResponse>> getTodayEntryList() {
        List<EntryListResponse> list = entryService.getTodayEntryList();
        return ApiResponse.success(CommonResponseCode.ENTRY_GET_LIST_SUCCESS, list);
    }

    /**
     * 잠입 기지 리스트 조회 API
     * @param
     * @return
     */
    @GetMapping("/list/{groupNo}")
    public ApiResponse<List<EntryListResponse>> getEntryListByGroupNo(@PathVariable("groupNo") Long groupNo) {
        List<EntryListResponse> list = entryService.getEntryListByGroupNo(groupNo);
        return ApiResponse.success(CommonResponseCode.ENTRY_GET_LIST_SUCCESS, list);
    }

    /**
     * 잠입 기지 단일 조회 API
     * @param
     * @return
     */
    @GetMapping("/{baseId}")
    public ApiResponse<EntryDetailResponse> getEntryDetail(@PathVariable("baseId") Long baseId) {
        EntryDetailResponse entryRes = entryService.getEntryByBaseId(baseId);
        return ApiResponse.success(CommonResponseCode.ENTRY_GET_SUCCESS, entryRes);
    }

}
