package com.crawler.service.impl;

import com.crawler.entity.Dept;
import com.crawler.entity.DeptTree;
import com.crawler.entity.Result;
import com.crawler.mapper.DeptMapper;
import com.crawler.service.DeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DeptServiceImpl implements DeptService {

    @Autowired
    private DeptMapper deptMapper;

    /**
     * 获取部门树形列表
     */
    @Override
    public Result getDeptTree(Long deptId, Integer showEnable) {

        List<Dept> allDepts;
        if(showEnable == 1) {
            allDepts = deptMapper.selectAll();
        }else {
            allDepts = deptMapper.selectAllActive();
        }
        List<DeptTree> tree = new ArrayList<>();

        if (deptId != null) {
            Dept targetDept = deptMapper.selectById(deptId);
            if (targetDept != null) {
                Long parentId = targetDept.getParentDeptId();
                if (parentId == 0) {
                    // 如果目标部门是根部门，直接构建完整树
                    tree = buildChildren(allDepts, 0L);
                } else {
                    // 找到父部门
                    Dept parentDept = deptMapper.selectById(parentId);
                    if (parentDept != null) {
                        // 为父部门创建树节点
                        DeptTree parentNode = new DeptTree();
                        parentNode.setDeptId(parentDept.getDeptId());
                        parentNode.setParentDeptId(parentDept.getParentDeptId());
                        parentNode.setDeptName(parentDept.getDeptName());
                        parentNode.setDeptLevel(parentDept.getDeptLevel());
                        parentNode.setStatus(parentDept.getStatus());
                        // 构建父部门的子节点（包含同级部门）
                        parentNode.setChildren(buildChildren(allDepts, parentId));
                        tree.add(parentNode);
                    }
                }
            }
        } else {
            // 未传入部门ID，构建完整树
            tree = buildChildren(allDepts, 0L);
        }

        return Result.success(tree);
    }
    // 构建子部门树（基于parentDeptId）
    private List<DeptTree> buildChildren(List<Dept> allDepts, Long parentId) {
        List<DeptTree> children = new ArrayList<>();

        for (Dept dept : allDepts) {
            if (dept.getParentDeptId().equals(parentId)) {
                DeptTree vo = new DeptTree();
                vo.setDeptId(dept.getDeptId());
                vo.setParentDeptId(dept.getParentDeptId());
                vo.setDeptName(dept.getDeptName());
                vo.setDeptLevel(dept.getDeptLevel());
                vo.setStatus(dept.getStatus());
                // 递归构建子节点
                vo.setChildren(buildChildren(allDepts, dept.getDeptId()));
                children.add(vo);
            }
        }
        return children;
    }
    /**
     * 新增顶级部门
     */
    @Override
    public void addTopDept(Dept dept) {
        // 校验重名（parent=0的顶级部门不能重名）
        Dept exist = deptMapper.selectByParentAndName(0L, dept.getDeptName());
        if (exist != null) {
            throw new IllegalArgumentException("部门名称已存在");
        }
        // 强制设置parentId=0，deptLevel=1
        dept.setParentDeptId(0L);
        dept.setDeptLevel(1);
        if (dept.getStatus() == null) {
            dept.setStatus(1);
        }
        dept.setCreateTime(new Date());
        dept.setUpdateTime(new Date());
        deptMapper.insert(dept);
    }


    /**
     * 新增下级部门
     */
    @Override
    public void addChildDept(Dept dept) {
        Long parentId = dept.getParentDeptId();
        // 1. 校验父部门是否存在
        Dept parentDept = deptMapper.selectById(parentId);
        if (parentDept == null) {
            throw new IllegalArgumentException("上级部门不存在");
        }
        // 2. 校验同一父节点下部门名称不重复
        Dept exist = deptMapper.selectByParentAndName(parentId, dept.getDeptName());
        if (exist != null) {
            throw new IllegalArgumentException("部门名称重复");
        }
        // 3. 层级自动+1，禁止跨级
        int newLevel = parentDept.getDeptLevel() + 1;
        if (dept.getDeptLevel() != null && !dept.getDeptLevel().equals(newLevel)) {
            throw new IllegalArgumentException("层级不能越级");
        }
        dept.setDeptLevel(newLevel);
        if (dept.getStatus() == null) {
            dept.setStatus(1);
        }
        dept.setCreateTime(new Date());
        dept.setUpdateTime(new Date());
        deptMapper.insert(dept);
    }


    /**
     * 编辑部门信息
     */
    @Override
    public void updateDept( Dept dept) {
        Long deptId = dept.getDeptId();
        // 1. 校验部门是否存在
        Dept existDept = deptMapper.selectById(deptId);
        if (existDept == null) {
            throw new IllegalArgumentException("部门不存在");
        }
        // 2. 校验同一父节点下部门名称不重复（排除自身）
        Dept sameNameDept = deptMapper.selectByParentAndName(existDept.getParentDeptId(), dept.getDeptName());
        if (sameNameDept != null && !sameNameDept.getDeptId().equals(deptId)) {
            throw new IllegalArgumentException("部门名称重复");
        }
        // 3. 不允许修改层级和上级部门
        dept.setParentDeptId(null);
        dept.setDeptLevel(null);
        dept.setUpdateTime(new Date());
        deptMapper.update(dept);
    }

    /**
     * 删除部门
     */
    @Transactional
    @Override
    public void deleteDept(Long deptId) {
        // 1. 校验部门是否存在
        Dept dept = deptMapper.selectById(deptId);
        if (dept == null) {
            throw new IllegalArgumentException("该部门不存在");
        }

        // 2. 校验是否存在子部门
        int childCount = deptMapper.countChildrenDept(deptId);
        if (childCount > 0) {
            throw new IllegalArgumentException("该部门下存在子部门，请先删除下级部门");
        }

        // 3. 校验是否存在关联用户
        int userCount = deptMapper.countUserByDeptId(deptId);
        if (userCount > 0) {
            throw new IllegalArgumentException("该部门下存在用户，无法删除");
        }

        // 4. 执行删除
        deptMapper.deleteById(deptId);
    }

    // 部门搜索过滤
    @Override
    public Result searchDeptTree(Long deptId, String searchName, Integer showEnable) {
        if (searchName == null || searchName.trim().isEmpty()) {
            // 搜索条件为空，返回完整树
            return getDeptTree(deptId, 1);
        }

        // 1. 获取所有部门
        List<Dept> allDepts;
        if (showEnable == 1) {
            allDepts = deptMapper.selectAll();
        } else {
            allDepts = deptMapper.selectAllActive();
        }
        // 2. 确定需要考虑的部门范围（同级和子部门）
        Set<Long> scopeDeptIds = new HashSet<>();
        if (deptId != null) {
            Dept targetDept = deptMapper.selectById(deptId);
            if (targetDept != null) {
                Long parentId = targetDept.getParentDeptId();
                // 添加同级部门（具有相同父部门的部门）
                for (Dept dept : allDepts) {
                    if (dept.getParentDeptId().equals(parentId)) {
                        scopeDeptIds.add(dept.getDeptId());
                    }
                }
                // 添加子部门（递归获取所有子部门）
                addAllChildren(allDepts, deptId, scopeDeptIds);
            }
        } else {
            // 未传入deptId，考虑所有部门
            for (Dept dept : allDepts) {
                scopeDeptIds.add(dept.getDeptId());
            }
        }
        // 3. 模糊查询所有部门
        List<Dept> allMatchedDepts = deptMapper.selectByDeptNameLike(searchName.trim());
        if (allMatchedDepts.isEmpty()) {
            return Result.error("未找到匹配的部门");
        }
        // 4. 过滤出在范围内的匹配部门
        List<Dept> matchedDepts = allMatchedDepts.stream()
                .filter(d -> scopeDeptIds.contains(d.getDeptId()))
                .collect(Collectors.toList());

        if (matchedDepts.isEmpty()) {
            return Result.error("未找到匹配的部门");
        }
        // 5. 收集所有匹配部门及其所有上级部门ID（保证路径完整）
        Set<Long> includeIds = new HashSet<>();
        for (Dept dept : matchedDepts) {
            includeIds.add(dept.getDeptId());
            // 递归查询所有上级部门ID
            List<Long> parentIds = deptMapper.selectParentIdsByDeptId(dept.getDeptId());
            includeIds.addAll(parentIds);
        }
        // 6. 查询所有需要保留的部门，构建过滤后的树
        List<Dept> filteredDepts = allDepts.stream()
                .filter(d -> includeIds.contains(d.getDeptId()))
                .collect(Collectors.toList());

        // 7. 构建树形结构
        List<DeptTree> tree = new ArrayList<>();
        if (deptId != null) {
            tree = buildChildren(filteredDepts, deptId);
        }

        return Result.success(tree);
    }
    // 递归添加所有子部门ID
    private void addAllChildren(List<Dept> allDepts, Long parentId, Set<Long> deptIds) {
        for (Dept dept : allDepts) {
            if (dept.getParentDeptId().equals(parentId)) {
                deptIds.add(dept.getDeptId());
                // 递归添加子部门
                addAllChildren(allDepts, dept.getDeptId(), deptIds);
            }
        }
    }

}
