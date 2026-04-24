package com.crawler.controller;

import com.crawler.entity.Result;
import com.crawler.entity.User;
import com.crawler.entity.dto.*;
import com.crawler.service.RoleService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/role")
public class RoleController {

    @Resource
    private RoleService roleService;

    /** 角色条件分页查询 */
    @PostMapping("/pageList")
    public Result pageList(@RequestBody RolePageQueryDto queryDto) {
        Map<String, Object> data = roleService.pageList(queryDto);
        return Result.success(data);
    }

    /** 新增角色 */
    @PostMapping("/add")
    public Result add(@RequestBody RoleCreateDto createDto) {
        Map<String, Object> data = roleService.add(createDto);
        return Result.success(data);
    }

    /** 编辑角色 */
    @PostMapping("/update")
    public Result update(@RequestBody RoleEditDto editDto) {
        Map<String, Object> data = roleService.update(editDto);
        return Result.success(data);
    }

    /** 角色详情 */
    @GetMapping("/detail")
    public Result detail(@RequestParam Long roleId) {
        Map<String, Object> data = roleService.detail(roleId);
        return Result.success(data);
    }

    /** 单条删除 */
    @GetMapping("/delete")
    public Result delete(@RequestParam Long roleId) {
        roleService.delete(roleId);
        return Result.success();
    }

    /** 批量删除 */
    @PostMapping("/batchDelete")
    public Result batchDelete(@RequestBody Map<String, List<Long>> body) {
        List<Long> roleIds = body.get("roleIds");
        roleService.batchDelete(roleIds);
        return Result.success();
    }

    /** 查看该角色下用户列表 */
    @PostMapping("/userList")
    public Result userList(@RequestBody RolePageQueryDto queryDto) {
        Map<String, Object> data = roleService.userList(
                queryDto.getRoleId(), queryDto.getPageNum(), queryDto.getPageSize());
        return Result.success(data);
    }

    /** 角色名称下拉列表 */
    @GetMapping("/dropdownList")
    public Result dropdownList() {
        List<RoleDropdownDto> data = roleService.dropdownList();
        return Result.success(data);
    }

    /** 查看当前用户所有权限 */
    @GetMapping("/authority")
    public Result authority(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        Map<String, Object> data = roleService.getAuthority(
                Long.valueOf(currentUser.getUserId()));
        return Result.success(data);
    }
}