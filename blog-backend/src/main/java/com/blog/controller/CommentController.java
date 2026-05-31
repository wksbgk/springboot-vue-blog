package com.blog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.Result;
import com.blog.entity.Comment;
import com.blog.service.CommentService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController

@RequestMapping("/api/comment")
public class CommentController {

    @Resource
    private CommentService commentService;

    private boolean isAdmin(HttpServletRequest request) {
        return "admin".equals(request.getAttribute("role"));
    }

    // 根据文章ID查评论（公开）
    @GetMapping("/article/{articleId}")
    public Result list(@PathVariable Long articleId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getArticleId, articleId);
        List<Comment> list = commentService.list(wrapper);
        return Result.success(list);
    }

    // 发布评论（需登录）
    @PostMapping
    public Result add(@RequestBody Comment comment, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String username = (String) request.getAttribute("username");
        if (userId == null) return Result.fail("请先登录");
        comment.setUserId(userId);
        comment.setUsername(username);
        commentService.save(comment);
        return Result.success(comment);
    }

    // 删除评论（本人或管理员）
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id, HttpServletRequest request) {
        Comment comment = commentService.getById(id);
        if (comment == null) return Result.fail("评论不存在");
        Long userId = (Long) request.getAttribute("userId");
        if (!isAdmin(request) && !comment.getUserId().equals(userId)) {
            return Result.fail("无权限，只能删除自己的评论");
        }
        commentService.removeById(id);
        return Result.success("删除成功");
    }
}
