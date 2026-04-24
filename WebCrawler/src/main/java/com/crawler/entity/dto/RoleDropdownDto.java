package com.crawler.entity.dto;

import com.crawler.entity.Role;
import lombok.Data;

@Data
public class RoleDropdownDto {
    private Long   id;
    private String name;

    public RoleDropdownDto(Role role) {
        this.id   = role.getRoleId();
        this.name = role.getRoleName();
    }
}