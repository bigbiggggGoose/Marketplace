## 二手物品交易平台 - 前端模板（Vue3 + Vite + TypeScript）

### 快速开始

1. 安装依赖
```bash
npm install
```

2. 本地启动（开发）
```bash
npm run dev
```

3. 生产构建
```bash
npm run build
```

4. 本地预览构建产物
```bash
npm run preview
```

### 技术栈
- Vue 3 + Vite + TypeScript
- Pinia（状态管理）
- Vue Router（路由）
- Element Plus（UI 组件库）

### 环境变量
- 将 `env.development.sample` 复制为 `.env.development`，按需修改：
```
VITE_API_BASE=/api
```
在开发环境下保持 `/api`，由 `vite.config.ts` 代理到 `http://localhost:8085`。

### 目录结构
```
marketplace-vue3
├─ index.html
├─ package.json
├─ tsconfig.json
├─ vite.config.ts
├─ env.development.sample
├─ src
│  ├─ main.ts
│  ├─ App.vue
│  ├─ api
│  │  ├─ http.ts
│  │  ├─ auth.ts
│  │  ├─ items.ts
│  │  └─ report.ts
│  ├─ router
│  │  └─ index.ts
│  ├─ store
│  │  └─ auth.ts
│  ├─ types
│  │  └─ index.ts
│  ├─ utils
│  │  └─ storage.ts
│  ├─ components
│  │  ├─ NavBar.vue
│  │  ├─ ItemCard.vue
│  │  ├─ UploadImage.vue
│  │  ├─ MessagePanel.vue
│  │  └─ ReportDialog.vue
│  └─ pages
│     ├─ Home.vue
│     ├─ Login.vue
│     ├─ Register.vue
│     ├─ ItemList.vue
│     ├─ ItemDetail.vue
│     ├─ ItemPublish.vue
│     ├─ UserProfile.vue
│     └─ AdminReview.vue
```

### 接口与代理说明
- 默认所有请求走 `/api` 前缀，Vite devServer 代理到 `http://localhost:8085`（见 `vite.config.ts`）。
- 鉴权：请求头自动加 `Authorization: Bearer <token>`（登录后保存到 `localStorage`）。
- 上传：`UploadImage` 组件支持自动附加鉴权头，后端返回形如 `{ url: 'https://...' }`。

如后端路径不同，请修改：
- 代理：`vite.config.ts` 中 `server.proxy['/api'].target`
- 基础地址：`src/api/http.ts` 的 `baseURL`

### 后端与数据库
- 数据库使用 MySQL。表结构与 API 契约见 `docs/backend-spec.md`。
- 本地后端建议运行在 `http://localhost:8085`，与前端代理匹配。


