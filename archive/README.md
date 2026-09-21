# Mekanism Complex Industries - v1 归档说明

本目录归档了 **Mekanism Complex Industries** 初代设计（Electric Mining Extractor 电动采矿提取器与原油开采系统）。

## 归档元数据
- **归档时间**：2026-09-21
- **目标游戏版本**：Minecraft 1.20.1 (Forge 47.3.0 / NeoForge 兼容)
- **Mekanism 依赖版本**：`1.20.1-10.4.16.80`
- **Git 归档分支**：`archive/v1-extractor-design`
- **Git 归档标签**：`v1.0-extractor-archive`
- **完整物理快照文件**：`archive/Mekanism-Complex-Industries_v1_backup.zip`

## 初代主要功能与模块
1. **电动采矿提取器 (Electric Mining Extractor)**：
   - 多方块/单方块结合逻辑：检测地底岩芯（Rock Core），钻探基岩层并产出原油（Crude Oil）与矿物。
   - 升级支持：支持能量升级（Speed/Energy Upgrade）以及区块加载升级（Anchor Upgrade）。
   - 自定义 GUI：高仿 Mekanism 原版风格数字采矿机（Digital Miner）布局，包含独立升级侧滑窗（Upgrade Window）、安全配置侧栏、运行状态机与可视化光束/钻探反馈。
2. **原油流体系统 (Crude Oil)**：
   - 具备完整的自定义流体属性、流体方块、流体桶与物理特性。
3. **网络数据包通信 (Packets)**：
   - `PacketExtractorConfig`：支持范围、运行状态配置的客户端-服务端双向同步。
4. **设计文档存盘**：
   - 包含 `AgentTemp/Mekanism Add-on UI.md` 及相关设计思路。

如需回溯或查看历史实现代码，可随时解压本目录下的备份包或切换到 Git 归档分支：
```bash
git checkout archive/v1-extractor-design
```
