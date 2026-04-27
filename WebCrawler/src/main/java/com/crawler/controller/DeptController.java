package com.crawler.controller;


import com.crawler.annotation.RequirePermission;
import com.crawler.entity.Dept;
import com.crawler.entity.Result;
import com.crawler.entity.Role;
import com.crawler.entity.User;
import com.crawler.mapper.DeptMapper;
import com.crawler.mapper.RoleMapper;
import com.crawler.mapper.UserRoleMapper;
import com.crawler.service.DeptService;
import com.crawler.service.DeptUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/dept")
public class DeptController {

    @Autowired
    private DeptService deptService;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private UserRoleMapper userRoleMapper;
    @Autowired
    private DeptMapper deptMapper;

    @GetMapping("/tree")
    public Result getDeptTree(HttpServletRequest request,@RequestParam Integer showEnable) {
        User currentUser = (User) request.getAttribute("currentUser");

        // TODO 权限
        // 根据用户权限设置top部门ID
        Long topDeptId = currentUser.getDeptId();
        Long roleId = userRoleMapper.selectRoleIdByUserId(currentUser.getUserId());
        Role role = roleMapper.selectById(roleId);
        Integer dataScope = role.getDataScope();
        if (dataScope == 1) {
            topDeptId = currentUser.getDeptId();
        }else if (dataScope == 2) {
            topDeptId = deptMapper.getTopLevelParentId(currentUser.getDeptId());
        }else if (dataScope == 3) {
            topDeptId = 0L;
        }else{
            throw new RuntimeException("未知的数据权限范围！");
        }


        if(deptService.getDeptTree(topDeptId, showEnable) == null) {
            return Result.error("部门树不存在");
        }

        return deptService.getDeptTree(topDeptId, showEnable);
    }

    /**
     * 新增顶级部门
     */
    @PostMapping("/addTop")
    @RequirePermission(module = "dept", action = "dept_insert")
    public Result addTopDept(@RequestBody Dept dept) {
        deptService.addTopDept(dept);
        return Result.success("新增成功");
    }

    /**
     * 新增下级部门
     */
    @PostMapping("/addChild")
    @RequirePermission(module = "dept", action = "dept_insert")
    public Result addChildDept(@RequestBody Dept dept) {
        deptService.addChildDept(dept);
        return Result.success("新增成功");
    }

    /**
     * 编辑部门信息
     */
    @PostMapping("/update")
    @RequirePermission(module = "dept", action = "dept_update")
    public Result updateDept(@RequestBody Dept dept) {
        deptService.updateDept(dept);
        return Result.success("更新成功");
    }



    @GetMapping("/delete")
    @RequirePermission(module = "dept", action = "dept_delete")
    public Result deleteDept(@RequestParam Long deptId) {
        log.info("删除部门：{}", deptId);
        deptService.deleteDept(deptId);
        return Result.success("删除成功");
    }

    // -------------------------- 新增：部门搜索过滤 --------------------------
    @GetMapping("/searchTree")
    public Result searchDeptTree(HttpServletRequest request,@RequestParam(required = false) String searchName, @RequestParam(required = false) Integer showEnable) {
        User currentUser = (User) request.getAttribute("currentUser");
        if(currentUser == null) {
            return Result.error("用户未登录");
        }
        return deptService.searchDeptTree(currentUser.getDeptId(), searchName, showEnable);
    }

}
