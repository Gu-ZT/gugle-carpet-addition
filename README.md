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

**Usage**: Right-click on a fake player to open their inventory

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

**Usage**: Right-click on other real players to open their inventory, requires appropriate permission level

### openFakePlayerEnderChest

Allow player to open the fake player's ender chest

* Type: `String`
* Default: `false`
* Options: `ender_chest`, `true`, `false`
* Categories: `GCA`, `BOT`

```
/carpet openFakePlayerEnderChest true
/carpet openFakePlayerEnderChest ender_chest
```

**Usage**: Sneak (hold Shift) + right-click on a fake player to open their ender chest

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

Make fake player to auto fish. When the fishing hook catches a fish, it will automatically reel in and cast again.

* Type: `boolean`
* Default: `false`
* Options: `true`, `false`
* Categories: `GCA`, `BOT`

```
/carpet fakePlayerAutoFish true
```

**Usage**: Let the fake player hold a fishing rod and use `/player <name> use` to start fishing. The fake player will automatically complete the reel-in and cast operations.

### fakePlayerAutoReplaceTool

Make fake player to auto replace almost damaged tool

* Type: `String`
* Default: `false`
* Options: `true`, `false`, `keep`
* Categories: `GCA`, `BOT`

> `true`: Tools with Mending will keep 10 durability; other tools will automatically switch after they break.

> `keep`: All tools will keep 10 durability.

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

A Bot Management Menu for saving, loading, and grouping fake players

* Type: `String`
* Default: `ops`
* Options: `ops`, `0`, `1`, `2`, `3`, `4`, `true`, `false`
* Categories: `GCA`, `BOT`, `command`

```
/carpet commandBot ops
```

**Command Usage**:
```
/bot                                    # Show saved bot list
/bot list [page]                        # Show bot list with pagination
/bot add <player> <desc>                # Save current online fake player (including position, actions, etc.)
/bot load <player>                      # Load saved fake player
/bot remove <player>                    # Delete saved fake player
/bot group                              # Show bot group list
/bot group create <name>                # Create bot group
/bot group remove <name>                # Delete bot group
/bot group add <bot> <group>            # Add bot to group
/bot group load <group>                 # Load all bots in group
/bot group unload <group>               # Unload all bots in group
/bot group info <group>                 # View group details
/bot group generated <name> <count> [load]  # Batch generate bots and group them
```

### commandTodo

A Todo Management Menu with support for adding, removing, and marking completion

* Type: `String`
* Default: `ops`
* Options: `ops`, `0`, `1`, `2`, `3`, `4`, `true`, `false`
* Categories: `GCA`, `command`

```
/carpet commandTodo ops
```

**Command Usage**:
```
/todo                           # Show todo list
/todo list [page]               # Show todo list with pagination
/todo add <desc>                # Add new todo item
/todo remove <id>               # Remove todo item
/todo success <id> [true/false] # Mark todo as done/undone
```

### commandHere

Quickly broadcast your current position to all players and add glowing effect to yourself

* Type: `String`
* Default: `ops`
* Options: `ops`, `0`, `1`, `2`, `3`, `4`, `true`, `false`
* Categories: `GCA`, `command`
* Conditions: Only works when CarpetAmsAddition is NOT loaded

```
/carpet commandHere ops
```

**Command Usage**:
```
/here   # Broadcast your coordinates, showing current dimension and converted coordinates
        # Also outputs Xaero's Minimap compatible waypoint format
```

### commandWhereis

Quickly locate players and add glowing effect to target

* Type: `String`
* Default: `ops`
* Options: `ops`, `0`, `1`, `2`, `3`, `4`, `true`, `false`
* Categories: `GCA`, `command`

```
/carpet commandWhereis ops
```

**Command Usage**:
```
/whereis <player>   # Query specified player's location
/vris <player>      # Alias, same function
```

### commandLoc

A Location Management Menu for saving, viewing, and deleting location points

* Type: `String`
* Default: `ops`
* Options: `ops`, `0`, `1`, `2`, `3`, `4`, `true`, `false`
* Categories: `GCA`, `command`

```
/carpet commandLoc ops
```

**Command Usage**:
```
/loc                    # Show location list
/loc list [page]        # Show locations with pagination
/loc add <desc>         # Add location at current position
/loc remove <id>        # Remove specified location
/loc info <id>          # View location details (including dimension converted coordinates)
```

### commandWlist

Whitelist Management, allows authorized regular players to manage the whitelist

* Type: `boolean`
* Default: `false`
* Options: `true`, `false`
* Categories: `GCA`, `command`

```
/carpet commandWlist true
```

**Command Usage**:
```
/wlist                              # Show current whitelist
/wlist add <player>                 # Add player to whitelist
/wlist remove <player>              # Remove player from whitelist
/wlist permission add <player>      # Grant player whitelist management permission (OP only)
/wlist permission remove <player>   # Revoke player's whitelist management permission (OP only)
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
