# Gugle's Carpet Addition [ English | [中文](README_cn.md) ]

[![Development Builds](https://github.com/Gu-ZT/gugle-carpet-addition/workflows/Mod%20Build/badge.svg)](https://github.com/Gu-ZT/gugle-carpet-addition/actions/workflows/build.yml)
[![CurseForge downloads](http://cf.way2muchnoise.eu/full_662867_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/guglecarpetaddition)
[![Modrinth downloads](https://img.shields.io/modrinth/dt/gca?color=00AF5C&label=Modrinth%20downloads&logo=modrinth)](https://modrinth.com/mod/gca)
[![GitHub downloads](https://img.shields.io/github/downloads/Gu-ZT/gugle-carpet-addition/total?label=Github%20downloads&logo=github)](https://github.com/Gu-ZT/gugle-carpet-addition/releases)

![menu](docs/pics/menu_en.png)

## GCA

#### If you need GCA for Minecraft 1.13, [please click here](https://github.com/Gu-ZT/TISCarpet113WithGCA/releases/latest)

### openFakePlayerInventory

Allow player to open the fake player's inventory

* Type: `boolean`
* Default: `false`
* Options: `true`, `false`
* Categories: `GCA`, `BOT`

```
/carpet openFakePlayerInventory true
```

![inv](docs/pics/inv.png)

### openRealPlayerInventory

Allow player to open the real player's inventory

* Type: `String`
* Default: `false`
* Options: `true`, `false`, `ops`, `0`, `1`, `2`, `3`, `4`
* Categories: `GCA`, `experimental`

```
/carpet openRealPlayerInventory true
/carpet openRealPlayerInventory ops
```

### openFakePlayerEnderChest

Allow player to open the fake player's ender chest. Sneak to open the fake player's ender chest.

* Type: `String`
* Default: `false`
* Options: `ender_chest`, `true`, `false`
* Categories: `GCA`, `BOT`

```
/carpet openFakePlayerEnderChest true
/carpet openFakePlayerEnderChest ender_chest
```

![ender](docs/pics/ender.png)

### fakePlayerResident

Keep the fake player when exiting the level

* Type: `boolean`
* Default: `false`
* Options: `true`, `false`
* Categories: `GCA`, `BOT`

```
/carpet fakePlayerResident true
```

### fakePlayerReloadAction

Keep the fake player action when exiting the level

* Type: `boolean`
* Default: `false`
* Options: `true`, `false`
* Categories: `GCA`, `BOT`

```
/carpet fakePlayerReloadAction true
```

### fakePlayerAutoReplenishment

Make fake player to auto replenishment

* Type: `boolean`
* Default: `false`
* Options: `true`, `false`
* Categories: `GCA`, `BOT`

```
/carpet fakePlayerAutoReplenishment true
```

### fakePlayerAutoReplenishmentFormShulkerBox

Make fake player to auto replenishment from shulker box

* Type: `boolean`
* Default: `false`
* Options: `true`, `false`
* Categories: `GCA`, `BOT`

```
/carpet fakePlayerAutoReplenishmentFormShulkerBox true
```

### fakePlayerAutoFish

Make fake player to auto fish

* Type: `boolean`
* Default: `false`
* Options: `true`, `false`
* Categories: `GCA`, `BOT`

```
/carpet fakePlayerAutoFish true
```

### fakePlayerAutoReplaceTool

Make fake player to auto replace almost damaged tool

* Type: `boolean`
* Default: `false`
* Options: `true`, `false`
* Categories: `GCA`, `BOT`

```
/carpet fakePlayerAutoReplaceTool true
```

### fakePlayerPrefixName

Fake Player Prefix Name

* Type: `String`
* Default: `#none`
* Options: `#none`, `bot_`
* Categories: `GCA`, `BOT`

```
/carpet fakePlayerPrefixName bot_
/carpet fakePlayerPrefixName #none
```

### fakePlayerSuffixName

Fake Player Suffix Name

* Type: `String`
* Default: `#none`
* Options: `#none`, `_fake`
* Categories: `GCA`, `BOT`

```
/carpet fakePlayerSuffixName _fake
/carpet fakePlayerSuffixName #none
```

### commandBot

A Bot Management Menu

* Type: `String`
* Default: `ops`
* Options: `ops`, `0`, `1`, `2`, `3`, `4`, `true`, `false`
* Categories: `GCA`, `BOT`, `command`

```
/carpet commandBot ops
/bot
```

### commandTodo

A Todo Management Menu

* Type: `String`
* Default: `ops`
* Options: `ops`, `0`, `1`, `2`, `3`, `4`, `true`, `false`
* Categories: `GCA`, `command`

```
/carpet commandTodo ops
/todo
```

### commandHere

Quickly send position

* Type: `String`
* Default: `ops`
* Options: `ops`, `0`, `1`, `2`, `3`, `4`, `true`, `false`
* Categories: `GCA`, `command`
* Conditions: Requires CarpetAmsAddition loaded

```
/carpet commandHere ops
/here
```

### commandWhereis

Quickly locate players

* Type: `String`
* Default: `ops`
* Options: `ops`, `0`, `1`, `2`, `3`, `4`, `true`, `false`
* Categories: `GCA`, `command`

```
/carpet commandWhereis ops
/whereis <player>
```

### commandLoc

A Loc Management Menu

* Type: `String`
* Default: `ops`
* Options: `ops`, `0`, `1`, `2`, `3`, `4`, `true`, `false`
* Categories: `GCA`, `command`

```
/carpet commandLoc ops
/loc
```

### commandWlist

Whitelist Management

* Type: `boolean`
* Default: `false`
* Options: `true`, `false`
* Categories: `GCA`, `command`

```
/carpet commandWlist true
/wlist
```

### commandBlist

Banned List Management

* Type: `boolean`
* Default: `false`
* Options: `true`, `false`
* Categories: `GCA`, `command`

```
/carpet commandBlist true
/blist
```

### commandSop

Simple op get

* Type: `boolean`
* Default: `false`
* Options: `true`, `false`
* Categories: `GCA`, `command`

```
/carpet commandSop true
/sop
```

### betterFenceGatePlacement

Make the placed fence gate have the same block status as the fence gate you clicked

* Type: `boolean`
* Default: `false`
* Options: `true`, `false`
* Categories: `GCA`

```
/carpet betterFenceGatePlacement true
```

### betterWoodStrip

Only the axe with "Strip" in its name is allowed to peel logs

* Type: `boolean`
* Default: `false`
* Options: `true`, `false`
* Categories: `GCA`

```
/carpet betterWoodStrip true
```

### betterSignInteraction

Make the block attached to the sign interact when you right-click it

* Type: `boolean`
* Default: `false`
* Options: `true`, `false`
* Categories: `GCA`

```
/carpet betterSignInteraction true
```

### betterItemFrameInteraction

Make the block attached to the ItemFrame interact when you right-click it

* Type: `boolean`
* Default: `false`
* Options: `true`, `false`
* Categories: `GCA`

```
/carpet betterItemFrameInteraction true
```

### betterQuickCrafting

Keep an item in the inventory during quick crafting

* Type: `boolean`
* Default: `false`
* Options: `true`, `false`
* Categories: `GCA`, `experimental`

```
/carpet betterQuickCrafting true
```

### simpleInGameCalculator

Simple In-Game Calculator

* Type: `boolean`
* Default: `false`
* Options: `true`, `false`
* Categories: `GCA`

```
/carpet simpleInGameCalculator true
```

### fastPingFriend

Fast ping friend

* Type: `boolean`
* Default: `false`
* Options: `true`, `false`
* Categories: `GCA`

```
/carpet fastPingFriend true
```

### qnmdLC

What is the height value for setting the LC value

* Type: `int`
* Default: `-1`
* Categories: `GCA`, `experimental`

```
/carpet qnmdLC 64
/carpet qnmdLC -1
```

### fixedEndCrystalSync

Fixed End Crystal Sync

* Type: `boolean`
* Default: `false`
* Options: `true`, `false`
* Categories: `GCA`, `experimental`

```
/carpet fixedEndCrystalSync true
```

### welcomePlayer

Welcome Player

* Type: `boolean`
* Default: `false`
* Options: `true`, `false`
* Categories: `GCA`

```
/carpet welcomePlayer true
```

### wanderingTraderSpawnFailedWarning

Wandering Trader Spawn Failed Warning

* Type: `boolean`
* Default: `false`
* Options: `true`, `false`
* Categories: `GCA`, `experimental`

```
/carpet wanderingTraderSpawnFailedWarning true
```

### wanderingTraderSpawnRemind

Wandering Trader Spawn Remind

* Type: `boolean`
* Default: `false`
* Options: `true`, `false`
* Categories: `GCA`, `experimental`

```
/carpet wanderingTraderSpawnRemind true
```

### commandTransfer

/transfer command

* Type: `boolean`
* Default: `false`
* Options: `true`, `false`
* Categories: `GCA`, `command`
* Conditions: Requires Minecraft >= 1.21

```
/carpet commandTransfer true
/transfer <server> [port]
```

### fakePlayerLocatorBar

Display Fake Player Positions On The Locator Bar

* Type: `boolean`
* Default: `true`
* Options: `true`, `false`
* Categories: `GCA`, `command`, `BOT`
* Conditions: Requires Minecraft >= 1.21.6

```
/carpet fakePlayerLocatorBar true
```
