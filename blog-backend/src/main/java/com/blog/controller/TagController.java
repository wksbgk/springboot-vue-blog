package com.blog.controller;

import com.blog.common.Result;
import com.blog.entity.Tag;
import com.blog.service.TagService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController

@RequestMapping("/api/tag")
public class TagController {

    @Resource
    private TagService tagService;

    private boolean isAdmin(HttpServletRequest request) {
        return "admin".equals(request.getAttribute("role"));
    }

    // 列表（公开）
    @GetMapping
    public Result list() {
        List<Tag> list = tagService.list();
        return Result.success(list);
    }

    // 新增（仅管理员）
    @PostMapping
    public Result add(@RequestBody Tag tag, HttpServletRequest request) {
        if (!isAdmin(request)) return Result.fail("无权限，仅管理员可操作");
        tagService.save(tag);
        return Result.success("添加成功");
    }

    // 修改（仅管理员）
    @PutMapping
    public Result update(@RequestBody Tag tag, HttpServletRequest request) {
        if (!isAdmin(request)) return Result.fail("无权限，仅管理员可操作");
        tagService.updateById(tag);
        return Result.success("修改成功");
    }

    // 删除（仅管理员）
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdmin(request)) return Result.fail("无权限，仅管理员可操作");
        tagService.removeById(id);
        return Result.success("删除成功");
    }
}
