# 云笺阁图片分享平台

## 项目概述
云笺阁图片分享平台是一个基于云存储的图库平台，提供图片上传、管理、展示、下载等功能。
<img width="1895" height="814" alt="image" src="https://github.com/user-attachments/assets/cc58902d-a991-409a-b6ea-975fb7888449" />
<img width="1422" height="725" alt="image" src="https://github.com/user-attachments/assets/ab32d62c-4b7b-438e-969a-c69f8b88f122" />
<img width="1865" height="811" alt="image" src="https://github.com/user-attachments/assets/cd5a17ae-7e6a-4056-a2d6-360f28024cca" />
<img width="1877" height="816" alt="image" src="https://github.com/user-attachments/assets/4cc9c558-db49-450a-a74a-f034d00e11ed" />



## 技术栈

### 前端技术
- 框架：Vue 3
- UI组件库：Ant Design Vue
- 状态管理：Pinia
- 路由：Vue Router
- 图表：ECharts
- 构建工具：Vite
- 其他功能库：
  - 文件下载：file-saver
  - 图片裁剪：vue-cropper
  - 颜色选择器：vue3-colorpicker

### 后端技术
- 框架：Spring Boot 2
- 数据库：MySQL + MyBatis Plus
- 权限认证：Sa-Token
- 缓存：
  - Redis
  - Caffeine (本地缓存)
- 云存储：腾讯云COS
- 文档：Knife4j
- 其他功能：
  - WebSocket
  - 验证码
  - 分库分表
  - 高性能队列(Disruptor)

## 项目结构

### 前端目录结构
```
cloud-picture-frontend/
├── src/
│   ├── api/            # API请求封装
│   ├── assets/         # 静态资源
│   ├── components/     # 公共组件
│   ├── layouts/        # 布局组件
│   ├── pages/          # 页面组件
│   ├── router/         # 路由配置
│   ├── stores/         # 状态管理(Pinia)
│   ├── utils/          # 工具函数
│   ├── App.vue         # 根组件
│   └── main.ts         # 应用入口
```

### 后端目录结构
```
cloud-picture-backend/
├── src/main/java/com/steve/cloudpicturebackend/
│   ├── annotation/     # 自定义注解
│   ├── aop/            # 切面编程
│   ├── api/            # API接口
│   ├── config/         # 配置类
│   ├── controller/     # 控制器层
│   ├── exception/      # 异常处理
│   ├── manager/        # 业务管理层
│   ├── mapper/         # 数据访问层
│   ├── model/          # 数据模型
│   ├── service/        # 业务逻辑层
│   └── utils/          # 工具类
├── sql/                # 数据库脚本
```
### 功能体验
```
访问如下地址：xxxxxxxxxx
