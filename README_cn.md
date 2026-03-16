# Gugle的Carpet附加包 [ [English](README.md) | 中文 ]

[![Development Builds](https://github.com/Gu-ZT/gugle-carpet-addition/workflows/Mod%20Build/badge.svg)](https://github.com/Gu-ZT/gugle-carpet-addition/actions/workflows/build.yml)
[![CurseForge downloads](http://cf.way2muchnoise.eu/full_662867_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/guglecarpetaddition)
[![Modrinth downloads](https://img.shields.io/modrinth/dt/gca?color=00AF5C&label=Modrinth%20downloads&logo=modrinth)](https://modrinth.com/mod/gca)
[![GitHub downloads](https://img.shields.io/github/downloads/Gu-ZT/gugle-carpet-addition/total?label=Github%20downloads&logo=github)](https://github.com/Gu-ZT/gugle-carpet-addition/releases)
![menu](docs/pics/menu_zh.png)

## GCA

#### 如果你需要Minecraft 1.13版本的GCA, [请点击此处](https://github.com/Gu-ZT/TISCarpet113WithGCA/releases/latest)

### 假人背包 (openFakePlayerInventory)

允许玩家打开假人背包

* 类型: `boolean`
* 默认值: `false`
* 参考选项: `true`, `false`
* 分类: `GCA`, `BOT`

```
/carpet openFakePlayerInventory true
```

![inv](docs/pics/inv.png)

### 真人背包 (openRealPlayerInventory)

允许玩家打开真人背包

* 类型: `String`
* 默认值: `false`
* 参考选项: `true`, `false`, `ops`, `0`, `1`, `2`, `3`, `4`
* 分类: `GCA`, `experimental`

```
/carpet openRealPlayerInventory true
/carpet openRealPlayerInventory ops
```

### 假人末影箱 (openFakePlayerEnderChest)

允许玩家打开假人末影箱，潜行可以打开假人的末影箱

* 类型: `String`
* 默认值: `false`
* 参考选项: `ender_chest`, `true`, `false`
* 分类: `GCA`, `BOT`

```
/carpet openFakePlayerEnderChest true
/carpet openFakePlayerEnderChest ender_chest
```

![ender](docs/pics/ender.png)

### 假人驻留 (fakePlayerResident)

退出存档时保留假人

* 类型: `boolean`
* 默认值: `false`
* 参考选项: `true`, `false`
* 分类: `GCA`, `BOT`

```
/carpet fakePlayerResident true
```

### 假人动作保存 (fakePlayerReloadAction)

退出存档时保留假人动作

* 类型: `boolean`
* 默认值: `false`
* 参考选项: `true`, `false`
* 分类: `GCA`, `BOT`

```
/carpet fakePlayerReloadAction true
```

### 假人补货 (fakePlayerAutoReplenishment)

让假人自动补货

* 类型: `boolean`
* 默认值: `false`
* 参考选项: `true`, `false`
* 分类: `GCA`, `BOT`

```
/carpet fakePlayerAutoReplenishment true
```

### 假人潜影盒补货 (fakePlayerAutoReplenishmentFormShulkerBox)

让假人自动从潜影盒补货

* 类型: `boolean`
* 默认值: `false`
* 参考选项: `true`, `false`
* 分类: `GCA`, `BOT`

```
/carpet fakePlayerAutoReplenishmentFormShulkerBox true
```

### 假人钓鱼 (fakePlayerAutoFish)

让假人自动钓鱼

* 类型: `boolean`
* 默认值: `false`
* 参考选项: `true`, `false`
* 分类: `GCA`, `BOT`

```
/carpet fakePlayerAutoFish true
```

### 假人切换工具 (fakePlayerAutoReplaceTool)

让假人自动切换快损坏的工具

* 类型: `boolean`
* 默认值: `false`
* 参考选项: `true`, `false`
* 分类: `GCA`, `BOT`

```
/carpet fakePlayerAutoReplaceTool true
```

### 假人名称前缀 (fakePlayerPrefixName)

假人名称前缀

* 类型: `String`
* 默认值: `#none`
* 参考选项: `#none`, `bot_`
* 分类: `GCA`, `BOT`

```
/carpet fakePlayerPrefixName bot_
/carpet fakePlayerPrefixName #none
```

### 假人名称后缀 (fakePlayerSuffixName)

假人名称后缀

* 类型: `String`
* 默认值: `#none`
* 参考选项: `#none`, `_fake`
* 分类: `GCA`, `BOT`

```
/carpet fakePlayerSuffixName _fake
/carpet fakePlayerSuffixName #none
```

### 假人管理 (commandBot)

一个假人管理菜单

* 类型: `String`
* 默认值: `ops`
* 参考选项: `ops`, `0`, `1`, `2`, `3`, `4`, `true`, `false`
* 分类: `GCA`, `BOT`, `command`

```
/carpet commandBot ops
/bot
```

### 待办事项管理 (commandTodo)

待办事项管理菜单

* 类型: `String`
* 默认值: `ops`
* 参考选项: `ops`, `0`, `1`, `2`, `3`, `4`, `true`, `false`
* 分类: `GCA`, `command`

```
/carpet commandTodo ops
/todo
```

### /here 命令 (commandHere)

快速发送位置

* 类型: `String`
* 默认值: `ops`
* 参考选项: `ops`, `0`, `1`, `2`, `3`, `4`, `true`, `false`
* 分类: `GCA`, `command`
* 条件: 需要加载 CarpetAmsAddition

```
/carpet commandHere ops
/here
```

### /whereis 命令 (commandWhereis)

快速定位玩家

* 类型: `String`
* 默认值: `ops`
* 参考选项: `ops`, `0`, `1`, `2`, `3`, `4`, `true`, `false`
* 分类: `GCA`, `command`

```
/carpet commandWhereis ops
/whereis <player>
```

### 地标管理 (commandLoc)

地标管理菜单

* 类型: `String`
* 默认值: `ops`
* 参考选项: `ops`, `0`, `1`, `2`, `3`, `4`, `true`, `false`
* 分类: `GCA`, `command`

```
/carpet commandLoc ops
/loc
```

### 白名单管理 (commandWlist)

白名单管理

* 类型: `boolean`
* 默认值: `false`
* 参考选项: `true`, `false`
* 分类: `GCA`, `command`

```
/carpet commandWlist true
/wlist
```

### 封禁名单管理 (commandBlist)

封禁名单管理

* 类型: `boolean`
* 默认值: `false`
* 参考选项: `true`, `false`
* 分类: `GCA`, `command`

```
/carpet commandBlist true
/blist
```

### 简单获取OP (commandSop)

简单获取OP

* 类型: `boolean`
* 默认值: `false`
* 参考选项: `true`, `false`
* 分类: `GCA`, `command`

```
/carpet commandSop true
/sop
```

### 更好的栅栏门放置 (betterFenceGatePlacement)

让放置的栅栏门与你点击的栅栏门拥有相同的方块状态

* 类型: `boolean`
* 默认值: `false`
* 参考选项: `true`, `false`
* 分类: `GCA`

```
/carpet betterFenceGatePlacement true
```

### 更好的原木去皮 (betterWoodStrip)

仅允许名称中包含“去皮”的斧头对原木去皮

* 类型: `boolean`
* 默认值: `false`
* 参考选项: `true`, `false`
* 分类: `GCA`

```
/carpet betterWoodStrip true
```

### 更好的告示牌交互 (betterSignInteraction)

右键告示牌时与之附着的方块产生交互

* 类型: `boolean`
* 默认值: `false`
* 参考选项: `true`, `false`
* 分类: `GCA`

```
/carpet betterSignInteraction true
```

### 更好的展示框交互 (betterItemFrameInteraction)

右键包含物品的展示框时与之附着的方块产生交互

* 类型: `boolean`
* 默认值: `false`
* 参考选项: `true`, `false`
* 分类: `GCA`

```
/carpet betterItemFrameInteraction true
```

### 更好的快速合成 (betterQuickCrafting)

快速合成时在物品栏保留一份物品

* 类型: `boolean`
* 默认值: `false`
* 参考选项: `true`, `false`
* 分类: `GCA`, `experimental`

```
/carpet betterQuickCrafting true
```

### 简单的游戏内计算器 (simpleInGameCalculator)

简单的游戏内计算器

* 类型: `boolean`
* 默认值: `false`
* 参考选项: `true`, `false`
* 分类: `GCA`

```
/carpet simpleInGameCalculator true
```

### 快速Ping好友 (fastPingFriend)

快速Ping好友

* 类型: `boolean`
* 默认值: `false`
* 参考选项: `true`, `false`
* 分类: `GCA`

```
/carpet fastPingFriend true
```

### 设置LC值 (qnmdLC)

设置LC值的高度值是多少

* 类型: `int`
* 默认值: `-1`
* 分类: `GCA`, `experimental`

```
/carpet qnmdLC 64
/carpet qnmdLC -1
```

### 修复末地水晶同步 (fixedEndCrystalSync)

修复末地水晶同步

* 类型: `boolean`
* 默认值: `false`
* 参考选项: `true`, `false`
* 分类: `GCA`, `experimental`

```
/carpet fixedEndCrystalSync true
```

### 欢迎玩家 (welcomePlayer)

欢迎玩家

* 类型: `boolean`
* 默认值: `false`
* 参考选项: `true`, `false`
* 分类: `GCA`

```
/carpet welcomePlayer true
```

### 流浪商人生成失败提醒 (wanderingTraderSpawnFailedWarning)

在流浪商人生成失败进行提醒，并告诉玩家生成失败的原因

* 类型: `boolean`
* 默认值: `false`
* 参考选项: `true`, `false`
* 分类: `GCA`, `experimental`

```
/carpet wanderingTraderSpawnFailedWarning true
```

### 流浪商人生成提醒 (wanderingTraderSpawnRemind)

流浪商人生成提醒

* 类型: `boolean`
* 默认值: `false`
* 参考选项: `true`, `false`
* 分类: `GCA`, `experimental`

```
/carpet wanderingTraderSpawnRemind true
```

### 服务器玩家转移命令 (commandTransfer)

/transfer 命令

* 类型: `boolean`
* 默认值: `false`
* 参考选项: `true`, `false`
* 分类: `GCA`, `command`
* 条件: 需要 Minecraft >= 1.21

```
/carpet commandTransfer true
/transfer <server> [port]
```

### 假人定位条 (fakePlayerLocatorBar)

在定位条上显示假玩家位置

* 类型: `boolean`
* 默认值: `true`
* 参考选项: `true`, `false`
* 分类: `GCA`, `command`, `BOT`
* 条件: 需要 Minecraft >= 1.21.6

```
/carpet fakePlayerLocatorBar true
```
