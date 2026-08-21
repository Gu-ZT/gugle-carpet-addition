# 欢迎玩家功能配置说明

[[English](welcome.md) | 中文]

启用 `welcomePlayer` 规则后，玩家每次加入服务器时都会收到 `welcome.json` 中配置的消息。消息可以分为多行，并可插入玩家名、天数、随机文本和跨服入口等动态内容。

## 启用功能

临时启用：

```mcfunction
/carpet welcomePlayer true
```

设为当前存档的默认规则：

```mcfunction
/carpet setDefault welcomePlayer true
```

规则默认为 `false`。规则关闭时，配置文件会保留，但玩家加入服务器时不会收到欢迎消息。

## 配置文件位置

配置文件属于存档，而不是整个 Minecraft 实例共用。其路径为：

```text
<存档目录>/serverconfig/guglecarpetaddition/welcome.json
```

常见示例：

- 服务端：`<服务端目录>/<level-name>/serverconfig/guglecarpetaddition/welcome.json`
- 单人游戏：`.minecraft/saves/<存档名>/serverconfig/guglecarpetaddition/welcome.json`

服务器加载存档时会读取该文件。请先关闭服务器再修改，修改完成后重新启动服务器；运行期间直接编辑不会自动重新加载。

如果文件尚不存在，启动存档后会创建它。首次在规则开启的情况下有玩家加入时，默认内容为：

```json
{
  "info": {
    "message": [
      "{%player%}, welcome!"
    ],
    "args": {
      "player": "gca:player"
    }
  }
}
```

## 完整示例

```json
{
  "info": {
    "message": [
      "欢迎 {%player%} 来到服务器！",
      "今天是开服第 {%days%} 天，{%tip%}",
      "其他服务器：{%servers%}"
    ],
    "args": {
      "player": {
        "type": "gca:player",
        "color": "yellow"
      },
      "days": {
        "type": "gca:day_count",
        "data": {
          "from": "2026-01-01"
        },
        "color": "aqua"
      },
      "tip": {
        "type": "gca:random",
        "data": [
          "祝你游戏愉快！",
          "请先阅读服务器规则。",
          "遇到问题可以联系管理员。"
        ],
        "color": "green"
      },
      "servers": {
        "type": "gca:server",
        "data": [
          {
            "name": "生存服",
            "host": "survival.example.com:25565"
          },
          {
            "name": "创造服",
            "host": "creative.example.com"
          }
        ]
      }
    }
  }
}
```

该示例会向每位加入的玩家发送三条消息。`{%player%}`、`{%days%}`、`{%tip%}` 和 `{%servers%}` 都是占位符，其具体行为由 `args` 中的同名配置决定。

## 配置结构

最外层必须保留 `info`。其中 `message` 为必填字段，`args` 为可选字段：

| 字段 | 类型 | 是否必填 | 说明 |
| --- | --- | --- | --- |
| `message` | 字符串数组 | 是 | 要发送的消息；数组中的每个元素单独发送一条消息 |
| `args` | 对象 | 否 | 占位符名称与变量配置的对应关系；省略时默认为空对象 |

不使用动态变量时，可以省略 `args`；显式写成 `"args": {}` 也具有相同效果。消息中使用了占位符时，则需要通过 `args` 提供对应配置。

普通文字可以直接写入 `message`。JSON 字符串中的双引号和反斜杠需要分别写成 `\"` 和 `\\`，换行符需要写成 `\n`。配置文件是标准 JSON，不能包含注释或末尾多余的逗号。

### 占位符

占位符格式为：

```text
{%名称%}
```

名称只能使用英文字母、数字和下划线，并且必须能在 `args` 中找到完全相同的键。例如：

```json
{
  "message": [
    "欢迎 {%player_name%}！"
  ],
  "args": {
    "player_name": "gca:player"
  }
}
```

同一个占位符可以在消息中重复使用，`args` 中未被消息引用的配置不会显示。占位符没有对应配置、类型不存在或替换过程出错时，原占位符会保留在消息中，同时服务端日志会记录警告或错误。

### 变量的简写与完整写法

不需要 `data` 或自定义样式时，可以只写变量类型：

```json
"player": "gca:player"
```

这种简写等价于：

```json
"player": {
  "type": "gca:player",
  "color": "gold"
}
```

完整写法支持以下字段：

| 字段 | 是否必填 | 说明 |
| --- | --- | --- |
| `type` | 是 | 变量类型，例如 `gca:random` |
| `data` | 否 | 传给该变量的数据；不同类型要求的格式不同 |
| `color` | 否 | 变量显示样式，默认为 `gold` |

可用样式名称包括：

```text
black, dark_blue, dark_green, dark_aqua, dark_red, dark_purple,
gold, gray, dark_gray, blue, green, aqua, red, light_purple,
yellow, white, obfuscated, bold, strikethrough, underline, italic, reset
```

`color` 一次只能填写一个名称，不能同时组合多个样式。

## 内置变量类型

### `gca:player`：玩家名称

显示当前加入服务器的玩家名称。此类型不需要 `data`。

```json
"player": {
  "type": "gca:player",
  "color": "yellow"
}
```

在消息中使用：

```json
"欢迎 {%player%}！"
```

### `gca:day_count`：天数

此类型有两种用法。

不设置 `data` 时，显示主世界累计时间除以 `1728000` tick 后向下取整的结果。正常以 20 TPS 运行时，每 `1728000` tick 相当于 24 小时；睡觉等会改变主世界时间的行为也可能影响结果。

```json
"days": "gca:day_count"
```

设置起始日期时，显示从该日期到服务器当前日期的自然日数，起始当天计为第 1 天。日期按 UTC+8 时区计算，格式必须为 `yyyy-MM-dd`。

推荐的对象写法：

```json
"days": {
  "type": "gca:day_count",
  "data": {
    "from": "2026-01-01"
  },
  "color": "aqua"
}
```

也可以将日期直接写成字符串：

```json
"days": {
  "type": "gca:day_count",
  "data": "2026-01-01"
}
```

如果起始日期晚于当前日期，会退回到基于主世界累计时间的结果。日期格式无效时，该占位符无法替换，并会在服务端日志中记录错误。

### `gca:random`：随机文本

每次生成欢迎消息时，从 `data` 数组中随机选择一项。通常用于随机提示、公告或问候语。

```json
"tip": {
  "type": "gca:random",
  "data": [
    "欢迎回来！",
    "记得及时备份贵重物品。",
    "祝你今天也能找到钻石！"
  ],
  "color": "green"
}
```

`data` 必须是非空数组。没有提供数组或数组中没有可用的基础值时，会显示 `[EMPTY]`。

### `gca:server`：服务器入口

根据 `data` 数组生成一个或多个服务器入口，多个入口之间用空格分隔。鼠标悬停时会显示服务器地址；Minecraft 1.21 及以上版本中，点击入口会执行 `/transfer` 命令。

对象写法可以同时指定显示名称和地址：

```json
"servers": {
  "type": "gca:server",
  "data": [
    {
      "name": "生存服",
      "host": "survival.example.com:25565"
    },
    {
      "name": "小游戏服",
      "host": "games.example.com"
    }
  ]
}
```

显示文字会自动加上方括号，例如 `"name": "生存服"` 会显示为 `[生存服]`。如果省略 `name`，则显示 `[Server]`。

只需要地址时也可以使用字符串简写，此时同样显示 `[Server]`：

```json
"servers": {
  "type": "gca:server",
  "data": [
    "survival.example.com:25565",
    "creative.example.com"
  ]
}
```

地址可以写成 `域名` 或 `域名:端口`。使用此功能时，请确保目标 Minecraft 版本支持 `/transfer`；如果要让没有原版权限的普通玩家点击跳转，还需要启用 GCA 的 `commandTransfer` 规则。当前地址解析方式不适合直接填写包含多个冒号的 IPv6 地址。

## 纯文本示例

如果不需要任何动态内容，可以直接省略可选的 `args` 字段：

```json
{
  "info": {
    "message": [
      "欢迎来到服务器！",
      "请遵守服务器规则，祝你游戏愉快。"
    ]
  }
}
```

也可以显式添加 `"args": {}`，效果完全相同。

## 常见问题

### 修改后没有生效

配置只在服务器加载存档时读取。关闭服务器、保存文件并重新启动，然后重新加入服务器进行检查。还要确认 `welcomePlayer` 已设为 `true`。

### 文件被恢复成默认内容

无效的 JSON 或不符合上述结构的内容无法载入。当规则开启且玩家随后加入时，模组可能会重新写入默认配置。修改前建议备份原文件，并使用 JSON 校验工具检查语法。

### 消息中直接显示 `{%name%}`

检查 `args` 中是否存在 `name` 键、占位符两侧是否使用了完整的 `{%` 和 `%}`，以及 `type` 是否为支持的变量类型。变量名称区分大小写。

### 如何暂时停用欢迎消息

执行以下命令即可，无需删除配置文件：

```mcfunction
/carpet welcomePlayer false
```
