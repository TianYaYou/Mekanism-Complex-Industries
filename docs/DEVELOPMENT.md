<![CDATA[# Development Guide | 开发指南

[English](#english) | [中文](#中文)

---

## English

### Tech Stack

| Component | Version |
|-----------|---------|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.200 |
| Mekanism | 1.21.1-10.7.19.85 |
| Java | 21 (Gradle Toolchain managed) |
| Build System | Gradle with NeoForge ModDev 2.0.147 |
| Mixin | NeoForge built-in |

### Environment Setup

#### 1. Clone & Build

```bash
git clone https://github.com/TianYaYou/Mekanism-Complex-Industries.git
cd Mekanism-Complex-Industries
./gradlew build
```

#### 2. IDE Setup

**IntelliJ IDEA (Recommended):**
1. Open the project folder as a Gradle project.
2. Wait for Gradle sync to complete.
3. Run configurations for `runClient`, `runServer`, and `runData` will be auto-generated.

**VS Code:**
1. Install the "Extension Pack for Java" extension.
2. Open the project folder.
3. Use the Gradle sidebar to run tasks.

#### 3. Run Configurations

| Task | Description |
|------|-------------|
| `./gradlew runClient` | Launch dev client with mod loaded |
| `./gradlew runServer` | Launch dev server |
| `./gradlew runData` | Run data generators |
| `./gradlew build` | Compile and package JAR |
| `./gradlew deployToModpack` | Deploy JAR to test modpack |
| `./gradlew runGameTestServer` | Run GameTest suite |

### Project Architecture

#### Registration System

All game objects are registered via NeoForge `DeferredRegister` in the `registration/` package:

| Class | Registers |
|-------|-----------|
| `MCIBlocks` | All blocks (machines, ores, decorative) |
| `MCIItems` | Items (upgrade, solid crude oil, buckets) |
| `MCITileEntityTypes` | Block entities (tile entities) |
| `MCIContainerTypes` | GUI menu containers |
| `MCIFluids` | Fluid types and fluid blocks |
| `MCIChemicals` | Mekanism chemical types |
| `MCIGases` | Mekanism gas types (legacy) |
| `MCICreativeTabs` | Creative mode tabs |
| `MCIRecipeTypes` | Custom recipe types |
| `MCIRecipeSerializers` | Recipe serializers |

#### Multiblock System

Both multiblock structures (Refinery Tower and Industrial Freezer) follow Mekanism's multiblock framework:

```
Controller Block  →  TileEntityController  →  MultiblockData
     ↓                      ↓
  Validator  ←  Validates structure shape & composition
     ↓
  Cache  →  Persists multiblock state across chunk loads
```

**Refinery Tower:**
- Size: 3×3×5 to 5×5×12 (variable height)
- Components: Casing, Valve (input/output modes), Controller, Dredge Pipe
- Process: Heat-driven crude oil thermal cracking → 5 chemical products
- Special: Dynamic valve textures, mob spawn suppression

**Industrial Freezer:**
- Size: 4×4×4 to 8×8×8 (cubic)
- Components: Casing, Valve, Controller
- Process: Cryogenic distillation (requires < -100°C)
- Special: Integrates with Resistive Cooler for temperature management

#### Mixin System

The mod uses Mixins to extend Mekanism's internal systems:

| Mixin | Target | Purpose |
|-------|--------|---------|
| `MixinUpgrade` | `Upgrade` enum | Inject Petroleum Harvesting upgrade type |
| `MixinTileEntityMekanism` | `TileEntityMekanism` | Support custom upgrade slots |
| `MixinTileEntityDigitalMiner` | `TileEntityDigitalMiner` | Auto-harvest crude oil fluids |
| `MixinThreadMinerSearch` | `ThreadMinerSearch` | Include fluid blocks in miner search |
| `MixinMinerItemStackFilter` | `MinerItemStackFilter` | Custom filter for petroleum blocks |
| `MixinGuiUpgradeWindow` | `GuiUpgradeWindow` | Display custom upgrade in GUI |
| `MixinTileComponentUpgrade` | `TileComponentUpgrade` | Accept custom upgrade items |
| `MixinUpgradeInventorySlot` | `UpgradeInventorySlot` | Validate custom upgrade slots |
| `MixinUpgradeUtils` | `UpgradeUtils` | Register custom upgrade utilities |

#### Network System

Custom packets for client-server communication:

| Packet | Direction | Purpose |
|--------|-----------|---------|
| `PacketSetCoolerEnergy` | C→S | Set Resistive Cooler energy target |
| `PacketToggleFactorySorting` | C→S | Toggle factory output sorting mode |

#### Recipe System

Custom recipe type `ChemicalSolidifierRecipe` with serializer for the Chemical Solidifier:

```
Chemical Input (mB) → Item/Block Output
```

Recipes are defined as JSON in `data/mekanism_complex_industries/recipe/solidifying/`.

### Resource Generation Tools

The `tools/` directory contains scripts for automated asset generation:

| Script | Purpose |
|--------|---------|
| `generate_assets.py` | Generate block/item model JSONs |
| `generate_refinery_assets.py` | Generate refinery block models |
| `generate_textures.js` | Generate pixel art textures |
| `generate_pixel_textures.js` | Generate detailed pixel textures |
| `generate_cooler_textures.js` | Generate cooler block textures |
| `generate_data.js` | Generate recipe and loot table JSONs |
| `generate_loot_tables.js` | Generate block loot tables |
| `generate_models.js` | Generate 3D block models |
| `mc_rcon.py` | RCON client for live-server testing |

### Testing

The project uses NeoForge's GameTest framework:

```bash
./gradlew runGameTestServer
```

Test structures are located in `src/main/resources/data/mekanism_complex_industries/structure/`.

### Localization

Language files are in `src/main/resources/assets/mekanism_complex_industries/lang/`:

| File | Language |
|------|----------|
| `en_us.json` | English |
| `zh_cn.json` | Simplified Chinese |

### Adding New Content

#### Adding a New Block

1. Create the Block class in `content/block/`.
2. Register in `MCIBlocks`.
3. Create TileEntity (if needed) in `content/tile/` and register in `MCITileEntityTypes`.
4. Add blockstate JSON in `assets/.../blockstates/`.
5. Add block model JSON in `assets/.../models/block/`.
6. Add item model JSON in `assets/.../models/item/`.
7. Add texture in `assets/.../textures/block/`.
8. Add loot table in `data/.../loot_table/blocks/`.
9. Add crafting recipe in `data/.../recipe/`.
10. Add block to mining tags in `data/minecraft/tags/block/`.
11. Add translations in both `en_us.json` and `zh_cn.json`.

#### Adding a New Chemical

1. Register the chemical in `MCIChemicals`.
2. Add translations in both lang files.
3. Add a chemical texture in `assets/.../textures/chemical/` (if needed).

---

## 中文

### 技术栈

| 组件 | 版本 |
|------|------|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.200 |
| Mekanism | 1.21.1-10.7.19.85 |
| Java | 21（Gradle 工具链管理） |
| 构建系统 | Gradle + NeoForge ModDev 2.0.147 |
| Mixin | NeoForge 内置 |

### 环境搭建

#### 1. 克隆与构建

```bash
git clone https://github.com/TianYaYou/Mekanism-Complex-Industries.git
cd Mekanism-Complex-Industries
./gradlew build
```

#### 2. IDE 配置

**IntelliJ IDEA（推荐）：**
1. 将项目文件夹作为 Gradle 项目打开。
2. 等待 Gradle 同步完成。
3. `runClient`、`runServer` 和 `runData` 运行配置会自动生成。

**VS Code：**
1. 安装 "Extension Pack for Java" 扩展。
2. 打开项目文件夹。
3. 使用 Gradle 侧边栏运行任务。

#### 3. 运行配置

| 任务 | 描述 |
|------|------|
| `./gradlew runClient` | 启动加载了模组的开发客户端 |
| `./gradlew runServer` | 启动开发服务器 |
| `./gradlew runData` | 运行数据生成器 |
| `./gradlew build` | 编译并打包 JAR |
| `./gradlew deployToModpack` | 部署 JAR 到测试整合包 |
| `./gradlew runGameTestServer` | 运行 GameTest 测试套件 |

### 项目架构

#### 注册系统

所有游戏对象通过 NeoForge `DeferredRegister` 在 `registration/` 包中注册：

| 类 | 注册内容 |
|----|----------|
| `MCIBlocks` | 所有方块（机器、矿石、装饰） |
| `MCIItems` | 物品（升级、固态原油、桶） |
| `MCITileEntityTypes` | 方块实体 |
| `MCIContainerTypes` | GUI 菜单容器 |
| `MCIFluids` | 流体类型和流体方块 |
| `MCIChemicals` | Mekanism 化学品类型 |
| `MCICreativeTabs` | 创造模式物品栏 |
| `MCIRecipeTypes` | 自定义配方类型 |
| `MCIRecipeSerializers` | 配方序列化器 |

#### 多方块系统

两个多方块结构（炼油塔和工业冷冻机）均遵循 Mekanism 的多方块框架：

```
控制器方块  →  TileEntityController  →  MultiblockData
     ↓                    ↓
  验证器  ←  验证结构形状和组成
     ↓
  缓存  →  跨区块加载持久化多方块状态
```

**炼油塔：**
- 尺寸：3×3×5 至 5×5×12（可变高度）
- 组件：外壳、阀门（输入/输出模式）、控制器、疏浚管
- 工艺：热驱动原油热裂解 → 5 种化学品产物
- 特殊：动态阀门材质、怪物生成抑制

**工业冷冻机：**
- 尺寸：4×4×4 至 8×8×8（立方体）
- 组件：外壳、阀门、控制器
- 工艺：低温蒸馏（需要温度低于 -100°C）
- 特殊：与电阻冷却器集成进行温度管理

#### Mixin 系统

模组使用 Mixin 扩展 Mekanism 的内部系统：

| Mixin | 目标 | 用途 |
|-------|------|------|
| `MixinUpgrade` | `Upgrade` 枚举 | 注入石油采集升级类型 |
| `MixinTileEntityMekanism` | `TileEntityMekanism` | 支持自定义升级槽 |
| `MixinTileEntityDigitalMiner` | `TileEntityDigitalMiner` | 自动采集原油流体 |
| `MixinThreadMinerSearch` | `ThreadMinerSearch` | 在矿机搜索中包含流体方块 |
| `MixinMinerItemStackFilter` | `MinerItemStackFilter` | 石油方块自定义过滤器 |
| `MixinGuiUpgradeWindow` | `GuiUpgradeWindow` | 在 GUI 中显示自定义升级 |
| `MixinTileComponentUpgrade` | `TileComponentUpgrade` | 接受自定义升级物品 |
| `MixinUpgradeInventorySlot` | `UpgradeInventorySlot` | 验证自定义升级槽 |
| `MixinUpgradeUtils` | `UpgradeUtils` | 注册自定义升级工具 |

#### 网络系统

用于客户端-服务端通信的自定义数据包：

| 数据包 | 方向 | 用途 |
|--------|------|------|
| `PacketSetCoolerEnergy` | 客户端→服务端 | 设置电阻冷却器能量目标 |
| `PacketToggleFactorySorting` | 客户端→服务端 | 切换工厂输出排序模式 |

### 添加新内容

#### 添加新方块

1. 在 `content/block/` 中创建 Block 类。
2. 在 `MCIBlocks` 中注册。
3. 如需要，在 `content/tile/` 中创建 TileEntity 并在 `MCITileEntityTypes` 中注册。
4. 在 `assets/.../blockstates/` 中添加方块状态 JSON。
5. 在 `assets/.../models/block/` 中添加方块模型 JSON。
6. 在 `assets/.../models/item/` 中添加物品模型 JSON。
7. 在 `assets/.../textures/block/` 中添加材质。
8. 在 `data/.../loot_table/blocks/` 中添加战利品表。
9. 在 `data/.../recipe/` 中添加合成配方。
10. 在 `data/minecraft/tags/block/` 中将方块添加到挖掘标签。
11. 在 `en_us.json` 和 `zh_cn.json` 中添加翻译。

#### 添加新化学品

1. 在 `MCIChemicals` 中注册化学品。
2. 在两个语言文件中添加翻译。
3. 如需要，在 `assets/.../textures/chemical/` 中添加化学品材质。
]]>
