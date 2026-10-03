# Changelog | 更新日志

All notable changes to this project will be documented in this file.  
本项目的所有重要变更均记录在此文件中。

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.0] - 2026-10-03 (1.21.1-NeoForge)

### 🎉 Initial Release | 首次发布

#### Added | 新增

- **Petroleum Processing System | 石油加工系统**
  - Crude Oil fluid with worldgen lakes and ore deposits (石油流体，含世界生成的油湖和矿脉)
  - Solid Crude Oil Ore & Deepslate variant (固态原油矿石及深层变种)
  - Solid Crude Oil item — high-density fuel (固态原油物品——高密度燃料，燃烧时间 3200 ticks)
  - Dense Crude Oil chemical (致密原油化学品)
  - Petroleum Harvesting Upgrade for Digital Miner (数字矿机石油采集升级)

- **Industrial Refinery Tower (Multiblock) | 工业炼油塔（多方块结构）**
  - 3x3x5 ~ 5x5x12 variable-size multiblock (可变尺寸多方块结构)
  - Crude Oil thermal cracking into 5 product chemicals: Petroleum Gas, Naphtha, Refined Fuel, Heavy Oil, Bitumen (原油热裂解为 5 种产物化学品：石油气、石脑油、精炼燃料、重油、沥青)
  - Heat-driven processing with dynamic temperature management (热驱动加工，动态温度管理)
  - Refinery Casing, Valve (input/output modes), Controller, Dredge Pipe blocks (炼油厂外壳、阀门、控制器、疏浚管方块)
  - Dynamic valve mode textures and configurator interaction (动态阀门模式材质和配置器交互)
  - JEI & EMI recipe viewer integration (JEI 和 EMI 配方查看器集成)
  - Mob spawn suppression inside refinery structure (炼油塔结构内部生物生成抑制)

- **Industrial Freezer (Multiblock) | 工业冷冻机（多方块结构）**
  - 4x4x4 ~ 8x8x8 variable-size multiblock (可变尺寸多方块结构)
  - Cryogenic distillation processes: Nitrogen, Noble Gas, Cryogenic Refrigerant (低温蒸馏工艺：氮气、惰性气体、低温冷冻液)
  - Requires temperature below -100°C (需要温度低于 -100°C)
  - Freezer Casing, Valve, Controller blocks (冷冻机外壳、阀门、控制器方块)

- **Air Compressor | 空气压缩机**
  - Single-block machine that compresses ambient air into Compressed Air chemical (单方块机器，将环境空气压缩为压缩空气化学品)
  - Power consumption: 5 FE/t, output: 10 mB/t (功耗 5 FE/t，产出 10 mB/t)

- **Resistive Cooler | 电阻冷却器**
  - Converts electrical energy into cryogenic cooling effect (将电能转化为低温冷却效果)
  - Configurable energy-to-cooling ratio (可配置的能量冷却比率)
  - Integrates with Industrial Freezer multiblock (与工业冷冻机多方块结构集成)

- **Chemical Solidifier & Factory | 化学凝固机及工厂**
  - Compresses and solidifies chemicals into solid items or blocks (压缩并凝固化学品为固体物品或方块)
  - Factory tiers: Basic → Advanced → Elite → Ultimate (工厂等级：基础 → 高级 → 精英 → 终极)
  - Custom recipe type: Chemical Solidifying (自定义配方类型：化学凝固)

- **Decorative Blocks | 装饰方块**
  - Bitumen Block — solidified from bitumen chemical (沥青方块——由沥青化学品凝固而成)
  - 16 dyed variants with full color palette (16 种染色变体，完整调色板)
  - Stairs and Slabs for all variants (所有变体的楼梯和台阶)
  - Mekanism Painting Machine support for recoloring (Mekanism 涂装机支持重新上色)
  - Stonecutting recipes for all shapes (所有形状的切石配方)

- **Chemicals | 化学品**
  - Compressed Air (压缩空气)
  - Nitrogen (氮气)
  - Noble Gas (惰性气体)
  - Petroleum Gas (石油气)
  - Naphtha (石脑油)
  - Refined Fuel (精炼燃料)
  - Heavy Oil (重油)
  - Bitumen (沥青)
  - Dense Crude Oil (致密原油)

- **Fluids | 流体**
  - Crude Oil with custom dark viscous appearance (自定义深色粘稠外观的原油)
  - Cryogenic Refrigerant (低温冷冻液)

- **World Generation | 世界生成**
  - Crude Oil Lake feature — rare surface oil deposits (原油湖特性——稀有地表油矿)
  - Solid Crude Oil Ore — underground ore veins (固态原油矿石——地下矿脉)

- **Integration | 集成**
  - JEI (Just Enough Items) recipe viewer support (JEI 配方查看器支持)
  - EMI recipe viewer plugin (EMI 配方查看器插件)
  - Mekanism Rotary Condensentrator Crude Oil conversion (Mekanism 旋转凝结器原油转换)
  - Mekanism Enrichment Chamber ore processing (Mekanism 富集仓矿石处理)

- **Technical | 技术**
  - Mixin-based Mekanism integration for custom upgrades (基于 Mixin 的 Mekanism 集成，支持自定义升级)
  - Custom network packets for cooler energy & factory sorting (自定义网络包用于冷却器能量和工厂排序)
  - GameTest framework support (GameTest 框架支持)
  - Bilingual localization: English & Chinese (双语本地化：英文和中文)
