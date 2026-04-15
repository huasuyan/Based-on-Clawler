package com.crawler.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeLoginDto {
    private String phone;
    private String uuid;
    private String code;
}
