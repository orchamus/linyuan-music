# 推送到 GitHub

本地仓库已经初始化并完成了分次提交（`git log` 可查看），只差一个远程地址。

## 1. 在 GitHub 新建仓库

打开 <https://github.com/new>：

- Repository name：`linyuan-music`（或你想要的名字）
- 不要勾选 “Add a README / .gitignore / license”（本地已有）
- 建议 Public

## 2. 关联远程并推送

```bash
cd linyuan-music

# 换成你自己的用户名和仓库名
git remote add origin https://github.com/<你的用户名>/linyuan-music.git

git branch -M main
git push -u origin main
```

首次推送会要求输入账号密码，**密码处要填 Personal Access Token**（GitHub 已不支持账号密码推送）：

1. GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic) → Generate new token
2. 勾选 `repo` 权限，生成后复制（只显示一次）
3. `git push` 时：用户名填 GitHub 用户名，密码粘贴 token

## 3. 网络不通时的处理

如果 `git push` 超时（国内直连 GitHub 常见），任选其一：

```bash
# 方式一：给 git 单独配置代理（端口按你本地代理客户端实际端口改，如 7890 / 10809）
git config --global http.https://github.com.proxy http://127.0.0.1:7890
git config --global https.https://github.com.proxy http://127.0.0.1:7890

# 方式二：改用 SSH
ssh-keygen -t ed25519 -C "你的邮箱"
# 把 ~/.ssh/id_ed25519.pub 内容添加到 GitHub → Settings → SSH and GPG keys
git remote set-url origin git@github.com:<你的用户名>/linyuan-music.git
```

取消代理：

```bash
git config --global --unset http.https://github.com.proxy
git config --global --unset https.https://github.com.proxy
```

## 4. 提交信息与作者

当前提交作者取的是本机 git 配置：

```bash
git config user.name    # 若不是你想要的，先修改再提交
git config user.email
```

如果想让已有提交也显示正确的作者：

```bash
git rebase -i --root      # 逐条 edit，然后 git commit --amend --reset-author
```

## 5. 推送后建议检查

- 仓库首页 README 的截图能正常显示（`docs/screenshots` 已随仓库提交）
- 确认没有提交 `application-dev.yml`（含数据库密码，已在 `.gitignore` 中排除）
- `sql/linyuan_music.sql` 里包含演示账号，如不想公开可修改密码后重新导出
- 可以在仓库的 About 里填写：`Spring Boot 3 + Vue 3 在线音乐平台`，并添加 topics：`spring-boot`、`vue3`、`music-player`、`mybatis-plus`
