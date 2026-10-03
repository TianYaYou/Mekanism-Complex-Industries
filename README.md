<![CDATA[<div align="center">

# ⚙️ Mekanism Complex Industries

**An advanced industrial expansion add-on for Mekanism — Petroleum Harvesting & Chemical Ecology**

**Mekanism 高级工业扩展模组 — 石油开采与化工生态**

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-62B47A?logo=mojangstudios&logoColor=white)](https://www.minecraft.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-21.1.200-orange?logo=curseforge&logoColor=white)](https://neoforged.net/)
[![Mekanism](https://img.shields.io/badge/Mekanism-10.7.19.85-5FCDFA)](https://github.com/mekanism/Mekanism)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)

[English](#-overview) | [中文](#-概述)

</div>

---

## 📖 Overview

**Mekanism Complex Industries** is a content add-on mod for [Mekanism](https://github.com/mekanism/Mekanism) on Minecraft 1.21.1 (NeoForge). It introduces a complete **petroleum harvesting and chemical processing** production chain — from discovering crude oil in the world, to refining it through an industrial tower, to producing advanced chemicals and decorative materials.

The mod follows Mekanism's design philosophy: immersive Sci-Fi industrial aesthetics, modular multiblock structures, and deep integration with Mekanism's existing systems (upgrades, GUI components, recipe viewers, etc.).

### ✨ Key Features

- 🛢️ **Petroleum Processing** — Crude oil worldgen (lakes & ores), Digital Miner petroleum upgrade, and a complete oil-to-chemicals pipeline
- 🏭 **Industrial Refinery Tower** — Variable-size 3×3×5 to 5×5×12 multiblock for thermal cracking of crude oil into 5 chemical products
- ❄️ **Industrial Freezer** — Variable-size 4×4×4 to 8×8×8 cryogenic multiblock for air distillation processes
- 🔩 **Chemical Solidifier** — Single-block machine with full factory tier support (Basic → Ultimate)
- 💨 **Air Compressor** — Ambient air compression into industrial compressed air
- 🧊 **Resistive Cooler** — Energy-to-cryogenic-cooling converter for freezer integration
- 🎨 **Decorative Bitumen Blocks** — 17 colors × 3 shapes (block, stairs, slab) = 51 decorative blocks
- 🔌 **Full Integration** — JEI & EMI recipe viewer plugins, Mekanism Rotary Condensentrator support

---

## 📖 概述

**Mekanism Complex Industries** 是一个基于 Minecraft 1.21.1 NeoForge 平台的 [Mekanism](https://github.com/mekanism/Mekanism) 内容扩展模组。它引入了一套完整的**石油开采与化工加工**生产链——从世界中发现原油，到通过工业炼油塔进行精炼，再到生产高级化学品和装饰材料。

本模组遵循 Mekanism 的设计哲学：沉浸式科幻工业美学、模块化多方块结构，以及与 Mekanism 现有系统的深度集成（升级系统、GUI 组件、配方查看器等）。

### ✨ 核心特性

- 🛢️ **石油加工系统** — 原油世界生成（油湖和矿脉）、数字矿机石油采集升级、完整的油化工管线
- 🏭 **工业炼油塔** — 可变尺寸 3×3×5 至 5×5×12 多方块结构，原油热裂解产出 5 种化学品
- ❄️ **工业冷冻机** — 可变尺寸 4×4×4 至 8×8×8 低温蒸馏多方块结构
- 🔩 **化学凝固机** — 单方块机器，支持完整工厂等级（基础 → 终极）
- 💨 **空气压缩机** — 环境空气压缩为工业压缩空气
- 🧊 **电阻冷却器** — 电能转低温冷却效果，与冷冻机集成
- 🎨 **装饰沥青方块** — 17 种颜色 × 3 种形状（方块、楼梯、台阶）= 51 种装饰方块
- 🔌 **完整集成** — JEI 和 EMI 配方查看器插件、Mekanism 旋转凝结器支持

---

## 🧪 Content Overview | 内容总览

### Machines | 机器

| Machine | Description | Type |
|---------|-------------|------|
| **Industrial Refinery Tower** | Thermal cracking of crude oil into 5 chemicals | Multiblock (3×3×5 ~ 5×5×12) |
| **Industrial Freezer** | Cryogenic distillation at < -100°C | Multiblock (4×4×4 ~ 8×8×8) |
| **Chemical Solidifier** | Solidifies chemicals into items/blocks | Single Block + Factory |
| **Air Compressor** | Compresses ambient air (5 FE/t → 10 mB/t) | Single Block |
| **Resistive Cooler** | Converts energy to cryogenic cooling | Single Block |

### Chemicals | 化学品

| Chemical | Source | Usage |
|----------|--------|-------|
| Compressed Air | Air Compressor | Freezer input |
| Nitrogen | Freezer cryo distillation | Industrial processes |
| Noble Gas | Freezer cryo cycle | Advanced chemistry |
| Petroleum Gas | Refinery cracking | Fuel / further processing |
| Naphtha | Refinery cracking | Chemical feedstock |
| Refined Fuel | Refinery cracking | High-grade fuel |
| Heavy Oil | Refinery cracking | Dense fuel products |
| Bitumen | Refinery cracking | Decorative blocks |
| Dense Crude Oil | Rotary Condensentrator | Refinery input |

### Blocks & Items | 方块与物品

| Block / Item | Description |
|-------------|-------------|
| Solid Crude Oil Ore | Underground ore (+ deepslate variant) |
| Crude Oil (Fluid) | Worldgen oil lakes |
| Cryogenic Refrigerant | Cryogenic cooling fluid |
| Solid Crude Oil (Item) | Fuel item (burn time: 3200 ticks) |
| Petroleum Harvesting Upgrade | Digital Miner upgrade for auto oil mining |
| Bitumen Block (17 colors) | Decorative blocks with stairs & slabs |
| Chemical Solidifier Factory | 4 tiers (Basic/Advanced/Elite/Ultimate) |

---

## 📦 Installation | 安装

### Requirements | 依赖

| Dependency | Version |
|------------|---------|
| Minecraft | 1.21.1 |
| NeoForge | ≥ 21.1.200 |
| Mekanism | 1.21.1-10.7.19.85 |
| Java | 21+ |

### Steps | 步骤

1. Install [NeoForge](https://neoforged.net/) for Minecraft 1.21.1.
2. Install [Mekanism](https://github.com/mekanism/Mekanism) 1.21.1-10.7.19.85.
3. Download the latest release `.jar` from [Releases](../../releases).
4. Place the `.jar` file in your `mods/` folder.
5. Launch Minecraft!

---

## 🔧 Building from Source | 从源码构建

### Prerequisites | 前提条件

- Java 21+ (JDK)
- Git

### Build | 构建

```bash
# Clone the repository | 克隆仓库
git clone https://github.com/TianYaYou/Mekanism-Complex-Industries.git
cd Mekanism-Complex-Industries

# Build the mod | 构建模组
./gradlew build
```

The compiled `.jar` will be in `build/libs/`.

### Run Development Client | 运行开发客户端

```bash
./gradlew runClient
```

### Deploy to Test Modpack | 部署到测试整合包

```bash
./gradlew deployToModpack
```

> [!NOTE]
> Modify `modpack_mods_dir` in `gradle.properties` to point to your test modpack's `mods/` directory.

---

## 📁 Project Structure | 项目结构

```
Mekanism-Complex-Industries/
├── src/main/java/com/complexindustries/mekanism/
│   ├── MekanismComplexIndustries.java    # Mod entry point | 模组入口
│   ├── MCIConstants.java                 # Constants & utilities | 常量与工具
│   ├── client/                           # Client-side rendering | 客户端渲染
│   ├── content/
│   │   ├── block/                        # Block implementations | 方块实现
│   │   │   └── decorative/               # Bitumen decorative blocks | 沥青装饰方块
│   │   ├── fluid/                        # Fluid types | 流体类型
│   │   ├── freezer/                      # Industrial Freezer multiblock | 工业冷冻机
│   │   ├── item/                         # Custom items | 自定义物品
│   │   ├── miner/                        # Digital Miner integration | 数字矿机集成
│   │   ├── refinery/                     # Industrial Refinery Tower | 工业炼油塔
│   │   ├── solidifier/                   # Chemical Solidifier & Factory | 化学凝固机
│   │   ├── tile/                         # Tile entities | 方块实体
│   │   └── upgrade/                      # Custom upgrade system | 自定义升级系统
│   ├── inventory/container/              # GUI containers | GUI 容器
│   ├── mixin/                            # Mekanism Mixin hooks | Mekanism Mixin 钩子
│   ├── network/                          # Network packets | 网络数据包
│   ├── recipe/                           # Custom recipe types | 自定义配方类型
│   ├── registration/                     # Deferred registration | 延迟注册
│   ├── test/                             # GameTest framework | GameTest 框架
│   └── util/                             # Utilities | 工具类
├── src/main/resources/
│   ├── META-INF/neoforge.mods.toml       # Mod metadata | 模组元数据
│   ├── assets/.../lang/                  # i18n (en_us, zh_cn) | 国际化
│   ├── assets/.../models/                # Block & item models | 方块和物品模型
│   ├── assets/.../textures/              # Textures | 材质贴图
│   └── data/.../                         # Recipes, loot tables, worldgen | 配方、战利品表、世界生成
├── tools/                                # Asset generators & dev tools | 资源生成器和开发工具
├── docs/                                 # Documentation | 文档
├── build.gradle                          # Build script | 构建脚本
├── gradle.properties                     # Version & config | 版本与配置
└── settings.gradle                       # Repository mirrors | 仓库镜像
```

---

## 🏗️ Architecture | 架构设计

### Design Principles | 设计原则

1. **Mekanism-native integration** — Strictly reuses Mekanism's GUI component library (`GuiMekanism`, `GuiEnergyGauge`, `GuiFluidGauge`, `GuiProgress`, etc.) for a seamless user experience.

2. **Modular multiblock system** — Both the Refinery Tower and Industrial Freezer use Mekanism's multiblock framework with custom validators and cache systems.

3. **Mixin-based extension** — Custom upgrade types (Petroleum Harvesting) are injected into Mekanism's upgrade enum via Mixin, enabling deep integration without core modification.

4. **Full recipe ecosystem** — All processing recipes support JEI/EMI recipe viewers, crafting table, smelting, enriching, stonecutting, and Mekanism painting machine.

---

## 🗺️ Roadmap | 开发路线图

- [x] Crude Oil worldgen, fluid, and ore system
- [x] Digital Miner Petroleum Harvesting Upgrade
- [x] Industrial Refinery Tower multiblock
- [x] Industrial Freezer multiblock
- [x] Air Compressor & Resistive Cooler
- [x] Chemical Solidifier with factory tiers
- [x] Decorative Bitumen blocks (17 colors × 3 shapes)
- [x] JEI & EMI integration
- [ ] Advanced chemical processing chains
- [ ] Pipeline transport system
- [ ] More decorative & structural blocks
- [ ] CurseForge / Modrinth release

---

## 📜 License | 许可证

This project is licensed under the [MIT License](LICENSE).

---

## 🤝 Contributing | 贡献

Contributions are welcome! Please read our [Contributing Guide](CONTRIBUTING.md) before submitting a PR.

欢迎贡献！请在提交 PR 前阅读我们的[贡献指南](CONTRIBUTING.md)。

---

## 💖 Credits | 致谢

- [Mekanism](https://github.com/mekanism/Mekanism) — The incredible tech mod that makes this all possible.
- [NeoForge](https://neoforged.net/) — The modern modding platform for Minecraft.
- The Minecraft modding community for inspiration and support.

---

<div align="center">

**Made with ❤️ by TianYa**

*Petroleum Harvesting & Chemical Ecology for the modern Mekanism engineer*

</div>
]]>
