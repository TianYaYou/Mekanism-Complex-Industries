# Mekanism Complex Industries

**Mekanism Complex Industries** 是一个基于 Minecraft 1.20.1 NeoForge / Forge 平台的高科技重工业拓展模组（Add-on for Mekanism）。

## 核心设计理念
遵循 `AgentTemp/Mekanism Add-on UI.md` 中的工业科技视觉与交互规范：
- 沉浸式高科技冷硬工业风格（Sci-Fi Clean Industrial）。
- 严格复用 Mekanism 原生组件库（`GuiMekanism`, `GuiEnergyGauge`, `GuiFluidGauge`, `GuiProgress` 等）。
- 模块化侧边栏控制系统（安全配置、红石响应、多面 I/O 配置、升级插件槽）。

---

## 运行与编译环境
- **Minecraft 版本**: `1.20.1`
- **Mod 加载器**: `NeoForge 1.20.1-47.1.106` (FML 47.2.2 / Forge 47.1.x API 兼容)
- **编译工具链**: Java 17 (Gradle Toolchain 自动管理)
- **核心前置依赖**:
  - `Mekanism 1.20.1-10.4.16.80`
  - `MekanismGenerators`
  - `MekanismAdditions`
  - `MekanismTools`

---

## 目录结构
```text
Mekanism-Complex-Industries/
├── AgentTemp/                          # 开发文档与设计规划
│   ├── Mekanism Add-on UI.md           # Mekanism 官方级 UI 规范指南
│   └── Plans/                          # 历史版本计划归档
├── src/main/
│   ├── java/com/complexindustries/mekanism/
│   │   ├── MekanismComplexIndustries.java  # Mod 主入口
│   │   ├── MCIConstants.java               # 常量定义
│   │   ├── client/                         # 客户端渲染与设置
│   │   └── registration/                   # 注册器 (Items, Blocks, Tabs, Menus)
│   └── resources/
│       ├── META-INF/mods.toml              # Mod 元数据
│       ├── pack.mcmeta                     # 资源包描述
│       └── assets/mekanism_complex_industries/
│           ├── lang/                       # 中英双语国际化
│           ├── models/                     # 物品与方块模型
│           └── textures/                   # 材质贴图
├── build.gradle                        # 构建脚本 (含 deployToModpack)
├── gradle.properties                   # 版本与路径配置
└── settings.gradle                     # 仓库与插件镜像
```

---

## 常用 Gradle 指令

### 1. 编译构建
```bash
./gradlew build
```
编译生成的 Mod jar 包位于 `build/libs/`。

### 2. 一键部署到测试客户端
我们在 `build.gradle` 中内置了一键部署任务，会自动清理旧版本并将最新编译出来的 Jar 复制到你的测试整合包：
```bash
./gradlew deployToModpack
```
默认目标路径：`E:\Program Files (x86)\Marcft\.minecraft\versions\1.20.1 E7\mods`

### 3. 启动开发客户端进行调试
```bash
./gradlew runClient
```
