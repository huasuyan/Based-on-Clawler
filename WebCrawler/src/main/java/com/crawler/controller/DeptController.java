package com.crawler.controller;


import com.crawler.entity.Dept;
import com.crawler.entity.Result;
import com.crawler.entity.User;
import com.crawler.service.DeptService;
import com.crawler.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/dept")
public class DeptController {

    @Autowired
    private DeptService deptService;
    @Autowired
    private UserService userService;

    @GetMapping("/tree")
    public Result getDeptTree(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        return deptService.getDeptTree(currentUser.getDeptId());
    }

    /**
     * 新增顶级部门
     */
    @PostMapping("/addTop")
    public Result addTopDept(@RequestBody Dept dept) {
        deptService.addTopDept(dept);
        return Result.success("新增成功");
    }

    /**
     * 新增下级部门
     */
    @PostMapping("/addChild")
    public Result addChildDept(@RequestBody Dept dept) {
        deptService.addChildDept(dept);
        return Result.success("新增成功");
    }

    /**
     * 编辑部门信息
     */
    @PostMapping("/update")
    public Result updateDept(@RequestBody Dept dept) {
        deptService.updateDept(dept);
        return Result.success("更新成功");
    }



    @GetMapping("/delete/{deptId}")
    public Result deleteDept(@PathVariable Long deptId) {
        log.info("删除部门：{}", deptId);
        deptService.deleteDept(deptId);
        return Result.success("删除成功");
    }

    // -------------------------- 新增：部门搜索过滤 --------------------------
    @GetMapping("/searchTree")
    public Result searchDeptTree(@RequestParam(required = false) String searchName) {
        return deptService.searchDeptTree(searchName);
    }

}
