package com.runningcity.boutique.dto;

import com.runningcity.boutique.enums.DrawType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GachaRequest {

    private DrawType drawType;
}