package com.crawler.entity.dto;

import com.crawler.entity.User;
import lombok.Data;

import java.util.List;

@Data
public class UserPageDto {
    private Long total;
    private Long pages;
    private Integer pageNum;
    private Integer pageSize;
    private List<User> list;
}