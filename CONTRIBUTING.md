# Contributing to Mekanism Complex Industries | 贡献指南

[English](#english) | [中文](#中文)

---

## English

Thank you for your interest in contributing to **Mekanism Complex Industries**! We welcome contributions from the community.

### How to Contribute

1. **Fork the repository** and create a new branch from `1.21.1`.
2. **Make your changes** following the code style of the project.
3. **Test your changes** by running `./gradlew build` and `./gradlew runClient`.
4. **Submit a Pull Request** with a clear description of your changes.

### Development Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/TianYaYou/Mekanism-Complex-Industries.git
   cd Mekanism-Complex-Industries
   ```
2. Build the project:
   ```bash
   ./gradlew build
   ```
3. Run the development client:
   ```bash
   ./gradlew runClient
   ```

### Code Style Guidelines

- Follow standard Java naming conventions.
- Use 4-space indentation.
- All source files must use UTF-8 encoding.
- Reuse Mekanism's native component library wherever possible (`GuiMekanism`, `GuiEnergyGauge`, `GuiFluidGauge`, `GuiProgress`, etc.).
- Register all blocks, items, and tile entities via `DeferredRegister`.

### Reporting Issues

- Use the GitHub Issues tab to report bugs or request features.
- Include your Minecraft version, NeoForge version, Mekanism version, and a detailed description.
- If reporting a crash, please attach the full crash log.

### Pull Request Process

1. Ensure your code compiles without errors (`./gradlew build`).
2. Update any relevant documentation if needed.
3. Add entries to the changelog if applicable.
4. Your PR will be reviewed and merged if it aligns with the project goals.

---

## 中文

感谢你对 **Mekanism Complex Industries** 的贡献兴趣！我们欢迎来自社区的贡献。

### 如何贡献

1. **Fork 本仓库** 并从 `1.21.1` 分支创建新分支。
2. **进行修改**，遵循项目的代码风格。
3. **测试修改**，运行 `./gradlew build` 和 `./gradlew runClient`。
4. **提交 Pull Request**，附上清晰的修改说明。

### 开发环境搭建

1. 克隆仓库：
   ```bash
   git clone https://github.com/TianYaYou/Mekanism-Complex-Industries.git
   cd Mekanism-Complex-Industries
   ```
2. 构建项目：
   ```bash
   ./gradlew build
   ```
3. 运行开发客户端：
   ```bash
   ./gradlew runClient
   ```

### 代码规范

- 遵循标准 Java 命名规范。
- 使用 4 空格缩进。
- 所有源文件必须使用 UTF-8 编码。
- 尽可能复用 Mekanism 原生组件库（`GuiMekanism`、`GuiEnergyGauge`、`GuiFluidGauge`、`GuiProgress` 等）。
- 所有方块、物品和方块实体通过 `DeferredRegister` 注册。

### 报告问题

- 使用 GitHub Issues 选项卡报告错误或请求功能。
- 包含你的 Minecraft 版本、NeoForge 版本、Mekanism 版本和详细描述。
- 如果报告崩溃，请附上完整的崩溃日志。

### Pull Request 流程

1. 确保代码无错误编译（`./gradlew build`）。
2. 如有需要，更新相关文档。
3. 如适用，添加变更日志条目。
4. 你的 PR 将在审核后合并（如果符合项目目标）。
