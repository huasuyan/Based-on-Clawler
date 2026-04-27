package com.crawler.controller;


import com.crawler.annotation.RequirePermission;
import com.crawler.entity.Result;
import com.crawler.entity.User;
import com.crawler.entity.dto.UserAddDto;
import com.crawler.entity.dto.UserUpdateDto;
import com.crawler.service.DeptUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/deptUser")
public class DeptUserController {

    @Autowired
    private DeptUserService deptUserService;

    @GetMapping("/list")
    @RequirePermission(module = "dept_user", action = "dept_user_select")
    public Result pageList(HttpServletRequest request,
                           @RequestParam(defaultValue = "1") Integer pageNum,
                           @RequestParam(defaultValue = "10") Integer pageSize,
                           @RequestParam(required = false) Long deptId,
                           @RequestParam(required = false) String username,
                           @RequestParam(required = false) Integer status) {

        // 从request获取当前登录用户ID（简化，实际从token中获取）
        User currentUser = (User) request.getAttribute("currentUser");
        Long currentDeptId = currentUser.getDeptId();
        return Result.success(deptUserService.pageList(currentDeptId, pageNum, pageSize, deptId, username, status));
    }

    @PostMapping("/add")
    @RequirePermission(module = "dept_user", action = "dept_user_insert")
    public Result addUser(@RequestBody UserAddDto dto) {
        deptUserService.addUser(dto);
        return Result.success("用户添加成功");
    }

    @PostMapping("/update")
    @RequirePermission(module = "dept_user", action = "dept_user_update")
    public Result updateUser(@RequestBody UserUpdateDto userUpdateDto) {

        try {
            deptUserService.updateUser(userUpdateDto);
            return Result.success("用户信息修改成功");
        } catch (Exception e) {
            return Result.error("用户信息修改失败：" + e.getMessage());
        }
    }


    @GetMapping("/delete")
    @RequirePermission(module = "dept_user", action = "dept_user_delete")
    public Result deleteUser(HttpServletRequest request, @RequestParam Long userId) {
        // 从request获取当前登录用户ID（简化，实际从token中获取）
        User currentUser = (User) request.getAttribute("currentUser");
        Long currentUserId = currentUser.getUserId();
        deptUserService.deleteUser(userId, currentUserId);
        return Result.success("用户删除成功");
    }

    @PostMapping("/batchDelete")
    @RequirePermission(module = "dept_user", action = "dept_user_delete")
    public Result batchDelete(HttpServletRequest request, @RequestBody List<Long> userIds) {
        // 从request获取当前登录用户ID（简化，实际从token中获取）
        User currentUser = (User) request.getAttribute("currentUser");
        Long currentUserId = currentUser.getUserId();
        deptUserService.batchDelete(userIds, currentUserId);
        return Result.success("用户批量删除成功");
    }

    @GetMapping("/detail")
    @RequirePermission(module = "dept_user", action = "dept_user_select")
    public Result detail(@RequestParam Long userId) {
        return Result.success(deptUserService.detail(userId));
    }
}
