# Welcome Player Configuration Guide

[English | [中文](welcome_cn.md)]

When the `welcomePlayer` rule is enabled, players receive the messages configured in `welcome.json` every time they join the server. Messages may span multiple entries and can include dynamic content such as the player's name, a day count, random text, and links to other servers.

## Enabling the Feature

Enable it temporarily:

```mcfunction
/carpet welcomePlayer true
```

Make it the default rule for the current world:

```mcfunction
/carpet setDefault welcomePlayer true
```

The rule defaults to `false`. When the rule is disabled, the configuration file is preserved, but joining players do not receive welcome messages.

## Configuration File Location

The configuration belongs to an individual world rather than being shared by the entire Minecraft instance. Its path is:

```text
<world directory>/serverconfig/guglecarpetaddition/welcome.json
```

Common examples:

- Dedicated server: `<server directory>/<level-name>/serverconfig/guglecarpetaddition/welcome.json`
- Singleplayer: `.minecraft/saves/<world name>/serverconfig/guglecarpetaddition/welcome.json`

The file is read when the server loads the world. Stop the server before editing it, then restart the server after saving your changes. Editing the file while the server is running does not reload it automatically.

If the file does not exist, it is created after the world starts. When the rule is enabled and a player joins for the first time, the default content is:

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

## Complete Example

```json
{
  "info": {
    "message": [
      "Welcome, {%player%}!",
      "This is day {%days%} of the server. {%tip%}",
      "Other servers: {%servers%}"
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
          "Have fun!",
          "Please read the server rules.",
          "Contact an administrator if you need help."
        ],
        "color": "green"
      },
      "servers": {
        "type": "gca:server",
        "data": [
          {
            "name": "Survival",
            "host": "survival.example.com:25565"
          },
          {
            "name": "Creative",
            "host": "creative.example.com"
          }
        ]
      }
    }
  }
}
```

This example sends three messages to each joining player. `{%player%}`, `{%days%}`, `{%tip%}`, and `{%servers%}` are placeholders whose behavior is defined by the matching keys in `args`.

## Configuration Structure

The outermost `info` key must be preserved. Inside it, `message` is required and `args` is optional:

| Field | Type | Required | Description |
| --- | --- | --- | --- |
| `message` | Array of strings | Yes | Messages to send; every array element is sent as a separate message |
| `args` | Object | No | Maps placeholder names to argument definitions; defaults to an empty object when omitted |

If no dynamic arguments are used, `args` may be omitted. Explicitly writing `"args": {}` has the same effect. If a message contains placeholders, matching definitions must be provided through `args`.

Plain text can be written directly in `message`. In JSON strings, double quotes and backslashes must be escaped as `\"` and `\\`, respectively, while a newline is written as `\n`. The configuration file must be valid JSON: comments and trailing commas are not allowed.

### Placeholders

Placeholder syntax is:

```text
{%name%}
```

Names may contain only letters, digits, and underscores, and must exactly match a key in `args`. For example:

```json
{
  "message": [
    "Welcome, {%player_name%}!"
  ],
  "args": {
    "player_name": "gca:player"
  }
}
```

The same placeholder may be used multiple times. Definitions in `args` that are not referenced by a message are not displayed. If a placeholder has no matching definition, its type is unknown, or replacement fails, the original placeholder remains in the message and the server log records a warning or error.

### Shorthand and Full Argument Forms

When neither `data` nor a custom style is needed, an argument can be written as only its type:

```json
"player": "gca:player"
```

This shorthand is equivalent to:

```json
"player": {
  "type": "gca:player",
  "color": "gold"
}
```

The full form supports these fields:

| Field | Required | Description |
| --- | --- | --- |
| `type` | Yes | Argument type, such as `gca:random` |
| `data` | No | Data passed to the argument; the expected format depends on its type |
| `color` | No | Display style for the replaced value; defaults to `gold` |

Available style names include:

```text
black, dark_blue, dark_green, dark_aqua, dark_red, dark_purple,
gold, gray, dark_gray, blue, green, aqua, red, light_purple,
yellow, white, obfuscated, bold, strikethrough, underline, italic, reset
```

Only one name can be specified in `color`; multiple styles cannot be combined in one value.

## Built-in Argument Types

### `gca:player`: Player Name

Displays the name of the player currently joining the server. This type does not require `data`.

```json
"player": {
  "type": "gca:player",
  "color": "yellow"
}
```

Use it in a message like this:

```json
"Welcome, {%player%}!"
```

### `gca:day_count`: Day Count

This type has two modes.

Without `data`, it displays the overworld's accumulated time divided by `1728000` ticks and rounded down. At the normal rate of 20 TPS, `1728000` ticks are equivalent to 24 hours. Actions that change overworld time, such as sleeping, may also affect the result.

```json
"days": "gca:day_count"
```

When a start date is provided, it displays the number of calendar days from that date through the server's current date, counting the start date as day 1. Dates are calculated in the UTC+8 time zone and must use the `yyyy-MM-dd` format.

The recommended object form is:

```json
"days": {
  "type": "gca:day_count",
  "data": {
    "from": "2026-01-01"
  },
  "color": "aqua"
}
```

The date can also be provided directly as a string:

```json
"days": {
  "type": "gca:day_count",
  "data": "2026-01-01"
}
```

If the start date is later than the current date, the result falls back to the overworld-time calculation. If the date format is invalid, the placeholder cannot be replaced and an error is recorded in the server log.

### `gca:random`: Random Text

Selects one item at random from the `data` array each time a welcome message is generated. This is useful for random tips, announcements, or greetings.

```json
"tip": {
  "type": "gca:random",
  "data": [
    "Welcome back!",
    "Remember to keep valuable items safe.",
    "Good luck finding diamonds today!"
  ],
  "color": "green"
}
```

`data` must be a non-empty array. If no array is provided or it contains no usable primitive values, `[EMPTY]` is displayed.

### `gca:server`: Server Links

Generates one or more server links from the `data` array, separated by spaces. Hovering over a link shows its server address. In Minecraft 1.21 and later, clicking a link runs the `/transfer` command.

The object form specifies both a display name and an address:

```json
"servers": {
  "type": "gca:server",
  "data": [
    {
      "name": "Survival",
      "host": "survival.example.com:25565"
    },
    {
      "name": "Minigames",
      "host": "games.example.com"
    }
  ]
}
```

Square brackets are added to the display name automatically. For example, `"name": "Survival"` is displayed as `[Survival]`. If `name` is omitted, `[Server]` is displayed.

When only an address is needed, use the string shorthand. Each entry is then displayed as `[Server]`:

```json
"servers": {
  "type": "gca:server",
  "data": [
    "survival.example.com:25565",
    "creative.example.com"
  ]
}
```

Addresses may use either `hostname` or `hostname:port`. Make sure the target Minecraft version supports `/transfer`. To let ordinary players without the vanilla permission use clickable transfers, also enable GCA's `commandTransfer` rule. The current address parser is not suitable for IPv6 addresses containing multiple colons.

## Plain-text Example

If no dynamic content is needed, the optional `args` field can be omitted entirely:

```json
{
  "info": {
    "message": [
      "Welcome to the server!",
      "Please follow the server rules and have fun."
    ]
  }
}
```

Explicitly adding `"args": {}` has exactly the same effect.

## Troubleshooting

### Changes Do Not Take Effect

The configuration is read only when the server loads the world. Stop the server, save the file, restart the server, and then rejoin to test it. Also make sure `welcomePlayer` is set to `true`.

### The File Is Replaced with the Default Content

Invalid JSON or content that does not match the documented structure cannot be loaded. If the rule is enabled and a player subsequently joins, the mod may write the default configuration again. Back up the original file before editing and use a JSON validator to check its syntax.

### `{%name%}` Appears Literally in a Message

Make sure that `args` contains a `name` key, the placeholder uses the complete `{%` and `%}` delimiters, and `type` is one of the supported argument types. Argument names are case-sensitive.

### Temporarily Disabling Welcome Messages

Run the following command without deleting the configuration file:

```mcfunction
/carpet welcomePlayer false
```
