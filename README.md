# MCUniversalEconomy

A universal economy solution that works seamlessly across both FabricMC and PaperMC servers.

## Overview

MCUniversalEconomy is designed to provide a unified economy system for Minecraft servers, supporting both FabricMC and PaperMC platforms. This project aims to bridge the gap between different server implementations while maintaining consistent economy features and data across your network.

For command usage, please refer to [USAGE.md](USAGE.md).

### Key Features

- Universal compatibility with FabricMC and PaperMC servers
- MariaDB database support with customizable prefix
- Transaction history tracking
- API support for both platforms:
  - FabricMC: Common Economy API
  - PaperMC: Vault API (via VaultUnlockedAPI)

## Technical Details

### Dependencies

- **FabricMC Server**:
  - Fabric API
  - Common Economy API

- **PaperMC Server**:
  - VaultUnlockedAPI

### Database Structure

The project uses MariaDB with a structure inspired by XConomy:
- Account data table
- Transaction records table
- Configurable table prefix support

### Cross-Platform Compatibility

The mod/plugin is designed to work as a universal JAR file that can be placed in either:
- The `mods` folder of a FabricMC server
- The `plugins` folder of a PaperMC server

The system automatically detects the server type and loads the appropriate implementation.

## Installation

1. Ensure you have MariaDB installed and configured
2. Download the latest release JAR file
3. Place the JAR file in:
   - FabricMC: `mods` folder
   - PaperMC: `plugins` folder
4. Start the server
5. Configure the `config.yml` file that is generated on first run

## Configuration

The configuration file will be generated at:
- FabricMC: `config/mcuniversaleconomy.yml`
- PaperMC: `plugins/MCUniversalEconomy/config.yml`

### Database Configuration

```yaml
# Database Configuration
database:
  host: localhost
  port: 3306
  name: mceconomy
  username: your_username
  password: your_password
  table_prefix: mcue_

# General Settings
settings:
  initial_balance: 0.0
  currency_symbol: "$"
  currency_format: "#,##0.00"
  language: en
  check_updates: true
  server_id: null  # Optional server identifier. Use String value
  
# Transaction Settings
transactions:
  enable_logging: true
  payment_tax: 0.0
  minimum_payment: 0.0
  maximum_payment: -1  # -1 means no limit
```

## API Usage

For both fabricmc and papermc, our plugin will use the Common Economy API and Vault API respectively.

## Building from Source

1. Clone the repository
2. Run `./gradlew build`
3. Find the compiled JAR in `build/libs`

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

[GPL-3.0](https://www.gnu.org/licenses/gpl-3.0.en.html)

## Credits

This project takes inspiration from:
- XConomy (Database structure)

## Project Structure

### Core Components

```
src/main/java/com/iruanp/mcuniversaleconomy/
├── MCUniversalEconomy.java          # Common base class
├── MCUniversalEconomyFabric.java    # Main Fabric mod entry point
├── MCUniversalEconomyPaper.java     # Main Paper plugin entry point
├── commands/
│   ├── EconomyCommand.java          # Base command handling
│   ├── fabric/
│   │   └── FabricEconomyCommand.java # Fabric-specific commands
│   └── paper/
│       └── PaperEconomyCommand.java  # Paper-specific commands
├── config/
│   ├── ModConfig.java               # Configuration management
│   └── YamlHandler.java             # YAML configuration handling
├── database/
│   └── DatabaseManager.java         # MariaDB connection and operations
├── economy/
│   ├── UniversalEconomyService.java # Common economy interface
│   ├── UniversalEconomyServiceImpl.java # Implementation of economy service
│   └── paper/
│       └── VaultEconomyProvider.java  # Vault API implementation
├── platform/
│   └── PlatformType.java            # Platform detection utility
└── util/
    └── UnifiedLogger.java           # Cross-platform logging utility
```

### Database Schema

```sql
-- Accounts Table
CREATE TABLE {prefix}accounts (
    uuid VARCHAR(36) PRIMARY KEY,
    username VARCHAR(16) NOT NULL,
    balance DECIMAL(20,2) NOT NULL DEFAULT 0.00,
    last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Transactions Table
CREATE TABLE {prefix}transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_uuid VARCHAR(36),
    target_uuid VARCHAR(36),
    amount DECIMAL(20,2) NOT NULL,
    tax DECIMAL(20,2) NOT NULL,
    type VARCHAR(16) NOT NULL,
    server_id VARCHAR(36),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (source_uuid) REFERENCES {prefix}accounts(uuid),
    FOREIGN KEY (target_uuid) REFERENCES {prefix}accounts(uuid)
);
``` 