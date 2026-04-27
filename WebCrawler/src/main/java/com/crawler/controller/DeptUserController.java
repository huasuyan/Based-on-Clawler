package com.crawler.controller;


import com.crawler.annotation.RequirePermission;
import com.crawler.entity.Result;
import com.crawler.entity.User;
import com.crawler.entity.dto.UserAddDto;
import com.crawler.entity.dto.UserUpdateDto;
import com.crawler.service.DeptUserService;
import com.crawler.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/deptUser")
public class DeptUserController {

    @Autowired
    private DeptUserService deptUserService;
    @Autowired
    private UserService userService;

    @GetMapping("/list")
    @RequirePermission(module = "dept_user", action = "dept_user_select")
    public Result pageList(HttpServletRequest request,
                           @RequestParam(defaultValue = "1") Integer pageNum,
                           @RequestParam(defaultValue = "10") Integer pageSize,
                           @RequestParam(required = false) Long deptId,
                           @RequestParam(required = false) String username,
                           @RequestParam(required = false) Integer status) {

        // TODO 需要测试
        User currentUser = (User) request.getAttribute("currentUser");
        // 校验访问deptId是否在用户权限范围内
        if (userService.checkUserVisitDept(currentUser, deptId)) {
            return Result.success(deptUserService.pageList(pageNum, pageSize, deptId, username, status));
        }else{
            return Result.permissionError("您没有权限查询该部门人员！");
        }


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
