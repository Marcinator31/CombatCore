# CombatCore

A combat system for PaperMC 1.21.x with anti-combat-log, safe-zone regions,
and configurable command/item blocking — driven almost entirely by `config.yml`.

## Features

- **Combat mode (tagging):** players entering combat are tagged for a configurable
  duration. Fighting refreshes the timer.
- **Anti combat-log:** logging out while tagged kills the player (items drop) and
  optionally broadcasts it.
- **Hotbar timer:** the remaining combat time is shown above the hotbar (action bar),
  fully formattable via MiniMessage.
- **Safe-zone regions:** define cuboid regions that combat-tagged players cannot
  walk or teleport into (they get pushed back).
- **Command blocking:** block commands like `rtp`, `tpa`, `home` while in combat.
- **Item blocking:** block items like `TRIDENT`, plus dedicated toggles for
  elytra gliding and ender pearls.
- **Everything configurable:** durations, messages (MiniMessage), toggles, lists.
- **Bypass permissions** for staff.

## Build with GitHub Actions (recommended — no local Maven needed)

This repo ships with two workflows in `.github/workflows/`:

- **build.yml** — builds the plugin on every push to `main`/`master`, and lets
  you trigger a build manually.
- **release.yml** — builds and publishes a GitHub Release when you push a tag.

### One-time setup

```bash
git init
git add .
git commit -m "Initial commit: CombatCore"
git branch -M main
git remote add origin https://github.com/<your-user>/<your-repo>.git
git push -u origin main
```

The push automatically starts the build. To get the jar:

1. Go to your repo on GitHub → **Actions** tab.
2. Open the latest **Build Plugin** run.
3. Download the **CombatCore** artifact (a zip containing the `.jar`).

### Cutting a release (gets you a permanent download link)

```bash
git tag v1.0.0
git push origin v1.0.0
```

This builds the plugin and attaches `CombatCore-1.0.0.jar` to a new
**Release** on your repo's Releases page.

## Build locally (alternative)

Requires JDK 21 and Maven, with internet access to the PaperMC repository.

```bash
mvn clean package
```

The compiled plugin will be at `target/CombatCore-1.0.0.jar`.
Drop it into your server's `plugins/` folder and restart.

> Note: the `paper-api` version in `pom.xml` is set to `1.21.8-R0.1-SNAPSHOT`.
> If your exact server version differs, change that line to match (the plugin
> targets the whole 1.21.x API and `api-version: '1.21'` in plugin.yml keeps it
> compatible across the 1.21 line).

## Commands

| Command | Description | Permission |
|---|---|---|
| `/combatcore reload` | Reload config & regions | `combatcore.admin` |
| `/combatcore status` | Show your combat status | — |
| `/combatcore tag [player]` | Force-tag a player | `combatcore.admin` |
| `/combatcore untag [player]` | Remove a tag | `combatcore.admin` |
| `/combatregion pos1` / `pos2` | Set selection corners | `combatcore.admin` |
| `/combatregion create <name>` | Create a region from selection | `combatcore.admin` |
| `/combatregion delete <name>` | Delete a region | `combatcore.admin` |
| `/combatregion list` | List regions | `combatcore.admin` |

Aliases: `/cc`, `/combat`, `/creg`, `/cregion`.

## Permissions

- `combatcore.admin` — all admin actions (default: op)
- `combatcore.bypass.commands` — use blocked commands in combat
- `combatcore.bypass.items` — use blocked items/elytra in combat
- `combatcore.bypass.regions` — enter protected regions in combat

## Creating a safe zone

1. Stand at one corner: `/combatregion pos1`
2. Stand at the opposite corner: `/combatregion pos2`
3. `/combatregion create spawn`

Players in combat can no longer enter that region.

## Config

See `src/main/resources/config.yml` — every option is commented. Colours use
[MiniMessage](https://docs.advntr.dev/minimessage/format.html). Run
`/combatcore reload` after editing.
