# MCUniversalEconomy Usage Guide

## Commands

The main command is `/eco` (aliases: `economy`, `money`) with the following subcommands.
You can also use shorter aliases for common commands:

### Basic Commands
- `/eco balance [player]` - View your balance or another player's balance
  - Aliases: `/balance`, `/bal`, `/money`
- `/eco pay <player> <amount>` - Pay another player
  - Alias: `/pay`

### Admin Commands
- `/eco give <player> <amount>` - Give money to a player
- `/eco take <player> <amount>` - Take money from a player
- `/eco set <player> <amount>` - Set a player's balance
- `/eco balancetop` - View the richest players
  - Alias: `/baltop`

## Permissions

### User Permissions
- `mcuniversaleconomy.use` (default: true)
  - Allows access to basic economy commands
  - Includes:
    - Checking own balance
    - Making payments to other players

### Admin Permissions
- `mcuniversaleconomy.admin` (default: op)
  - Allows access to all MCUniversalEconomy admin commands
  - Includes:
    - Viewing other players' balances
    - Giving money to players
    - Taking money from players
    - Setting player balances
    - Viewing balance top list
  - Automatically includes all user permissions

### Permission Groups
- `mcuniversaleconomy.*` (default: op)
  - Grants all MCUniversalEconomy permissions
  - Includes both admin and user permissions

## Command Usage Examples

1. Checking Balance
```
/eco balance         # Check your own balance
/balance            # Short alias to check your balance
/bal player1        # Check player1's balance (requires admin permission)
```

2. Making Payments
```
/eco pay player1 100  # Pay 100 to player1
/pay player1 100     # Short alias for payment
```

3. View Top Balances
```
/eco balancetop     # View the richest players
/balancetop         # Short alias for balance top
/baltop             # Another alias for balance top
```

4. Admin Commands
```
/eco give player1 1000   # Give 1000 to player1
/eco take player1 500    # Take 500 from player1
/eco set player1 2000    # Set player1's balance to 2000
```

## Notes

- All amounts must be positive numbers
- Players cannot pay themselves
- Admin commands require the `mcuniversaleconomy.admin` permission
- Basic economy commands are available to all players by default
- The plugin integrates with Vault for broader plugin compatibility 