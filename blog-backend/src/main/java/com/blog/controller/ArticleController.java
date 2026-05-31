package com.blog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.Result;
import com.blog.entity.Article;
import com.blog.service.ArticleService;
import com.blog.util.RedisUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/article")
public class ArticleController {

    @Resource
    private ArticleService articleService;

    @Resource
    private RedisUtil redisUtil;

    private static final String VIEW_PREFIX = "article:view:";

    private boolean isAdmin(HttpServletRequest request) {
        return "admin".equals(request.getAttribute("role"));
    }

    // ====================== 分页列表（公开） ======================
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "5") Integer pageSize) {
        IPage<Article> page = articleService.page(new Page<>(pageNum, pageSize));
        mergeViewCounts(page.getRecords());

        Map<String, Object> data = new HashMap<>();
        data.put("records", page.getRecords());
        data.put("total", page.getTotal());
        data.put("current", page.getCurrent());
        data.put("pages", page.getPages());
        return Result.success(data);
    }

    // ====================== 详情 + 浏览量（公开，Redis 计数） ======================
    @GetMapping("/{id}")
    public Result get(@PathVariable Long id) {
        // Redis 浏览量 +1
        try {
            redisUtil.increment(VIEW_PREFIX + id);
        } catch (Exception e) { log.warn("Redis操作失败", e); }

        Article article = articleService.getById(id);
        if (article != null) {
            // 合并 Redis 中的浏览量
            try {
                String v = redisUtil.get(VIEW_PREFIX + id);
                if (v != null) {
                    int base = article.getViewCount() != null ? article.getViewCount() : 0;
                    article.setViewCount(base + Integer.parseInt(v));
                }
            } catch (Exception e) { log.warn("Redis操作失败", e); }
        }
        return Result.success(article);
    }

    // ====================== 发布（仅管理员） ======================
    @PostMapping
    public Result add(@RequestBody Article article, HttpServletRequest request) {
        if (!isAdmin(request)) return Result.fail("无权限，仅管理员可操作");
        Long userId = (Long) request.getAttribute("userId");
        article.setUserId(userId);
        articleService.save(article);
        return Result.success("发布成功");
    }

    // ====================== 修改（仅管理员） ======================
    @PutMapping
    public Result update(@RequestBody Article article, HttpServletRequest request) {
        if (!isAdmin(request)) return Result.fail("无权限，仅管理员可操作");
        articleService.updateById(article);
        return Result.success("修改成功");
    }

    // ====================== 删除（仅管理员） ======================
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdmin(request)) return Result.fail("无权限，仅管理员可操作");
        articleService.removeById(id);
        return Result.success("删除成功");
    }

    // 合并 Redis 浏览量
    private void mergeViewCounts(List<Article> list) {
        for (Article a : list) {
            try {
                String v = redisUtil.get(VIEW_PREFIX + a.getId());
                if (v != null) {
                    int base = a.getViewCount() != null ? a.getViewCount() : 0;
                    a.setViewCount(base + Integer.parseInt(v));
                }
            } catch (Exception e) { log.warn("Redis操作失败", e); }
        }
    }
}
