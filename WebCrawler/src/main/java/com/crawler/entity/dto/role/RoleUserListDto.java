package com.crawler.entity.dto.role;

import lombok.Data;
import java.util.Date;

@Data
public class RoleUserListDto {
    private Long   userId;
    private String username;
    private String phone;
    private String deptName;
    private Integer status;
    private Date   createTime;
}