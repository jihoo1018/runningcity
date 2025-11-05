package com.runningcity.model
import com.google.gson.annotations.SerializedName

//전부 템플릿, 대문자들은 교체해야하는것들

// 서버가 JSON 객체 하나를 줄 때
data class YOUR_RESPONSE_NAME(
    @SerializedName("JSON_KEY_1")
    val FIELD_NAME_1: FIELD_TYPE_1,

    @SerializedName("JSON_KEY_2")
    val FIELD_NAME_2: FIELD_TYPE_2,

    // 필요하면 계속 추가
    // @SerializedName("JSON_KEY_3")
    // val FIELD_NAME_3: FIELD_TYPE_3,

    // 예시
    // YOUR_RESPONSE_NAME → UserResponse
    // JSON_KEY_1 → "id"
    // FIELD_NAME_1 → id
    // FIELD_TYPE_1 → Long
)
//리스트(배열) 받을 때 템플릿

data class YOUR_LIST_RESPONSE(
    @SerializedName("JSON_ARRAY_KEY")
    val ITEMS: List<ITEM_TYPE>
)

// 배열 안 개별 아이템 모양
data class ITEM_TYPE(
    @SerializedName("JSON_KEY")
    val FIELD_NAME: FIELD_TYPE
)

//POST로 보낼때
data class YOUR_REQUEST_NAME(
    @SerializedName("JSON_KEY_1")
    val FIELD_NAME_1: FIELD_TYPE_1,

    @SerializedName("JSON_KEY_2")
    val FIELD_NAME_2: FIELD_TYPE_2,
    
    //예) GPS 기록 업로드면
    // YOUR_REQUEST_NAME → RunningUploadRequest
    // JSON_KEY_1 → "lat"
    // FIELD_NAME_1 → lat
    // FIELD_TYPE_1 → Double
)



