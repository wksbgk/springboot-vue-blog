# springboot-vue-blog

基于 Spring Boot 2.7 + Vue 3 + MyBatis-Plus + Redis + JWT 的前后端分离博客系统。

## 功能

- 用户注册/登录（JWT 认证）
- 角色权限（管理员/普通用户）
- 文章管理（发布/编辑/删除/分页/浏览量）
- 评论系统（发表/删除）
- 分类与标签管理
- Redis 缓存

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Spring Boot 2.7、MyBatis-Plus 3.5、Spring Security、JWT、Redis |
| 前端 | Vue 3、Vue Router、Axios、Vite |
| 数据库 | MySQL |

## 快速开始

### 本地开发

1. 创建 MySQL 数据库 `blog`，执行 `sql.txt`（建表 + 默认管理员）
2. 启动 Redis
3. 配置 `application-dev.yml` 中的数据库连接
4. 后端：`cd blog-backend && mvn spring-boot:run`
5. 前端：`cd blog-frontend && npm install && npm run dev`
6. 访问 `http://localhost:5173`
7. 默认管理员：`admin` / `123`

### Docker 部署

```bash
# 1. 打包
cd blog-backend && mvn clean package -DskipTests
cd ../blog-frontend && npm run build

# 2. 一键启动（MySQL + Redis + 后端 + 前端）
cd ..
docker compose up -d --build

# 3. 导入种子数据
docker exec -i blog-mysql mysql -uroot -p123456 blog --default-character-set=utf8mb4 < sql.txt
docker exec -i blog-mysql mysql -uroot -p123456 blog --default-character-set=utf8mb4 < seed.sql
```

Docker 前端：`http://localhost:9091`，管理员 `admin` / `123`

本地开发（`8080`/`5173`）和 Docker（`9090`/`9091`）可同时运行，互不冲突。

项目地址：[https://github.com/wksbgk/springboot-vue-blog](https://github.com/wksbgk/springboot-vue-blog)

 
