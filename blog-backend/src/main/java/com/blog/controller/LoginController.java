package com.blog.controller;

import com.blog.common.Result;
import com.blog.dto.LoginDTO;
import com.blog.entity.User;
import com.blog.service.UserService;
import com.blog.util.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    // ====================== 登录 ======================
    @PostMapping("/login")
    public Result login(@Valid @RequestBody LoginDTO dto) {

        // 1. 根据用户名查用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        User user = userService.getOne(wrapper);

        if (user == null) {
            return Result.fail("用户名不存在");
        }

        // 2. 明文密码对比
        if (!dto.getPassword().equals(user.getPassword())) {
            return Result.fail("密码错误");
        }

        // 3. 生成 JWT，嵌入角色
        String token = jwtUtil.createToken(user.getId(), user.getRole(), user.getUsername());

        // 4. 返回 token + 用户信息
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("role", user.getRole());

        return Result.success(data);
    }

    // ====================== 注册 ======================
    @PostMapping("/register")
    public Result register(@Valid @RequestBody LoginDTO dto) {

        // 1. 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        User exist = userService.getOne(wrapper);

        if (exist != null) {
            return Result.fail("用户名已被占用");
        }

        // 2. 创建用户（默认普通用户）
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setRole("user");
        userService.save(user);

        return Result.success(null);
    }
}
