package com.blog.controller;

import com.blog.common.Result;
import com.blog.entity.User;
import com.blog.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/users")

public class UserController {

    @Resource
    private UserService userService;

    // ====================== 权限检查 ======================
    private boolean isAdmin(HttpServletRequest request) {
        return "admin".equals(request.getAttribute("role"));
    }

    // ====================== 查询（所有登录用户可用） ======================
    @GetMapping
    public Result<List<User>> list() {
        return Result.success(userService.list());
    }

    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        return Result.success(user);
    }

    // ====================== 编辑用户（仅管理员，不允许改用户名） ======================
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody User user, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Result.fail("无权限，仅管理员可操作");
        }
        user.setId(id);
        user.setUsername(null); // 不允许修改用户名
        user.setPassword(null); // 不允许修改密码
        boolean b = userService.updateById(user);
        return b ? Result.success(null) : Result.fail("修改失败");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return Result.fail("无权限，仅管理员可操作");
        }
        boolean b = userService.removeById(id);
        return b ? Result.success(null) : Result.fail("删除失败");
    }
}
