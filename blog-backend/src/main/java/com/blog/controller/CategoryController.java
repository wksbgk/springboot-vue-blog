package com.blog.controller;

import com.blog.common.Result;
import com.blog.entity.Category;
import com.blog.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController

@RequestMapping("/api/category")
public class CategoryController {

    @Resource
    private CategoryService categoryService;

    private boolean isAdmin(HttpServletRequest request) {
        return "admin".equals(request.getAttribute("role"));
    }

    // 列表（公开）
    @GetMapping
    public Result list() {
        List<Category> list = categoryService.list();
        return Result.success(list);
    }

    // 新增（仅管理员）
    @PostMapping
    public Result add(@RequestBody Category category, HttpServletRequest request) {
        if (!isAdmin(request)) return Result.fail("无权限，仅管理员可操作");
        categoryService.save(category);
        return Result.success("添加成功");
    }

    // 修改（仅管理员）
    @PutMapping
    public Result update(@RequestBody Category category, HttpServletRequest request) {
        if (!isAdmin(request)) return Result.fail("无权限，仅管理员可操作");
        categoryService.updateById(category);
        return Result.success("修改成功");
    }

    // 删除（仅管理员）
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdmin(request)) return Result.fail("无权限，仅管理员可操作");
        categoryService.removeById(id);
        return Result.success("删除成功");
    }
}
