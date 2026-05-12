# TaiXiu - Minecraft Mini-game

A Sic Bo (Tai Xiu) dice betting game for Minecraft servers.

## Features
- ✅ **Betting System**: Bet on Tai (Big) or Xiu (Small).
- ✅ **Vault Integration**: Uses your server's economy.
- ✅ **Auto Sessions**: Periodic rounds with customizable timers.
- ✅ **History**: View results of the last 10 rounds.
- ✅ **Multi-Language Support**: Configurable messages.
- ✅ **Database**: Uses SQLite to store session history.

## Commands
- `/taixiu bet <tai|xiu> <amount>` - Place a bet.
- `/taixiu status` - Check current session status.
- `/taixiu history` - View session history.
- `/taixiu reload` - Reload configuration (Admin).

## Permissions
- `taixiu.use` - Basic player permissions (Default: true).
- `taixiu.admin` - Administrative permissions (Default: op).

## Requirements
- Spigot/Paper 1.16.5+
- Vault
- An Economy plugin (e.g., EssentialsX)
