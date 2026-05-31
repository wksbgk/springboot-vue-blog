-- 清空文章表
DELETE FROM article;

INSERT INTO article (title, summary, content, user_id, status, view_count) VALUES
('Spring Boot + Vue 全栈博客开发实战',
 '从零搭建一个前后端分离的博客系统，涵盖 Spring Boot、Vue 3、MyBatis-Plus、JWT 认证等核心技术的完整实践。',
 '## 项目概述\n本项目采用前后端分离架构，后端基于 Spring Boot 2.7，前端使用 Vue 3 + Vite。\n\n## 技术栈\n后端：Spring Boot 2.7.18 + MyBatis-Plus 3.5.5 + Spring Security + JWT + Redis + MySQL\n前端：Vue 3 Composition API + Vue Router 4 + Axios + Vite\n\n## 核心功能\n### 用户认证\n使用 JWT 实现无状态认证，Token 中嵌入用户角色信息，拦截器统一校验。\n\n### 角色权限\n系统分为管理员和普通用户两种角色。管理员拥有全部 CRUD 权限，普通用户仅可查看内容。\n\n### Redis 缓存\n文章列表使用 Redis 缓存，提升查询性能。浏览量通过 Redis 自增计数。\n\n## 总结\n这个项目涵盖了全栈开发的大部分基础技能点，适合入门学习。',
 1, 'published', 128);

INSERT INTO article (title, summary, content, user_id, status, view_count) VALUES
('Vue 3 Composition API 完全指南',
 '深入理解 Vue 3 的组合式 API，掌握 setup、ref、reactive、watch、computed 等核心概念。',
 '## 为什么选择 Composition API\nVue 3 引入的 Composition API 解决了 Options API 的几个痛点：逻辑复用困难、代码组织分散、TypeScript 支持不佳。\n\n## 核心 API\n### ref 与 reactive\nref 用于基本类型，reactive 用于对象。搭配 computed 做计算属性，watch 监听变化。\n\n### 生命周期\nonMounted、onUpdated、onUnmounted 替代了原来的 mounted、updated、destroyed。\n\n## 实战技巧\n使用 script setup 语法糖可以进一步简化代码，无需手动 return。搭配 defineProps 和 defineEmits 处理组件通信。Composition API 让代码更易于维护和测试，是 Vue 3 开发的首选方式。',
 1, 'published', 86);

INSERT INTO article (title, summary, content, user_id, status, view_count) VALUES
('MySQL 索引优化实战',
 '深入理解 MySQL 索引原理，掌握 EXPLAIN 执行计划分析，让你的查询速度提升百倍。',
 '## 索引的本质\n索引是帮助 MySQL 高效获取数据的数据结构，最常见的 B+ 树索引就像一本书的目录。\n\n## 常见索引类型\n主键索引：唯一非空，自动创建\n普通索引：可重复，用于查询条件\n联合索引：多列组合，遵循最左前缀原则\n全文索引：用于文本搜索\n\n## 最左前缀原则\n联合索引 (a, b, c) 在 WHERE a=1 或 WHERE a=1 AND b=2 时生效，但 WHERE b=2 或 WHERE c=3 不走索引。\n\n## EXPLAIN 分析\n使用 EXPLAIN 查看执行计划，关注 type、key、rows 字段。type 从好到差：system > const > ref > range > index > ALL。\n\n## 优化建议\n避免在 WHERE 中对字段做函数操作、LIKE 前置通配不走索引、合理使用覆盖索引减少回表。',
 1, 'published', 203);

INSERT INTO article (title, summary, content, user_id, status, view_count) VALUES
('Redis 缓存设计与最佳实践',
 '从缓存穿透到缓存雪崩，全面解析 Redis 缓存设计中的常见问题与解决方案。',
 '## Redis 为什么快\nRedis 基于内存操作、单线程模型、多路复用 IO，读写速度可达 10w+ QPS。\n\n## 常见问题\n### 缓存穿透\n查询不存在的数据，请求直接打到数据库。解决：布隆过滤器或缓存空值。\n\n### 缓存击穿\n热点 key 过期瞬间，大量请求涌入数据库。解决：互斥锁或永不过期。\n\n### 缓存雪崩\n大量 key 同时过期，数据库压力骤增。解决：随机过期时间、多级缓存、限流降级。\n\n## 数据结构应用\nString：计数、分布式锁\nHash：用户信息存储\nList：消息队列、时间线\nSet：标签、共同好友\nZSet：排行榜\n\n## 实战经验\n合理设置过期时间、避免 big key、使用 Pipeline 批量操作、主从+哨兵保证高可用。',
 1, 'published', 157);

INSERT INTO article (title, summary, content, user_id, status, view_count) VALUES
('Docker 容器化部署入门',
 '学会使用 Docker 打包应用，实现一键部署，告别环境不一致的烦恼。',
 '## 为什么需要 Docker\n开发环境、测试环境、生产环境不一致导致的各种问题，Docker 通过容器化一劳永逸。\n\n## 核心概念\n镜像 Image：应用的打包模板\n容器 Container：镜像的运行实例\n仓库 Registry：镜像的存储分发中心\n\n## Dockerfile 实战\nFROM 指定基础镜像，COPY 复制文件，RUN 执行命令，EXPOSE 声明端口，CMD 定义启动命令。多阶段构建可大幅减小镜像体积。\n\n## Docker Compose\n通过 YAML 文件一键启动多容器应用。例如博客项目可以定义 mysql、redis、backend、frontend 四个服务，网络互通。\n\n## 常用命令\ndocker build -t myapp .       构建镜像\ndocker run -d -p 8080:8080    后台运行\ndocker-compose up -d          编排启动\ndocker logs -f container      查看日志\n\nDocker 已经成为现代软件交付的标准方式，值得每个开发者掌握。',
 1, 'published', 91);
