# LinYuan Music Admin 💻

## 介绍 📖

**LinYuan Music Admin** 是 LinYuan Music 项目的后台管理系统。本项目基于 [vue-pure-admin](https://github.com/pure-admin/vue-pure-admin) 模板进行开发，采用 **Vue 3**、**Vite 5**、**Pinia**、**Tailwind CSS** 和 **Element Plus** 构建，旨在为 LinYuan Music 提供一个高效、易用的管理后台。

## 主要特性 ✨

管理员可以通过本系统进行以下操作：

- **用户管理**: 查看用户列表、用户资料详情、禁用或启用用户账号。
- **歌手管理**: 添加、编辑、删除歌手信息，维护歌手资料库。
- **歌曲管理**: 添加、编辑、删除歌曲信息，管理歌曲资源。
- **歌单管理**: 创建、编辑、删除歌单，管理推荐歌单或用户歌单。
- **反馈管理**: 查看用户提交的反馈信息，并进行处理或删除。
- **轮播图管理**: 添加、编辑、删除首页轮播图内容。

## 技术栈 🛠️

- **前端框架**: [Vue 3](https://vuejs.org/)
- **构建工具**: [Vite 5](https://vitejs.dev/)
- **状态管理**: [Pinia](https://pinia.vuejs.org/)
- **UI 库**: [Element Plus](https://element-plus.org/)
- **CSS 框架**: [Tailwind CSS](https://tailwindcss.com/)
- **语言**: TypeScript
- **包管理器**: [pnpm](https://pnpm.io/)

## 系统需求 ⚙️

- **Node.js**: `^18.18.0 || ^20.9.0 || >=22.0.0`
- **pnpm**: `>=9`

## 代码仓库 ⭐

- [GitHub 代码仓库](https://github.com/Alex-LiSun/linyuan-music-admin)

## 安装与启动 🚀

1.  **克隆项目**

    ```bash
    # GitHub
    git clone https://github.com/Alex-LiSun/linyuan-music-admin.git

    cd linyuan-music-admin
    ```

2.  **安装依赖**

    ```bash
    pnpm install
    ```

3.  **配置环境变量**

    - 复制 `.env.development` 文件并重命名为 `.env.development.local` (推荐) 或直接修改 `.env.development`。
    - 修改文件中的 `VITE_APP_BASE_API` 为你本地运行或部署的 **LinYuan Music Server** 后端服务地址。

    ```env
    # .env.development.local

    # LinYuan Music Server API 地址 (示例)
    # 请确保替换为你的实际后端服务地址和端口
    VITE_APP_BASE_API = 'http://localhost:8080'
    ```

    - **注意**: 启动前端应用前，请确保你的 `LinYuan Music Server` 后端服务已经成功配置、启动并正在运行。

4.  **启动开发服务器**

    ```bash
    pnpm dev
    ```

    启动后，访问浏览器中显示的本地地址即可。

5.  **构建项目**

    ```bash
    # 构建生产环境
    pnpm build

    # 构建测试环境 (如果配置了)
    # pnpm build:test
    ```

6.  **预览构建结果**

    ```bash
    # 预览生产环境构建
    pnpm preview

    # 预览测试环境构建 (如果配置了)
    # pnpm preview:test
    ```

## 项目脚本 📜

- `pnpm dev`: 启动开发服务器。
- `pnpm build`: 构建生产版本。
- `pnpm preview`: 本地预览生产版本。
- `pnpm build:test`: 构建测试版本。
- `pnpm preview:test`: 本地预览测试版本。
- `pnpm lint`: 使用 ESLint 检查代码规范。
- `pnpm lint:fix`: 使用 ESLint 自动修复代码规范问题。
- `pnpm format`: 使用 Prettier 格式化代码。
- `pnpm type-check`: 使用 vue-tsc 进行 TypeScript 类型检查。

## 项目演示 📺

视频地址：[https://www.bilibili.com/video/BV1tKJ8z8E6z/]

## 项目截图 📷

![登录界面](https://github.com/Alex-LiSun/linyuan-music-admin/blob/main/img/admin_login.png)
![系统首页界面](https://github.com/Alex-LiSun/linyuan-music-admin/blob/main/img/admin_home.png)
![用户管理界面](https://github.com/Alex-LiSun/linyuan-music-admin/blob/main/img/admin_user_management.png)
![歌手管理界面](https://github.com/Alex-LiSun/linyuan-music-admin/blob/main/img/admin_artist_management.png)
![歌曲管理界面](https://github.com/Alex-LiSun/linyuan-music-admin/blob/main/img/admin_song_management.png)
![歌单管理界面](https://github.com/Alex-LiSun/linyuan-music-admin/blob/main/img/admin_playlist_management.png)
![反馈管理界面](https://github.com/Alex-LiSun/linyuan-music-admin/blob/main/img/admin_feedback_management.png)
![轮播图管理界面](https://github.com/Alex-LiSun/linyuan-music-admin/blob/main/img/admin_banner_management.png)

## 项目后台接口 🧩

本项目的前端界面依赖自建的后端服务 **LinYuan Music Server** 来提供所有的业务逻辑和数据接口。

- 请确保你已经按照 **LinYuan Music Server** 项目的说明文档成功部署并运行了后端服务。
- 后端仓库地址: [https://github.com/Alex-LiSun/linyuan-music-server](https://github.com/Alex-LiSun/linyuan-music-server)

## 免责声明 ⚠️

**LinYuan Music Admin** 项目仅供学习和技术研究使用。应用内展示的所有音乐内容、用户数据等均由您自行部署和管理的 **LinYuan Music Server** 后端服务提供。请在遵守相关国家和地区的法律法规以及版权政策的前提下使用。

- **请勿用于任何商业用途。**
- 对于因使用本项目（包括其依赖的 **LinYuan Music Server** 后端服务）而可能产生的任何直接或间接问题、数据安全风险、版权纠纷或经济损失，项目作者不承担任何责任。
- 用户需自行承担所有使用风险，包括确保后端服务的数据来源合法合规。

在您使用本软件前，请仔细阅读并理解本免责声明。继续使用即表示您同意本声明的所有条款。

## 许可证 📄

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 贡献 ❤️

欢迎各种形式的贡献，包括提交 Issue、Pull Request 或提出建议！

## 常见问题 (FAQ) ❓

- **启动时遇到错误怎么办？**

  - 请确保您的 Node.js 和 pnpm 版本满足 [系统需求](#系统需求-⚙️)。
  - 检查 `pnpm install` 过程中是否有报错信息。
  - 确认 `.env.development.local` 或 `.env.development` 中的 `VITE_APP_BASE_API` 配置是否正确，并且对应的后端 API 服务已成功运行。

- **如何切换主题？**

  - 通常在应用的设置或侧边栏菜单中可以找到主题切换选项（例如亮色/暗色模式）。请根据应用内的指引操作。

- **API 无法访问？**
  - 首先确认你的 **LinYuan Music Server** 后端服务是否已经按照其文档正确启动，并且正在运行。
  - 检查你在 `.env.development.local` 或 `.env.development` 文件中配置的 `VITE_APP_BASE_API` 地址和端口是否与后端服务实际监听的地址和端口一致。
  - 检查浏览器开发者工具的网络(Network)选项卡，看看前端发起的 API 请求是否收到了正确的响应，或者是否有 CORS (跨域资源共享) 相关的错误。
  - 检查操作系统的防火墙或任何网络代理设置，确保没有阻止从前端到后端的网络连接。
  - 查阅 **LinYuan Music Server** 的运行日志，寻找可能的错误信息。
