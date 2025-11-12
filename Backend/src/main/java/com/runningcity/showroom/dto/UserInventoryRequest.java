package com.runningcity.showroom.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserInventoryRequest {

    private Long itemId;
    private Integer quantity;

}
